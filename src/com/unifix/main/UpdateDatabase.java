package com.unifix.main;

import com.unifix.database.DBConnection;
import java.io.*;
import java.sql.*;
import java.util.Properties;

public class UpdateDatabase {
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DB_NAME;
    
    public static void main(String[] args) {
        System.out.println("Updating UniFix Database Schema...");
        
        // Load the database configuration
        if (!loadConfiguration()) {
            System.out.println("❌ Failed to load database configuration.");
            return;
        }
        
        // Check if database exists, if not create it
        if (!createDatabaseIfNotExists()) {
            System.out.println("❌ Failed to create database.");
            return;
        }
        
        // Connect to the database and update schema
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Failed to connect to database after creation.");
                return;
            }
            
            // Create tables if they don't exist
            createTablesIfNotExist(conn);
            
            // Check if image_path column exists
            boolean columnExists = checkColumnExists(conn, "complaints", "image_path");
            if (columnExists) {
                System.out.println("✅ image_path column already exists.");
            } else {
                System.out.println("⚙️ Adding image_path column to complaints table...");
                
                try (Statement stmt = conn.createStatement()) {
                    String sql = "ALTER TABLE complaints ADD COLUMN image_path VARCHAR(255) AFTER description";
                    stmt.executeUpdate(sql);
                    System.out.println("✅ image_path column added successfully!");
                } catch (Exception e) {
                    System.out.println("❌ Error adding image_path column: " + e.getMessage());
                }
            }
            
            System.out.println("Database update completed.");
        } catch (Exception e) {
            System.out.println("❌ Error updating database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean loadConfiguration() {
        try (FileInputStream input = new FileInputStream("db_config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            String host = prop.getProperty("db.host", "localhost");
            String port = prop.getProperty("db.port", "3306");
            DB_NAME = prop.getProperty("db.name", "unifix_db");
            
            URL = String.format("jdbc:mysql://%s:%s/", host, port);
            USER = prop.getProperty("db.user", "root");
            PASSWORD = prop.getProperty("db.password", "");
            
            return true;
        } catch (IOException e) {
            System.out.println("❌ Failed to load database configuration: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean createDatabaseIfNotExists() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                try (Statement stmt = conn.createStatement()) {
                    String sql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                    stmt.executeUpdate(sql);
                    System.out.println("✅ Database " + DB_NAME + " created or already exists.");
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error creating database: " + e.getMessage());
            return false;
        }
    }
    
    private static void createTablesIfNotExist(Connection conn) {
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
            System.out.println("✅ Users table created or already exists.");

            // Create complaints table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS complaints (" +
                "complaint_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT, " +
                "category VARCHAR(100), " +
                "description TEXT, " +
                "location VARCHAR(100), " +
                "status ENUM('Pending', 'In Progress', 'Resolved') DEFAULT 'Pending', " +
                "assigned_to VARCHAR(100), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id))"
            );
            System.out.println("✅ Complaints table created or already exists.");

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
            System.out.println("✅ Solutions table created or already exists.");
        } catch (SQLException e) {
            System.out.println("❌ Error creating tables: " + e.getMessage());
        }
    }
    
    private static boolean checkColumnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, tableName, columnName);
            boolean exists = rs.next(); // If there's a result, the column exists
            rs.close();
            return exists;
        } catch (Exception e) {
            System.out.println("❌ Error checking if column exists: " + e.getMessage());
            return false;
        }
    }
}