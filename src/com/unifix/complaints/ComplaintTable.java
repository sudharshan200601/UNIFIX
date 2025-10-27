package com.unifix.complaints;

import com.unifix.database.DBConnection;
import com.unifix.utils.UIUtilities;
import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ComplaintTable extends JFrame {
    private JTable table;
    private int userId;
    private JPanel detailsPanel;
    private JLabel categoryLabel, locationLabel, statusLabel, dateLabel, userLabel;
    private JTextArea descriptionArea;
    private JLabel imageLabel;
    private int selectedComplaintId = -1;

    public ComplaintTable(int userId) {
        this.userId = userId;
        setTitle("Complaints - UniFix");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main panel with header
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        
        // Add header with logo
        JPanel headerPanel = UIUtilities.createHeaderPanel("My Complaints");
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create split pane for table and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left side - Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        
        // Table header panel with instructions
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JLabel instructionLabel = new JLabel("Select a complaint to view details. ⚠️ Double-click for full details view with image.");
        instructionLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        instructionLabel.setForeground(UIUtilities.SECONDARY_COLOR);
        tableHeaderPanel.add(instructionLabel, BorderLayout.WEST);
        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);
        
        // Table setup
        String[] columns = {"ID", "Category", "Location", "Status", "Date"};
        Object[][] data = fetchComplaints();
        
        table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setGridColor(new Color(230, 230, 230));
        table.getTableHeader().setBackground(UIUtilities.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        
        // Mouse listener for both single and double click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedComplaintId = (Integer) table.getValueAt(row, 0);
                    
                    // Single click loads the details in the right panel
                    if (e.getClickCount() == 1) {
                        loadComplaintDetails(selectedComplaintId);
                    }
                    
                    // Double click opens the details in a separate window
                    if (e.getClickCount() >= 2) {
                        System.out.println("Double clicked on complaint ID: " + selectedComplaintId);
                        new ComplaintDetailsView(selectedComplaintId);
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Right side - Details Panel
        detailsPanel = createDetailsPanel();
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
        detailsScrollPane.setBorder(BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR));
        
        // Add components to split pane
        splitPane.setLeftComponent(tablePanel);
        splitPane.setRightComponent(detailsScrollPane);
        splitPane.setDividerLocation(500);
        splitPane.setDividerSize(5);
        splitPane.setEnabled(true);
        
        // Add bottom buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JButton closeButton = UIUtilities.createStyledButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonsPanel.add(closeButton);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Complaint Details");
        titleLabel.setFont(UIUtilities.HEADER_FONT);
        titleLabel.setForeground(UIUtilities.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Info labels
        categoryLabel = new JLabel("Category: ");
        locationLabel = new JLabel("Location: ");
        statusLabel = new JLabel("Status: ");
        dateLabel = new JLabel("Date: ");
        userLabel = new JLabel("Filed by: ");

        // Style labels
        categoryLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        locationLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        statusLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        dateLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        userLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        
        // Set foreground color
        categoryLabel.setForeground(UIUtilities.TEXT_COLOR);
        locationLabel.setForeground(UIUtilities.TEXT_COLOR);
        statusLabel.setForeground(UIUtilities.TEXT_COLOR);
        dateLabel.setForeground(UIUtilities.TEXT_COLOR);
        userLabel.setForeground(UIUtilities.TEXT_COLOR);

        // Description area
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(UIUtilities.NORMAL_FONT);
        descriptionArea.setBackground(new Color(250, 250, 250));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR),
                "Description",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                UIUtilities.NORMAL_FONT,
                UIUtilities.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR),
                "Attached Image",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                UIUtilities.NORMAL_FONT,
                UIUtilities.PRIMARY_COLOR
            ),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Image label
        imageLabel = new JLabel("Select a complaint to view image", JLabel.CENTER);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setFont(UIUtilities.NORMAL_FONT);
        imageLabel.setForeground(Color.GRAY);
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        // Create info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtilities.PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Add all components to info panel
        infoPanel.add(categoryLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(locationLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(dateLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(userLabel);
        
        // Add all components to main panel
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(descScrollPane);
        panel.add(Box.createVerticalStrut(15));
        panel.add(imagePanel);

        return panel;
    }

    private void loadComplaintDetails(int complaintId) {
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
            System.out.println("Image path column exists: " + imagePathExists);
            
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
                        categoryLabel.setText("Category: " + rs.getString("category"));
                        locationLabel.setText("Location: " + rs.getString("location"));
                        statusLabel.setText("Status: " + rs.getString("status"));
                        dateLabel.setText("Date: " + rs.getTimestamp("created_at").toString());
                        userLabel.setText("Filed by: " + rs.getString("user_name"));
                        descriptionArea.setText(rs.getString("description"));
                        
                        // Handle image if exists and column is available
                        String imagePath = null;
                        if (imagePathExists) {
                            try {
                                imagePath = rs.getString("image_path");
                                System.out.println("Retrieved image path: " + imagePath);
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
                                int maxWidth = 300;
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
                        
                        detailsPanel.revalidate();
                        detailsPanel.repaint();
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading complaint details: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    private Object[][] fetchComplaints() {
        Object[][] data = new Object[0][5];
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT complaint_id, category, location, status, created_at " +
                        "FROM complaints WHERE user_id = ? ORDER BY created_at DESC";
            
            PreparedStatement stmt = conn.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            rs.last();
            data = new Object[rs.getRow()][5];
            rs.beforeFirst();

            int row = 0;
            while (rs.next()) {
                data[row][0] = rs.getInt("complaint_id");
                data[row][1] = rs.getString("category");
                data[row][2] = rs.getString("location");
                data[row][3] = rs.getString("status");
                data[row][4] = rs.getTimestamp("created_at");
                row++;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching complaints: " + e.getMessage());
        }
        return data;
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