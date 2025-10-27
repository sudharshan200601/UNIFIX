package com.unifix.dashboard;

import com.unifix.database.DBConnection;
import com.unifix.solutions.SolutionPage;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TechnicianDashboard extends JFrame {
    private JPanel mainPanel;
    private JTable assignedTable;
    private JButton updateStatusBtn, addSolutionBtn, logoutBtn;
    private int userId;
    private String userName;

    public TechnicianDashboard() {
        setTitle("Technician Dashboard - UniFix");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize main panel
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, Technician!", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Create table model
        String[] columns = {"ID", "Category", "Location", "Description", "Priority", "Status", "Date"};
        Object[][] data = fetchAssignedComplaints();
        assignedTable = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(assignedTable);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updateStatusBtn = new JButton("Update Status");
        addSolutionBtn = new JButton("Add Solution");
        logoutBtn = new JButton("Logout");

        buttonPanel.add(updateStatusBtn);
        buttonPanel.add(addSolutionBtn);
        buttonPanel.add(logoutBtn);

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        updateStatusBtn.addActionListener(e -> updateStatus());
        addSolutionBtn.addActionListener(e -> addSolution());
        logoutBtn.addActionListener(e -> logout());

        // Add main panel to frame
        add(mainPanel);
        setVisible(true);
    }

    private Object[][] fetchAssignedComplaints() {
        Object[][] data = new Object[0][7];
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT complaint_id, category, location, description, " +
                        "priority, status, created_at FROM complaints " +
                        "WHERE assigned_to = ? AND status != 'Resolved' " +
                        "ORDER BY priority DESC, created_at ASC";
            
            PreparedStatement stmt = conn.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            );
            stmt.setString(1, userName);
            
            ResultSet rs = stmt.executeQuery();
            rs.last();
            data = new Object[rs.getRow()][7];
            rs.beforeFirst();

            int row = 0;
            while (rs.next()) {
                data[row][0] = rs.getInt("complaint_id");
                data[row][1] = rs.getString("category");
                data[row][2] = rs.getString("location");
                data[row][3] = rs.getString("description");
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

    private void updateStatus() {
        int row = assignedTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first!");
            return;
        }

        int complaintId = (int) assignedTable.getValueAt(row, 0);
        String[] statuses = {"In Progress", "Resolved"};

        String newStatus = (String) JOptionPane.showInputDialog(
            this,
            "Update Status:",
            "Update Complaint Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            statuses[0]
        );

        if (newStatus != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "UPDATE complaints SET status = ? WHERE complaint_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newStatus);
                stmt.setInt(2, complaintId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Status updated successfully!");
                refreshTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage());
            }
        }
    }

    private void addSolution() {
        int row = assignedTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first!");
            return;
        }

        int complaintId = (int) assignedTable.getValueAt(row, 0);
        new SolutionPage(complaintId, this);
    }

    private void refreshTable() {
        Object[][] newData = fetchAssignedComplaints();
        assignedTable.setModel(new DefaultTableModel(
            newData,
            new String[]{"ID", "Category", "Location", "Description", "Priority", "Status", "Date"}
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