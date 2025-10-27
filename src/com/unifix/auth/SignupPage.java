package com.unifix.auth;

import com.unifix.database.DBConnection;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SignupPage extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton signupButton, loginRedirect;
    private BufferedImage backgroundImage;
    
    // Custom panel class for background image
    class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        
        public BackgroundPanel(BufferedImage backgroundImage) {
            this.backgroundImage = backgroundImage;
            setOpaque(false); // Make the panel non-opaque
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // If we have a background image, draw it
            if (backgroundImage != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Enable better quality rendering
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw the image scaled to fit the panel
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                
                // Add a semi-transparent overlay to improve readability
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        }
    }

    public SignupPage() {
        setTitle("UniFix - Signup");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Load background image
        try {
            File imageFile = new File("resources/campus.jpg");
            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
                if (backgroundImage != null) {
                    System.out.println("Background image loaded successfully: " + 
                                       backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
                } else {
                    System.out.println("Failed to read image file (null returned by ImageIO)");
                    createFallbackBackground();
                }
            } else {
                System.out.println("Background image file not found");
                createFallbackBackground();
            }
        } catch (IOException e) {
            System.out.println("Could not load background image: " + e.getMessage());
            createFallbackBackground();
        }
        
        // Create background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);
        
        // Create a semi-transparent panel for signup controls with mirror effect
        JPanel signupPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (g instanceof Graphics2D g2d) {
                    // Set rendering hints for better quality
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    
                    // Create a reflective gradient for mirror-like effect
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 80),
                        0, getHeight(), new Color(200, 200, 255, 60)
                    );
                    
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    
                    // Add a slight highlight at the top for glass effect
                    g2d.setColor(new Color(255, 255, 255, 50));
                    g2d.fillRoundRect(0, 0, getWidth(), 20, 10, 10);
                    
                    // Add a subtle border
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                }
            }
        };
        
        signupPanel.setLayout(new GridLayout(7, 1, 10, 10));
        signupPanel.setOpaque(false); // Make it non-opaque to see background
        signupPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        signupPanel.setPreferredSize(new Dimension(350, 400));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        nameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        roleBox = new JComboBox<>(new String[]{"Student", "Warden", "Technician", "Admin"});
        signupButton = new JButton("Signup");
        loginRedirect = new JButton("Already have an account? Login");
        
        // Style the components for better visibility
        signupButton.setBackground(new Color(0, 153, 51));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        
        loginRedirect.setBackground(new Color(0, 102, 204));
        loginRedirect.setForeground(Color.WHITE);
        loginRedirect.setFocusPainted(false);

        signupPanel.add(title);
        signupPanel.add(labeled("Full Name", nameField));
        signupPanel.add(labeled("Email", emailField));
        signupPanel.add(labeled("Password", passwordField));
        signupPanel.add(labeled("Role", roleBox));
        signupPanel.add(signupButton);
        signupPanel.add(loginRedirect);
        
        // Add signup panel to background panel
        backgroundPanel.add(signupPanel);

        signupButton.addActionListener(e -> registerUser());
        loginRedirect.addActionListener(e -> {
            dispose(); // Close the signup page first
            goToLoginPage(); // Navigate to login page
        });

        setVisible(true);
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        p.setOpaque(false); // Make it transparent
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void registerUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String role = roleBox.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Signup Successful! Please Login.");
            dispose(); // Close the signup page
            goToLoginPage(); // Navigate to login page
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    /**
     * Helper method to navigate to the login page
     */
    private void goToLoginPage() {
        EventQueue.invokeLater(() -> new LoginPage());
    }
    
    /**
     * Creates a fallback gradient background when image loading fails
     */
    private void createFallbackBackground() {
        try {
            // Create a gradient background as a fallback
            backgroundImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = backgroundImage.createGraphics();
            
            // Create a blue gradient background
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 102, 204).darker(), 
                800, 600, new Color(20, 20, 50)
            );
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, 800, 600);
            
            // Add some visual elements to make it look nice
            g2d.setColor(new Color(255, 255, 255, 30));
            for (int i = 0; i < 5; i++) {
                int size = 100 + i * 50;
                g2d.fillOval(400 - size/2, 300 - size/2, size, size);
            }
            
            g2d.dispose();
            System.out.println("Created fallback gradient background: 800x600");
        } catch (Exception e) {
            System.out.println("Failed to create fallback background: " + e.getMessage());
            backgroundImage = null;
        }
    }
}