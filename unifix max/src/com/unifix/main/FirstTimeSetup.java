package com.unifix.main;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import javax.swing.*;

public class FirstTimeSetup extends JFrame {
    private JTextField hostField, portField, userField, dbNameField;
    private JPasswordField passwordField;
    private static final String SETUP_FILE = ".setup_complete";
    private static final String CONFIG_FILE = "db_config.properties";

    public static boolean isFirstRun() {
        return !Files.exists(Paths.get(SETUP_FILE));
    }

    public FirstTimeSetup() {
        setTitle("UniFix - First Time Setup");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to UniFix Setup Wizard");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Database configuration panel
        JPanel configPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        
        hostField = new JTextField("localhost");
        portField = new JTextField("3306");
        dbNameField = new JTextField("unifix_db");
        userField = new JTextField("root");
        passwordField = new JPasswordField();

        configPanel.add(new JLabel("Database Host:"));
        configPanel.add(hostField);
        configPanel.add(new JLabel("Port:"));
        configPanel.add(portField);
        configPanel.add(new JLabel("Database Name:"));
        configPanel.add(dbNameField);
        configPanel.add(new JLabel("Username:"));
        configPanel.add(userField);
        configPanel.add(new JLabel("Password:"));
        configPanel.add(passwordField);

        mainPanel.add(configPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Test Connection button
        JButton testButton = new JButton("Test Connection");
        testButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        testButton.addActionListener(e -> testConnection());
        mainPanel.add(testButton);
        mainPanel.add(Box.createVerticalStrut(10));

        // Setup Database button
        JButton setupButton = new JButton("Setup Database");
        setupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        setupButton.addActionListener(e -> setupDatabase());
        mainPanel.add(setupButton);

        add(mainPanel);
        setVisible(true);
    }

    private void testConnection() {
        String url = String.format("jdbc:mysql://%s:%s/",
            hostField.getText(),
            portField.getText()
        );

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url,
                    userField.getText(),
                    new String(passwordField.getPassword()))) {
                JOptionPane.showMessageDialog(this,
                    "Connection successful!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Connection failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupDatabase() {
        String url = String.format("jdbc:mysql://%s:%s/",
            hostField.getText(),
            portField.getText()
        );

        try {
            // Create database and tables
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url,
                    userField.getText(),
                    new String(passwordField.getPassword()))) {
                
                // Create database
                String createDB = String.format("CREATE DATABASE IF NOT EXISTS %s", dbNameField.getText());
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(createDB);
                }

                // Switch to the new database
                url = url + dbNameField.getText();
                try (Connection dbConn = DriverManager.getConnection(url,
                        userField.getText(),
                        new String(passwordField.getPassword()))) {
                    
                    // Create tables
                    createTables(dbConn);
                }

                // Save configuration
                saveConfiguration();

                // Create setup complete file
                Files.createFile(Paths.get(SETUP_FILE));

                JOptionPane.showMessageDialog(this,
                    "Database setup completed successfully!\nThe application will now start.",
                    "Setup Complete",
                    JOptionPane.INFORMATION_MESSAGE);

                // Close setup wizard and start main application
                dispose();
                new UniFix();

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Setup failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTables(Connection conn) throws SQLException {
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

            // Create complaints table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS complaints (" +
                "complaint_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT, " +
                "category VARCHAR(100), " +
                "subcategory VARCHAR(100), " +
                "description TEXT, " +
                "location VARCHAR(100), " +
                "priority ENUM('Low', 'Medium', 'High') DEFAULT 'Low', " +
                "status ENUM('Pending', 'In Progress', 'Resolved') DEFAULT 'Pending', " +
                "assigned_to VARCHAR(100), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id))"
            );

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
        }
    }

    private void saveConfiguration() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write("db.host=" + hostField.getText() + "\n");
            writer.write("db.port=" + portField.getText() + "\n");
            writer.write("db.name=" + dbNameField.getText() + "\n");
            writer.write("db.user=" + userField.getText() + "\n");
            writer.write("db.password=" + new String(passwordField.getPassword()) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}