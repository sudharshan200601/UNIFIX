package com.unifix.complaints;

import com.unifix.database.DBConnection;
import com.unifix.utils.Location;
import com.unifix.utils.UIUtilities;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ComplaintForm extends JFrame {
    private final JComboBox<Location> locationBox;
    private final JComboBox<String> categoryBox;
    private final JTextArea descriptionArea;
    private final JButton submitButton;
    private final JButton uploadImageButton;
    private final JLabel imageNameLabel;
    private final int userId;
    private File selectedImage = null;
    private final JFrame parentFrame;
    private static final String[] CATEGORIES = {"Maintenance", "Security", "Cleanliness", "Infrastructure", "Other"};

    public ComplaintForm(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.userId = userId;
        this.locationBox = new JComboBox<>(Location.values());
        this.categoryBox = new JComboBox<>(CATEGORIES);
        this.descriptionArea = new JTextArea(5, 20);
        this.uploadImageButton = new JButton("Upload Image");
        this.imageNameLabel = new JLabel("No image selected");
        this.submitButton = new JButton("Submit");
        
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        setTitle("Submit New Complaint");
        setSize(500, 600);
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
        JPanel headerPanel = UIUtilities.createHeaderPanel("Submit New Complaint");
        outerPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Put Location and Category at the top
        JPanel locationPanel = new JPanel(new BorderLayout(5, 5));
        locationPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        locationPanel.add(locationLabel, BorderLayout.NORTH);
        locationPanel.add(locationBox, BorderLayout.CENTER);
        
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 5));
        categoryPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        categoryPanel.add(categoryLabel, BorderLayout.NORTH);
        categoryPanel.add(categoryBox, BorderLayout.CENTER);
        
        // Description area
        JPanel descriptionPanel = new JPanel(new BorderLayout(5, 5));
        descriptionPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
        
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(UIUtilities.NORMAL_FONT);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        descriptionPanel.add(scrollPane, BorderLayout.CENTER);

        // Image upload panel
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JLabel imageLabel = new JLabel("Attach Image (Optional):");
        imageLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        imagePanel.add(imageLabel, BorderLayout.NORTH);
        
        JPanel imageUploadPanel = new JPanel(new BorderLayout(5, 5));
        imageUploadPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        imageUploadPanel.add(uploadImageButton, BorderLayout.WEST);
        imageNameLabel.setFont(UIUtilities.NORMAL_FONT);
        imageNameLabel.setForeground(Color.GRAY);
        imageUploadPanel.add(imageNameLabel, BorderLayout.CENTER);
        imagePanel.add(imageUploadPanel, BorderLayout.CENTER);

        // Style buttons
        submitButton.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        submitButton.setBackground(UIUtilities.PRIMARY_COLOR);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        
        uploadImageButton.setFont(UIUtilities.NORMAL_FONT);
        uploadImageButton.setBackground(UIUtilities.SECONDARY_COLOR);
        uploadImageButton.setForeground(Color.WHITE);
        uploadImageButton.setFocusPainted(false);

        // Submit button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        buttonPanel.add(submitButton);
        
        // Add all components to main panel with spacing
        mainPanel.add(locationPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(categoryPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(descriptionPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(imagePanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(null);
        outerPanel.add(mainScrollPane, BorderLayout.CENTER);
        
        add(outerPanel);
        setVisible(true);
    }

    private void setupListeners() {
        submitButton.addActionListener(e -> submitComplaint());
        
        uploadImageButton.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImage = fileChooser.getSelectedFile();
                imageNameLabel.setText(selectedImage.getName());
                System.out.println("Selected image: " + selectedImage.getAbsolutePath());
            }
        });
    }

    // This section was causing duplicated declarations - removed
    
    private void submitComplaint() {
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "Error: Invalid user ID!");
            return;
        }

        String category = (String) categoryBox.getSelectedItem();
        Location location = (Location) locationBox.getSelectedItem();
        String description = descriptionArea.getText().trim();

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a description!");
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Error connecting to database!");
            return;
        }

        try {
            String sql;
            String imagePath = null;
            
            // Handle image upload if an image was selected
            if (selectedImage != null) {
                // Create uploads directory if it doesn't exist
                String uploadsDir = "uploads/complaints";
                Path uploadsDirPath = Paths.get(uploadsDir);
                if (!Files.exists(uploadsDirPath)) {
                    Files.createDirectories(uploadsDirPath);
                }
                
                // Generate unique filename using timestamp and user ID
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                String extension = selectedImage.getName().substring(selectedImage.getName().lastIndexOf('.'));
                String newFileName = userId + "_" + timestamp + extension;
                imagePath = uploadsDir + "/" + newFileName;
                
                // Copy the image to the uploads directory
                Path source = selectedImage.toPath();
                Path destination = Paths.get(imagePath);
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("Image saved to: " + imagePath);
                
                // Check if image_path column exists
                boolean imagePathExists = checkImagePathColumnExists(conn);
                
                if (imagePathExists) {
                    sql = "INSERT INTO complaints (user_id, category, location, description, image_path, status) " +
                          "VALUES (?, ?, ?, ?, ?, 'Pending')";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, category);
                        stmt.setString(3, location.toString());
                        stmt.setString(4, description);
                        stmt.setString(5, imagePath);
                        
                        stmt.executeUpdate();
                    }
                } else {
                    // If image_path column doesn't exist, save without the image
                    sql = "INSERT INTO complaints (user_id, category, location, description, status) " +
                          "VALUES (?, ?, ?, ?, 'Pending')";
                    
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, category);
                        stmt.setString(3, location.toString());
                        stmt.setString(4, description);
                        
                        stmt.executeUpdate();
                    }
                    
                    JOptionPane.showMessageDialog(this, 
                        "Complaint submitted, but image couldn't be saved (database schema issue).",
                        "Partial Success", 
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // No image selected, simple insert
                sql = "INSERT INTO complaints (user_id, category, location, description, status) " +
                      "VALUES (?, ?, ?, ?, 'Pending')";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, category);
                    stmt.setString(3, location.toString());
                    stmt.setString(4, description);
                    
                    stmt.executeUpdate();
                }
            }
            
            JOptionPane.showMessageDialog(this, "Complaint submitted successfully!");
            
            // Refresh complaints table in parent window if it's StudentDashboard
            if (parentFrame != null && parentFrame.getClass().getSimpleName().equals("StudentDashboard")) {
                try {
                    // Use reflection to call the loadComplaintsData method
                    java.lang.reflect.Method refreshMethod = parentFrame.getClass().getDeclaredMethod("loadComplaintsData");
                    refreshMethod.setAccessible(true);
                    refreshMethod.invoke(parentFrame);
                } catch (Exception ex) {
                    System.out.println("Error refreshing complaints: " + ex.getMessage());
                }
            }
            
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving image: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conn.close();
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