package com.unifix.auth;

import com.unifix.dashboard.*;
import com.unifix.database.DBConnection;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.sql.*;
import javax.swing.*;

public class LoginPage extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton, signupRedirect;
    private BufferedImage backgroundImage;
    
    // Custom panel class for background
    class BackgroundPanel extends JPanel {
        private BufferedImage backgroundImage;
        
        public BackgroundPanel(BufferedImage backgroundImage) {
            this.backgroundImage = backgroundImage;
            setOpaque(false); // Make the panel non-opaque
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (backgroundImage != null) {
                // Draw the pre-rendered background image
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Fallback gradient if the background is null
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(42, 72, 110), 
                    getWidth(), getHeight(), new Color(28, 34, 87)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
            g2d.dispose();
        }
    }

    public LoginPage() {
        setTitle("UniFix - Login");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create a gradient background
        System.out.println("Creating blank gradient background");
        createGradientBackground();
        
        // Create background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);
        
        // Create a semi-transparent panel for login controls with enhanced mirror effect
        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (g instanceof Graphics2D g2d) {
                    // Set rendering hints for better quality
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                    
                    int width = getWidth();
                    int height = getHeight();
                    
                    // Create a multi-layer glass effect
                    
                    // Layer 1: Base transparent layer with blur effect
                    AlphaComposite alphaBase = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
                    g2d.setComposite(alphaBase);
                    g2d.setColor(new Color(230, 240, 255));
                    g2d.fillRoundRect(0, 0, width, height, 20, 20);
                    
                    // Layer 2: Reflective gradient for mirror-like effect
                    AlphaComposite alphaGrad = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
                    g2d.setComposite(alphaGrad);
                    GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 100),
                        0, height, new Color(200, 200, 255, 40)
                    );
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, width, height, 20, 20);
                    
                    // Layer 3: Highlight at the top for glass effect (light reflection)
                    AlphaComposite alphaHighlight = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                    g2d.setComposite(alphaHighlight);
                    g2d.setColor(new Color(255, 255, 255));
                    g2d.fillRoundRect(5, 5, width-10, height/4, 15, 15);
                    
                    // Layer 4: Add subtle edge highlights
                    AlphaComposite alphaBorder = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
                    g2d.setComposite(alphaBorder);
                    g2d.setColor(new Color(255, 255, 255));
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRoundRect(0, 0, width-1, height-1, 20, 20);
                    
                    // Reset composite to normal for any child components
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
            }
        };
        
        loginPanel.setLayout(new GridLayout(5, 1, 10, 10));
        loginPanel.setOpaque(false); // Make it non-opaque to see background
        loginPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        loginPanel.setPreferredSize(new Dimension(380, 320));

        // Create a fancy title with shadow effect
        JLabel title = new JLabel("Welcome to UniFix", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                
                // Draw text shadow
                g2d.setFont(getFont());
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.drawString(getText(), 3, 26);
                
                // Draw main text
                g2d.setColor(getForeground());
                g2d.drawString(getText(), 1, 24);
                g2d.dispose();
            }
        };
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(240, 240, 255));
        
        // Create stylish text fields
        emailField = new JTextField();
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 255, 120), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 255, 120), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        // Create gradient buttons
        loginButton = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create button gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(30, 120, 220),
                    0, getHeight(), new Color(0, 80, 160)
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Add highlight
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight()/2, 10, 10);
                
                // Draw text
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(getForeground());
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setPreferredSize(new Dimension(100, 36));
        
        signupRedirect = new JButton("Create New Account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create button gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 153, 0),
                    0, getHeight(), new Color(220, 120, 0)
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Add highlight
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight()/2, 10, 10);
                
                // Draw text
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(getForeground());
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }
        };
        signupRedirect.setFont(new Font("SansSerif", Font.BOLD, 14));
        signupRedirect.setForeground(Color.WHITE);
        signupRedirect.setFocusPainted(false);
        signupRedirect.setBorderPainted(false);
        signupRedirect.setContentAreaFilled(false);
        signupRedirect.setPreferredSize(new Dimension(100, 36));

        loginPanel.add(title);
        loginPanel.add(labeled("Email", emailField));
        loginPanel.add(labeled("Password", passwordField));
        loginPanel.add(loginButton);
        loginPanel.add(signupRedirect);
        
        // Add login panel to background panel
        backgroundPanel.add(loginPanel);

        loginButton.addActionListener(e -> authenticate());
        signupRedirect.addActionListener(e -> {
            dispose(); // Close current window first
            SwingUtilities.invokeLater(() -> new SignupPage()); // Open signup page
        });

        setVisible(true);
    }

    private JPanel labeled(String text, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        
        // Create a label with shadow effect for better visibility
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw text shadow for better readability
                g2d.setFont(getFont());
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(getText(), 1, 14);
                
                // Draw main text
                g2d.setColor(getForeground());
                g2d.drawString(getText(), 0, 13);
                g2d.dispose();
            }
        };
        
        // Style the label
        label.setForeground(new Color(255, 255, 255));
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Make panel transparent and add padding
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        
        // Add components to panel
        p.add(label, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void authenticate() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE email=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("name");
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login Successful as " + role);

                switch (role) {
                    case "Student": 
                        StudentDashboard studentDash = new StudentDashboard();
                        studentDash.setUserInfo(userId, userName);
                        break;
                    case "Warden": 
                        WardenDashboard wardenDash = new WardenDashboard();
                        wardenDash.setUserInfo(userId, userName);
                        break;
                    case "Technician": 
                        TechnicianDashboard techDash = new TechnicianDashboard();
                        techDash.setUserInfo(userId, userName);
                        break;
                    case "Admin": 
                        AdminDashboard adminDash = new AdminDashboard();
                        adminDash.setUserInfo(userId, userName);
                        break;
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }
    
    /**
     * Creates a beautiful gradient background 
     */
    private void createGradientBackground() {
        try {
            // Create a blank canvas for our background
            backgroundImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = backgroundImage.createGraphics();
            
            // Enable anti-aliasing for smoother shapes
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Create a professional blue-purple gradient background
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(42, 72, 110), 
                800, 600, new Color(28, 34, 87)
            );
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, 800, 600);
            
            // Add some subtle graphical elements to create depth and interest
            
            // Large subtle highlight in corner
            RadialGradientPaint rgp = new RadialGradientPaint(
                new Point(100, 100), 400,
                new float[] {0.0f, 0.7f, 1.0f},
                new Color[] {
                    new Color(100, 140, 180, 40),
                    new Color(70, 100, 150, 10),
                    new Color(40, 60, 100, 0)
                }
            );
            g2d.setPaint(rgp);
            g2d.fillRect(0, 0, 800, 600);
            
            // Add some soft circular highlights
            g2d.setColor(new Color(255, 255, 255, 10));
            for (int i = 0; i < 5; i++) {
                int size = 150 + i * 70;
                g2d.fillOval(650 - size/2, 150 - size/3, size, size);
            }
            
            // Add some abstract design elements
            g2d.setColor(new Color(255, 255, 255, 15));
            g2d.setStroke(new BasicStroke(3f));
            for (int i = 0; i < 4; i++) {
                int offset = i * 50;
                g2d.drawRoundRect(50 + offset, 300 + offset, 200, 200, 40, 40);
            }
            
            // Add subtle line pattern at bottom
            g2d.setColor(new Color(100, 150, 200, 20));
            g2d.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 15; i++) {
                int y = 500 + i * 6;
                g2d.drawLine(0, y, 800, y - 50);
            }
            
            g2d.dispose();
            System.out.println("Created gradient background: 800x600");
        } catch (Exception e) {
            System.out.println("Failed to create gradient background: " + e.getMessage());
            backgroundImage = null;
        }
    }
}