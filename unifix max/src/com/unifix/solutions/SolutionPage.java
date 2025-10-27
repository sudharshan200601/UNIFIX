package com.unifix.solutions;

import com.unifix.database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class SolutionPage extends JFrame {
    private JTextField topicField;
    private JTextArea resolutionArea;
    private JButton submitButton;
    private int complaintId;
    private JFrame parentFrame;

    public SolutionPage(int complaintId, JFrame parentFrame) {
        this.complaintId = complaintId;
        this.parentFrame = parentFrame;
        
        setTitle("Add Solution - UniFix");
        setSize(500, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Topic field
        topicField = new JTextField();
        mainPanel.add(labeled("Solution Topic:", topicField));

        // Resolution area
        resolutionArea = new JTextArea(5, 20);
        resolutionArea.setLineWrap(true);
        resolutionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resolutionArea);
        mainPanel.add(labeled("Resolution Details:", scrollPane));

        // Submit button
        submitButton = new JButton("Submit Solution");
        submitButton.addActionListener(e -> submitSolution());
        mainPanel.add(submitButton);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel labeled(String text, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(new JLabel(text), BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void submitSolution() {
        String topic = topicField.getText().trim();
        String resolution = resolutionArea.getText().trim();

        if (topic.isEmpty() || resolution.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Insert solution
            String sql = "INSERT INTO solutions (complaint_id, topic, resolution) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, complaintId);
            stmt.setString(2, topic);
            stmt.setString(3, resolution);
            stmt.executeUpdate();

            // Update complaint status
            sql = "UPDATE complaints SET status = 'Resolved' WHERE complaint_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, complaintId);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Solution submitted successfully!");
            dispose();
            
            // Refresh parent frame if it's the TechnicianDashboard
            if (parentFrame != null && parentFrame.getClass().getSimpleName().equals("TechnicianDashboard")) {
                try {
                    parentFrame.getClass().getMethod("refreshTable").invoke(parentFrame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error submitting solution: " + e.getMessage());
        }
    }
}