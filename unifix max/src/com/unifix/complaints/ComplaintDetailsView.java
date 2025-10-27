package com.unifix.complaints;

import com.unifix.database.DBConnection;
import com.unifix.utils.UIUtilities;
import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ComplaintDetailsView extends JFrame {
    private final int complaintId;
    private JLabel categoryLabel;
    private JLabel locationLabel;
    private JLabel statusLabel;
    private JLabel dateLabel;
    private JTextArea descriptionArea;
    private JLabel imageLabel;
    private JButton closeButton;

    public ComplaintDetailsView(int complaintId) {
        this.complaintId = complaintId;
        setupUI();
        loadComplaintDetails();
        setVisible(true);
    }

    private void setupUI() {
        setTitle("Complaint Details");
        setSize(700, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main panel with header
        JPanel outerPanel = new JPanel(new BorderLayout());
        
        // Add header with logo
        JPanel headerPanel = UIUtilities.createHeaderPanel("Full Complaint Details (Read Only)");
        outerPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info Panel for brief details
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR, 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        categoryLabel = new JLabel("Category: ");
        locationLabel = new JLabel("Location: ");
        statusLabel = new JLabel("Status: ");
        dateLabel = new JLabel("Date: ");

        categoryLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        locationLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        statusLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        dateLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        
        categoryLabel.setForeground(UIUtilities.TEXT_COLOR);
        locationLabel.setForeground(UIUtilities.TEXT_COLOR);
        statusLabel.setForeground(UIUtilities.TEXT_COLOR);
        dateLabel.setForeground(UIUtilities.TEXT_COLOR);

        infoPanel.add(categoryLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(dateLabel);

        // Description Panel
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBackground(Color.WHITE);
        descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR, 2, true),
                "Description",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                UIUtilities.NORMAL_FONT,
                UIUtilities.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(UIUtilities.NORMAL_FONT);
        descriptionArea.setBackground(new Color(250, 250, 250));
        descriptionArea.setForeground(UIUtilities.TEXT_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setPreferredSize(new Dimension(350, 150));
        descriptionPanel.add(scrollPane, BorderLayout.CENTER);

        // Image Panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR, 2, true),
                "Attached Image",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                UIUtilities.NORMAL_FONT,
                UIUtilities.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        imageLabel = new JLabel("Loading image...", JLabel.CENTER);
        imageLabel.setFont(UIUtilities.NORMAL_FONT);
        imageLabel.setForeground(Color.GRAY);
        imageLabel.setPreferredSize(new Dimension(400, 300));
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        
        closeButton = UIUtilities.createStyledButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        // Add components to main panel
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(descriptionPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(imagePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(buttonPanel);

        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(null);
        outerPanel.add(mainScrollPane, BorderLayout.CENTER);
        
        add(outerPanel);
    }

    private void loadComplaintDetails() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, 
                "Unable to connect to database. Please check your database configuration.", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // First check if image_path column exists
            boolean imagePathExists = checkImagePathColumnExists(conn);
            System.out.println("Image path column exists in ComplaintDetailsView: " + imagePathExists);
            
            // Build query based on whether image_path column exists
            String sql;
            if (imagePathExists) {
                sql = "SELECT c.category, c.location, c.description, c.status, c.created_at, " +
                      "c.image_path, u.name as user_name " +
                      "FROM complaints c " +
                      "JOIN users u ON c.user_id = u.user_id " +
                      "WHERE c.complaint_id = ?";
            } else {
                sql = "SELECT c.category, c.location, c.description, c.status, c.created_at, " +
                      "u.name as user_name " +
                      "FROM complaints c " +
                      "JOIN users u ON c.user_id = u.user_id " +
                      "WHERE c.complaint_id = ?";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, complaintId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String category = rs.getString("category");
                        String location = rs.getString("location");
                        String status = rs.getString("status");
                        String date = rs.getTimestamp("created_at").toString();
                        String description = rs.getString("description");
                        String userName = rs.getString("user_name");
                        
                        // Set the values in the UI
                        categoryLabel.setText("Category: " + category);
                        locationLabel.setText("Location: " + location);
                        statusLabel.setText("Status: " + status);
                        dateLabel.setText("Date: " + date);
                        descriptionArea.setText(description);
                        
                        // Handle image if exists and column is available
                        String imagePath = null;
                        if (imagePathExists) {
                            try {
                                imagePath = rs.getString("image_path");
                                System.out.println("Retrieved image path in details view: " + imagePath);
                            } catch (SQLException e) {
                                System.out.println("Note: image_path column not found in result set: " + e.getMessage());
                            }
                        }
                        
                        if (imagePath != null && !imagePath.isEmpty()) {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                System.out.println("Loading image from: " + imagePath);
                                ImageIcon originalIcon = new ImageIcon(imagePath);
                                Image image = originalIcon.getImage();
                                
                                // Scale image to fit nicely
                                int maxWidth = 400;
                                int maxHeight = 300;
                                
                                double scale = Math.min(
                                    (double) maxWidth / image.getWidth(null),
                                    (double) maxHeight / image.getHeight(null)
                                );
                                
                                int scaledWidth = (int) (scale * image.getWidth(null));
                                int scaledHeight = (int) (scale * image.getHeight(null));
                                
                                Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                                imageLabel.setIcon(new ImageIcon(scaledImage));
                                imageLabel.setText("");
                            } else {
                                imageLabel.setIcon(null);
                                imageLabel.setText("Image not found at: " + imagePath);
                                System.out.println("Image not found at: " + imagePath);
                            }
                        } else {
                            imageLabel.setIcon(null);
                            imageLabel.setText("No image attached");
                        }
                        
                        // Update window title to include user name
                        setTitle("Complaint Details - Filed by " + userName);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Could not find complaint details", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        dispose();
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading complaint details: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            dispose();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if the image_path column exists in the complaints table
     * @param conn Database connection
     * @return true if the column exists, false otherwise
     */
    private boolean checkImagePathColumnExists(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "complaints", "image_path");
            boolean exists = rs.next(); // If there's a result, the column exists
            rs.close();
            return exists;
        } catch (SQLException e) {
            System.out.println("Error checking for image_path column: " + e.getMessage());
            return false;
        }
    }
}