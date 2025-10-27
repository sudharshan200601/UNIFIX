package com.unifix.database;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DB_NAME;
    private static boolean isInitialized = false;

    private static void loadConfiguration() {
        if (!isInitialized) {
            try (FileInputStream input = new FileInputStream("db_config.properties")) {
                Properties prop = new Properties();
                prop.load(input);

                String host = prop.getProperty("db.host", "localhost");
                String port = prop.getProperty("db.port", "3306");
                DB_NAME = prop.getProperty("db.name", "unifix_db");
                
                URL = String.format("jdbc:mysql://%s:%s/%s", host, port, DB_NAME);
                USER = prop.getProperty("db.user", "root");
                PASSWORD = prop.getProperty("db.password", "");
                
                isInitialized = true;
            } catch (IOException e) {
                System.out.println("❌ Failed to load database configuration: " + e.getMessage());
                // Use default values if config file is not found
                URL = "jdbc:mysql://localhost:3306/unifix_db";
                USER = "root";
                PASSWORD = "";
                DB_NAME = "unifix_db";
                isInitialized = true;
            }
        }
    }

    public static Connection getConnection() {
        loadConfiguration();
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Database Connected Successfully!");
        } catch (SQLException e) {
            if (e.getMessage().contains("Unknown database")) {
                System.out.println("⚠️ Database does not exist. Attempting to create it...");
                createDatabase();
                // Try connecting again
                try {
                    conn = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("✅ Database Connected Successfully!");
                } catch (SQLException e2) {
                    System.out.println("❌ Database Connection Failed After Create Attempt: " + e2.getMessage());
                }
            } else {
                System.out.println("❌ Database Connection Failed: " + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found: " + e.getMessage());
        }
        return conn;
    }
    
    private static void createDatabase() {
        String baseUrl = URL.substring(0, URL.lastIndexOf("/"));
        try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASSWORD)) {
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                stmt.executeUpdate(sql);
                System.out.println("✅ Database " + DB_NAME + " created successfully!");
                
                // Create initial schema
                try (Connection dbConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                    createInitialSchema(dbConn);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error creating database: " + e.getMessage());
        }
    }
    
    private static void createInitialSchema(Connection conn) {
        try {
            ScriptRunner runner = new ScriptRunner(conn, false, true);
            
            // Try to run the schema SQL file if it exists
            File schemaFile = new File("sql/unifix_schema.sql");
            if (schemaFile.exists()) {
                System.out.println("✅ Running schema SQL script...");
                runner.runScript(new BufferedReader(new FileReader(schemaFile)));
            } else {
                createTablesManually(conn);
            }
        } catch (Exception e) {
            System.out.println("❌ Error creating schema: " + e.getMessage());
            // Fallback to manual table creation
            createTablesManually(conn);
        }
    }
    
    private static void createTablesManually(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Create users table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "role ENUM('Student', 'Warden', 'Technician', 'Admin') NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );
            System.out.println("✅ Users table created successfully!");

            // Create complaints table with image_path
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS complaints (" +
                "complaint_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT, " +
                "category VARCHAR(100), " +
                "description TEXT, " +
                "image_path VARCHAR(255), " +
                "location VARCHAR(100), " +
                "status ENUM('Pending', 'In Progress', 'Resolved') DEFAULT 'Pending', " +
                "assigned_to VARCHAR(100), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id))"
            );
            System.out.println("✅ Complaints table created successfully!");

            // Create solutions table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS solutions (" +
                "solution_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "complaint_id INT, " +
                "topic VARCHAR(100), " +
                "resolution TEXT, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id))"
            );
            System.out.println("✅ Solutions table created successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error creating tables: " + e.getMessage());
        }
    }
    
    // Simple ScriptRunner class to execute SQL scripts
    private static class ScriptRunner {
        private Connection connection;
        private boolean stopOnError;
        private boolean autoCommit;
        
        public ScriptRunner(Connection connection, boolean stopOnError, boolean autoCommit) {
            this.connection = connection;
            this.stopOnError = stopOnError;
            this.autoCommit = autoCommit;
        }
        
        public void runScript(BufferedReader reader) throws IOException, SQLException {
            StringBuilder command = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                
                command.append(line);
                
                if (line.trim().endsWith(";")) {
                    // Execute the command
                    try (Statement statement = connection.createStatement()) {
                        String sql = command.toString().trim();
                        // Skip USE statements as we're already connected to the DB
                        if (!sql.toUpperCase().startsWith("USE")) {
                            statement.execute(sql);
                        }
                    } catch (SQLException e) {
                        if (stopOnError) {
                            throw e;
                        } else {
                            System.out.println("⚠️ Error executing: " + command + " : " + e.getMessage());
                        }
                    }
                    
                    command = new StringBuilder();
                }
            }
        }
    }
}