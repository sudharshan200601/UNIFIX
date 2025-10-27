package com.unifix.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Utility class for UI-related functionality
 */
public class UIUtilities {
    
    // Color scheme for the application
    public static final Color PRIMARY_COLOR = new Color(0, 102, 204);    // Deep blue
    public static final Color SECONDARY_COLOR = new Color(255, 153, 0);  // Orange
    public static final Color ACCENT_COLOR = new Color(51, 153, 102);    // Green
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 245); // Light gray/blue
    public static final Color TEXT_COLOR = new Color(50, 50, 50);        // Dark gray
    
    // Fonts
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 14);
    
    /**
     * Creates a stylish header panel with the UniFix logo and title
     */
    public static JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.setBackground(PRIMARY_COLOR);
        
        // Logo
        ImageIcon originalIcon = new ImageIcon("resources/logo.png");
        if (originalIcon.getIconWidth() > 0) { // Check if image loaded successfully
            Image image = originalIcon.getImage();
            // Scale logo to appropriate size
            Image scaledImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            headerPanel.add(logoLabel, BorderLayout.WEST);
        }
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    /**
     * Creates a styled button with consistent look and feel
     */
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        return button;
    }
    
    /**
     * Applies consistent styling to a JPanel
     */
    public static void applyPanelStyle(JPanel panel) {
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}