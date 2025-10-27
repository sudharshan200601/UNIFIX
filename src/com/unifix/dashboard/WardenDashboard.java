package com.unifix.dashboard;

import com.unifix.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class WardenDashboard extends JFrame {
    private JPanel mainPanel;
    private JTable complaintsTable;
    private JButton assignButton, viewDetailsBtn, logoutBtn;
    private int userId;
    private String userName;

    public WardenDashboard() {
        setTitle("Warden Dashboard - UniFix");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize main panel
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, Warden!", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Create table model
        String[] columns = {"ID", "Student", "Category", "Location", "Priority", "Status", "Date"};
        Object[][] data = fetchComplaints();
        complaintsTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(complaintsTable);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        assignButton = new JButton("Assign to Technician");
        viewDetailsBtn = new JButton("View Details");
        logoutBtn = new JButton("Logout");

        buttonPanel.add(assignButton);
        buttonPanel.add(viewDetailsBtn);
        buttonPanel.add(logoutBtn);

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        assignButton.addActionListener(e -> assignComplaint());
        viewDetailsBtn.addActionListener(e -> viewDetails());
        logoutBtn.addActionListener(e -> logout());

        // Add main panel to frame
        add(mainPanel);
        setVisible(true);
    }

    private Object[][] fetchComplaints() {
        Object[][] data = new Object[0][7];
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.complaint_id, u.name, c.category, c.location, " +
                        "c.priority, c.status, c.created_at " +
                        "FROM complaints c JOIN users u ON c.user_id = u.user_id " +
                        "WHERE c.status = 'Pending' ORDER BY c.priority DESC";
            
            Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            
            ResultSet rs = stmt.executeQuery(sql);
            rs.last();
            data = new Object[rs.getRow()][7];
            rs.beforeFirst();

            int row = 0;
            while (rs.next()) {
                data[row][0] = rs.getInt("complaint_id");
                data[row][1] = rs.getString("name");
                data[row][2] = rs.getString("category");
                data[row][3] = rs.getString("location");
                data[row][4] = rs.getString("priority");
                data[row][5] = rs.getString("status");
                data[row][6] = rs.getTimestamp("created_at");
                row++;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching complaints: " + e.getMessage());
        }
        return data;
    }

    private void assignComplaint() {
        int row = complaintsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first!");
            return;
        }

        int complaintId = (int) complaintsTable.getValueAt(row, 0);
        String[] technicians = {"Tech1", "Tech2", "Tech3"}; // Should fetch from database

        String selectedTech = (String) JOptionPane.showInputDialog(
            this,
            "Choose Technician:",
            "Assign Complaint",
            JOptionPane.QUESTION_MESSAGE,
            null,
            technicians,
            technicians[0]
        );

        if (selectedTech != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE complaints SET status = 'In Progress', " +
                           "assigned_to = ? WHERE complaint_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, selectedTech);
                stmt.setInt(2, complaintId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Complaint assigned successfully!");
                refreshTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error assigning complaint: " + e.getMessage());
            }
        }
    }

    private void viewDetails() {
        int row = complaintsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first!");
            return;
        }

        int complaintId = (int) complaintsTable.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.*, u.name FROM complaints c " +
                        "JOIN users u ON c.user_id = u.user_id " +
                        "WHERE c.complaint_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, complaintId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String details = String.format(
                    "Complaint Details:\n\n" +
                    "ID: %d\n" +
                    "Student: %s\n" +
                    "Category: %s\n" +
                    "Subcategory: %s\n" +
                    "Location: %s\n" +
                    "Description: %s\n" +
                    "Priority: %s\n" +
                    "Status: %s\n" +
                    "Created: %s",
                    rs.getInt("complaint_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("subcategory"),
                    rs.getString("location"),
                    rs.getString("description"),
                    rs.getString("priority"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                );

                JOptionPane.showMessageDialog(
                    this,
                    details,
                    "Complaint Details",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + e.getMessage());
        }
    }

    private void refreshTable() {
        Object[][] newData = fetchComplaints();
        complaintsTable.setModel(new DefaultTableModel(
            newData,
            new String[]{"ID", "Student", "Category", "Location", "Priority", "Status", "Date"}
        ));
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