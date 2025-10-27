package com.unifix.main;

import com.unifix.auth.LoginPage;
import com.unifix.database.DBConnection;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Testing UniFix Application...");
        System.out.println("==============================");
        
        // Test database connection
        System.out.println("Checking Database Connection...");
        Connection conn = DBConnection.getConnection();
        
        if (conn != null) {
            System.out.println("✅ Database connection successful!");
            System.out.println("Launching UniFix Application...\n");
            
            // Launch the login page
            javax.swing.SwingUtilities.invokeLater(() -> {
                new LoginPage();
            });
        } else {
            System.out.println("❌ Database connection failed!");
            System.out.println("Please check your database settings in DBConnection.java");
            System.exit(1);
        }
    }
}