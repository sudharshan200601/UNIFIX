package com.unifix.dashboard;

import com.unifix.database.DBConnection;
import com.unifix.utils.UIUtilities;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JTable usersTable, complaintsTable;
    private JButton addUserBtn, removeUserBtn, generateReportBtn, logoutBtn;
    private int userId;
    private String userName;

    public AdminDashboard() {
        setTitle("Admin Dashboard - UniFix");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize main panel
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Header Panel with Logo
        JPanel headerPanel = UIUtilities.createHeaderPanel("Admin Dashboard");

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Users Panel
        JPanel usersPanel = createUsersPanel();
        tabbedPane.addTab("Users Management", usersPanel);

        // Complaints Panel
        JPanel complaintsPanel = createComplaintsPanel();
        tabbedPane.addTab("Complaints Overview", complaintsPanel);

        // Statistics Panel
        JPanel statsPanel = createStatisticsPanel();
        tabbedPane.addTab("Statistics", statsPanel);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generateReportBtn = new JButton("Generate Report");
        logoutBtn = new JButton("Logout");

        buttonPanel.add(generateReportBtn);
        buttonPanel.add(logoutBtn);

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        generateReportBtn.addActionListener(e -> generateReport());
        logoutBtn.addActionListener(e -> logout());

        // Add main panel to frame
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Users table
        String[] columns = {"ID", "Name", "Email", "Role", "Created"};
        Object[][] data = fetchUsers();
        usersTable = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        usersTable.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        JScrollPane scrollPane = new JScrollPane(usersTable);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addUserBtn = new JButton("Add User");
        removeUserBtn = new JButton("Remove User");
        
        addUserBtn.addActionListener(e -> addUser());
        removeUserBtn.addActionListener(e -> removeUser());
        
        buttonPanel.add(addUserBtn);
        buttonPanel.add(removeUserBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createComplaintsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Complaints table
        String[] columns = {"ID", "User", "Category", "Status", "Assigned To", "Created"};
        Object[][] data = fetchComplaints();
        complaintsTable = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        complaintsTable.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        
        // Add double-click listener with debugging
        complaintsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = complaintsTable.getSelectedRow();
                if (row != -1) {
                    // Handle double-click to open detailed view
                    if (e.getClickCount() >= 2) {
                        int complaintId = (Integer) complaintsTable.getValueAt(row, 0);
                        System.out.println("Admin panel: Double clicked on complaint ID: " + complaintId);
                        new com.unifix.complaints.ComplaintDetailsView(complaintId);
                    }
                }
            }
        });
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        
        JPanel filterControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterControlsPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Pending", "In Progress", "Resolved"});
        filterControlsPanel.add(new JLabel("Filter by Status: "));
        filterControlsPanel.add(statusFilter);
        
        // Add instruction label
        JLabel instructionLabel = new JLabel("⚠️ Double-click on a complaint to view full details with image");
        instructionLabel.setFont(UIUtilities.NORMAL_FONT.deriveFont(Font.BOLD));
        instructionLabel.setForeground(UIUtilities.SECONDARY_COLOR);
        
        filterPanel.add(filterControlsPanel, BorderLayout.WEST);
        filterPanel.add(instructionLabel, BorderLayout.EAST);
        
        statusFilter.addActionListener(e -> filterComplaints((String)statusFilter.getSelectedItem()));
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add statistics cards
        panel.add(createStatCard("Total Complaints", fetchTotalComplaints()));
        panel.add(createStatCard("Pending Complaints", fetchPendingComplaints()));
        panel.add(createStatCard("Resolved Today", fetchResolvedToday()));
        panel.add(createStatCard("Average Resolution Time", fetchAvgResolutionTime()));
        
        return panel;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        
        card.add(titleLabel);
        card.add(valueLabel);
        
        return card;
    }

    private Object[][] fetchUsers() {
        Object[][] data = new Object[0][5];
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT user_id, name, email, role, created_at FROM users ORDER BY created_at DESC";
            Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            ResultSet rs = stmt.executeQuery(sql);
            
            rs.last();
            data = new Object[rs.getRow()][5];
            rs.beforeFirst();

            int row = 0;
            while (rs.next()) {
                data[row][0] = rs.getInt("user_id");
                data[row][1] = rs.getString("name");
                data[row][2] = rs.getString("email");
                data[row][3] = rs.getString("role");
                data[row][4] = rs.getTimestamp("created_at");
                row++;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching users: " + e.getMessage());
        }
        return data;
    }

    private Object[][] fetchComplaints() {
        Object[][] data = new Object[0][6];
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.complaint_id, u.name, c.category, " +
                        "c.status, c.assigned_to, c.created_at " +
                        "FROM complaints c JOIN users u ON c.user_id = u.user_id " +
                        "ORDER BY c.created_at DESC";
            
            Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            ResultSet rs = stmt.executeQuery(sql);
            
            rs.last();
            data = new Object[rs.getRow()][6];
            rs.beforeFirst();

            int row = 0;
            while (rs.next()) {
                data[row][0] = rs.getInt("complaint_id");
                data[row][1] = rs.getString("name");
                data[row][2] = rs.getString("category");
                data[row][3] = rs.getString("status");
                data[row][4] = rs.getString("assigned_to");
                data[row][5] = rs.getTimestamp("created_at");
                row++;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching complaints: " + e.getMessage());
        }
        return data;
    }

    private String fetchTotalComplaints() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as total FROM complaints";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return String.valueOf(rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String fetchPendingComplaints() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as pending FROM complaints WHERE status = 'Pending'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return String.valueOf(rs.getInt("pending"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String fetchResolvedToday() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as resolved FROM complaints " +
                        "WHERE status = 'Resolved' AND DATE(created_at) = CURRENT_DATE";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return String.valueOf(rs.getInt("resolved"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String fetchAvgResolutionTime() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT AVG(TIMESTAMPDIFF(HOUR, created_at, updated_at)) as avg_time " +
                        "FROM complaints c JOIN solutions s ON c.complaint_id = s.complaint_id " +
                        "WHERE c.status = 'Resolved'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double avgHours = rs.getDouble("avg_time");
                return String.format("%.1f hrs", avgHours);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private void addUser() {
        // Show dialog to add new user
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Warden", "Technician", "Admin"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add New User",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nameField.getText());
                stmt.setString(2, emailField.getText());
                stmt.setString(3, new String(passwordField.getPassword()));
                stmt.setString(4, (String)roleBox.getSelectedItem());
                stmt.executeUpdate();
                
                refreshUsersTable();
                JOptionPane.showMessageDialog(this, "User added successfully!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage());
            }
        }
    }

    private void removeUser() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to remove!");
            return;
        }

        int userId = (int)usersTable.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove this user?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                
                refreshUsersTable();
                JOptionPane.showMessageDialog(this, "User removed successfully!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error removing user: " + e.getMessage());
            }
        }
    }

    private void filterComplaints(String status) {
        if (status.equals("All")) {
            complaintsTable.setModel(new DefaultTableModel(
                fetchComplaints(),
                new String[]{"ID", "User", "Category", "Status", "Assigned To", "Created"}
            ));
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.complaint_id, u.name, c.category, " +
                        "c.status, c.assigned_to, c.created_at " +
                        "FROM complaints c JOIN users u ON c.user_id = u.user_id " +
                        "WHERE c.status = ? ORDER BY c.created_at DESC";
            
            PreparedStatement stmt = conn.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            rs.last();
            Object[][] data = new Object[rs.getRow()][6];
            rs.beforeFirst();

            int row = 0;
            while (rs.next()) {
                data[row][0] = rs.getInt("complaint_id");
                data[row][1] = rs.getString("name");
                data[row][2] = rs.getString("category");
                data[row][3] = rs.getString("status");
                data[row][4] = rs.getString("assigned_to");
                data[row][5] = rs.getTimestamp("created_at");
                row++;
            }

            complaintsTable.setModel(new DefaultTableModel(
                data,
                new String[]{"ID", "User", "Category", "Status", "Assigned To", "Created"}
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make all cells non-editable
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error filtering complaints: " + e.getMessage());
        }
    }

    private void generateReport() {
        // Implement report generation logic
        StringBuilder report = new StringBuilder();
        report.append("UniFix Complaint Management System - Status Report\n");
        report.append("=================================================\n\n");
        
        report.append("Summary Statistics:\n");
        report.append("Total Complaints: ").append(fetchTotalComplaints()).append("\n");
        report.append("Pending Complaints: ").append(fetchPendingComplaints()).append("\n");
        report.append("Resolved Today: ").append(fetchResolvedToday()).append("\n");
        report.append("Average Resolution Time: ").append(fetchAvgResolutionTime()).append("\n\n");
        
        try (Connection conn = DBConnection.getConnection()) {
            // Category-wise breakdown
            report.append("Complaints by Category:\n");
            String catSql = "SELECT category, COUNT(*) as count FROM complaints GROUP BY category";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(catSql);
            while (rs.next()) {
                report.append(rs.getString("category")).append(": ")
                      .append(rs.getInt("count")).append("\n");
            }
            
            // Status-wise breakdown
            report.append("\nComplaints by Status:\n");
            String statSql = "SELECT status, COUNT(*) as count FROM complaints GROUP BY status";
            rs = stmt.executeQuery(statSql);
            while (rs.next()) {
                report.append(rs.getString("status")).append(": ")
                      .append(rs.getInt("count")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "System Report",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshUsersTable() {
        Object[][] newData = fetchUsers();
        usersTable.setModel(new DefaultTableModel(
            newData,
            new String[]{"ID", "Name", "Email", "Role", "Created"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new com.unifix.auth.LoginPage();
        }
    }

    // Method to set user info when logging in
    public void setUserInfo(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        JLabel welcomeLabel = (JLabel) ((JPanel)mainPanel.getComponent(0)).getComponent(0);
        welcomeLabel.setText("Welcome, " + userName + "!");
    }
}