package com.unifix.dashboard;

import com.unifix.complaints.ComplaintForm;
import com.unifix.complaints.ComplaintTable;
import com.unifix.database.DBConnection;
import com.unifix.utils.Location;
import java.awt.*;
import java.awt.event.*;
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
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class StudentDashboard extends JFrame {
    // Main components
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JPanel homePanel;
    private JPanel complaintsPanel;
    private JPanel solutionsPanel;
    private JPanel profilePanel;
    private JPanel complaintFormPanel;
    private JPanel complaintTablePanel;
    private JPanel complaintDetailPanel;
    private int userId;
    private String userName;
    
    // File upload components
    private File selectedImage = null;
    private JLabel imageNameLabel;
    
    // Navigation elements
    private JButton homeButton, complaintsButton, solutionsButton;
    private JButton newComplaintBtn, viewComplaintsBtn, logoutBtn, backButton;
    private JButton profileButton;
    
    // Dashboard summary components
    private JLabel pendingCountLabel;
    private JLabel needClarificationCountLabel;
    private JLabel awaitingApprovalCountLabel;

    public StudentDashboard() {
        setTitle("UniFix ServiceDesk - Student Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Customize button UI
            UIManager.put("Button.background", Color.WHITE);
            UIManager.put("Button.foreground", new Color(50, 50, 70));
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main container
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Create the top navigation bar (similar to SRMIST ServiceDesk)
        createTopNavBar();
        
        // Create the main content area with sidebar and content panel
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(new Color(240, 240, 245)); // Light gray background
        
        // Create sidebar panel (left side)
        createSidebarPanel();
        
        // Create main content panel (right side)
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(new Color(240, 240, 245));
        
        // Create various content panels
        homePanel = createHomePanel();
        complaintsPanel = createComplaintsPanel();
        solutionsPanel = createSolutionsPanel();
        profilePanel = createProfilePanel();
        complaintFormPanel = createComplaintFormPanel();
        complaintTablePanel = createComplaintTablePanel();
        complaintDetailPanel = createComplaintDetailPanel();
        
        // Add all panels to the card layout
        contentPanel.add(homePanel, "home");
        contentPanel.add(complaintsPanel, "complaints");
        contentPanel.add(solutionsPanel, "solutions");
        contentPanel.add(profilePanel, "profile");
        contentPanel.add(complaintFormPanel, "complaintForm");
        contentPanel.add(complaintTablePanel, "complaintTable");
        contentPanel.add(complaintDetailPanel, "complaintDetail");
        
        // Set home panel as default
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "home");
        
        // Add sidebar and content to body panel
        bodyPanel.add(sidebarPanel, BorderLayout.WEST);
        bodyPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add components to main panel
        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        
        // Add action listeners for navigation and actions
        setupActionListeners();
        
        // Add main panel to frame
        setContentPane(mainPanel);
        setVisible(true);
    }
    
    private void createTopNavBar() {
        // Top navigation bar with dark background (like SRMIST)
        JPanel navBar = new JPanel();
        navBar.setLayout(new BorderLayout());
        navBar.setBackground(new Color(40, 50, 70)); // Dark navy blue
        navBar.setPreferredSize(new Dimension(getWidth(), 40));
        
        // Logo and brand on left
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        brandPanel.setBackground(navBar.getBackground());
        
        JLabel brandLabel = new JLabel("UNIFIX");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandLabel.setForeground(Color.WHITE);
        
        JLabel serviceLabel = new JLabel("ServiceDesk");
        serviceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        serviceLabel.setForeground(Color.WHITE);
        
        brandPanel.add(brandLabel);
        brandPanel.add(serviceLabel);
        
        // Navigation buttons
        JPanel navButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navButtonsPanel.setBackground(navBar.getBackground());
        
        homeButton = createNavButton("Home");
        complaintsButton = createNavButton("Complaints");
        solutionsButton = createNavButton("Solutions");
        
        // Set Home as selected by default
        homeButton.setBackground(new Color(0, 120, 215));
        homeButton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 70, 90)));
        
        navButtonsPanel.add(homeButton);
        navButtonsPanel.add(complaintsButton);
        navButtonsPanel.add(solutionsButton);
        
        // User icons on the right
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        userPanel.setBackground(navBar.getBackground());
        
        // Add user info display to the right corner
        JPanel userInfoDisplay = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        userInfoDisplay.setBackground(navBar.getBackground());
        
        // User name label
        JLabel userNameLabel = new JLabel("Welcome, Guest");
        userNameLabel.setName("topUserNameLabel"); // for updating later
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userNameLabel.setForeground(Color.WHITE);
        
        // Profile logo/icon
        JLabel profileLogoLabel = new JLabel();
        try {
            ImageIcon profileIcon = new ImageIcon("resources/icons/profile_icon.svg");
            if (profileIcon.getIconWidth() > 0) {
                Image img = profileIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                profileLogoLabel.setIcon(new ImageIcon(img));
            } else {
                // Text fallback if image not found
                profileLogoLabel.setText("👤");
                profileLogoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
                profileLogoLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            // Text fallback if image not found
            profileLogoLabel.setText("👤");
            profileLogoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
            profileLogoLabel.setForeground(Color.WHITE);
        }
        
        // Make the profile logo clickable to go to profile
        profileLogoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileLogoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showProfilePanel();
            }
        });
        
        // Add name and icon to user info display
        userInfoDisplay.add(userNameLabel);
        userInfoDisplay.add(profileLogoLabel);
        
        // Add icons similar to those in SRMIST ServiceDesk
        JButton searchButton = createIconButton(new ImageIcon("resources/icons/search_icon.svg"), "Search");
        JButton notificationButton = createIconButton(new ImageIcon("resources/icons/notification_icon.svg"), "Notifications");
        profileButton = createIconButton(new ImageIcon("resources/icons/profile_icon.svg"), "Profile");
        
        // Add back button for navigation
        backButton = new JButton("← Back");
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setVisible(false); // Initially hidden
        
        // If icons are not available, create text-based buttons
        if (searchButton.getIcon() == null) {
            searchButton = createTextIconButton("🔍");
            notificationButton = createTextIconButton("🔔");
            profileButton = createTextIconButton("👤");
        }
        
        // Add action listeners for icon buttons
        searchButton.addActionListener(e -> showSearchDialog());
        notificationButton.addActionListener(e -> showNotificationsPanel());
        profileButton.addActionListener(e -> showProfilePanel());
        backButton.addActionListener(e -> goBack());
        
        userPanel.add(backButton);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(searchButton);
        userPanel.add(notificationButton);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(userInfoDisplay);
        
        // Add components to navbar
        navBar.add(brandPanel, BorderLayout.WEST);
        navBar.add(navButtonsPanel, BorderLayout.CENTER);
        navBar.add(userPanel, BorderLayout.EAST);
        
        mainPanel.add(navBar, BorderLayout.NORTH);
    }
    
    private void createSidebarPanel() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        sidebarPanel.setPreferredSize(new Dimension(350, getHeight()));
        
        // Create complaint summary panel (like in SRMIST ServiceDesk)
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel summaryTitle = new JLabel("My Complaint Summary");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Need Clarification status panel
        JPanel needClarificationPanel = createStatusPanel("Need Clarification", "0");
        needClarificationCountLabel = (JLabel) needClarificationPanel.getComponent(1);
        
        // Pending status panel
        JPanel pendingPanel = createStatusPanel("Pending", "0");
        pendingCountLabel = (JLabel) pendingPanel.getComponent(1);
        
        // Awaiting Approval status panel
        JPanel awaitingApprovalPanel = createStatusPanel("Awaiting Approval", "0");
        awaitingApprovalCountLabel = (JLabel) awaitingApprovalPanel.getComponent(1);
        
        // Add components to summary panel
        summaryPanel.add(summaryTitle);
        summaryPanel.add(Box.createVerticalStrut(20));
        summaryPanel.add(needClarificationPanel);
        summaryPanel.add(Box.createVerticalStrut(10));
        summaryPanel.add(pendingPanel);
        summaryPanel.add(Box.createVerticalStrut(10));
        summaryPanel.add(awaitingApprovalPanel);
        
        // Create announcements panel
        JPanel announcementsPanel = createAnnouncementsPanel();
        
        // Add components to sidebar
        sidebarPanel.add(summaryPanel);
        sidebarPanel.add(new JSeparator());
        sidebarPanel.add(announcementsPanel);
    }
    
    private JPanel createHomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245)); // Light gray background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Welcome panel with user info
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        JPanel welcomeContent = new JPanel();
        welcomeContent.setLayout(new BoxLayout(welcomeContent, BoxLayout.Y_AXIS));
        welcomeContent.setBackground(Color.WHITE);
        welcomeContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel welcomeTitle = new JLabel("Welcome to UniFix ServiceDesk");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel welcomeSubtitle = new JLabel("Your campus maintenance solution");
        welcomeSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeSubtitle.setForeground(new Color(100, 100, 100));
        welcomeSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(600, 120));
        
        // Create stat boxes with fixed preferred widths to ensure text visibility
        JPanel pendingBox = createStatBox("Pending", "0", new Color(0, 120, 215));
        pendingBox.setPreferredSize(new Dimension(150, 100));  // Set preferred width
        
        JPanel resolvedBox = createStatBox("Resolved", "0", new Color(0, 150, 136));
        resolvedBox.setPreferredSize(new Dimension(150, 100));  // Set preferred width
        
        JPanel totalBox = createStatBox("Total", "0", new Color(156, 39, 176));
        totalBox.setPreferredSize(new Dimension(150, 100));  // Set preferred width
        
        statsPanel.add(pendingBox);
        statsPanel.add(resolvedBox);
        statsPanel.add(totalBox);
        
        welcomeContent.add(welcomeTitle);
        welcomeContent.add(Box.createVerticalStrut(10));
        welcomeContent.add(welcomeSubtitle);
        welcomeContent.add(statsPanel);
        
        welcomePanel.add(welcomeContent, BorderLayout.CENTER);
        
        // Quick actions panel
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionsPanel.setBackground(panel.getBackground());
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        newComplaintBtn = createActionButton("New Complaint", new Color(0, 120, 215));
        viewComplaintsBtn = createActionButton("View My Complaints", new Color(0, 150, 136));
        logoutBtn = createActionButton("Logout", new Color(211, 47, 47));
        
        actionsPanel.add(newComplaintBtn);
        actionsPanel.add(viewComplaintsBtn);
        actionsPanel.add(logoutBtn);
        
        // Add components to panel
        panel.add(welcomePanel, BorderLayout.CENTER);
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createComplaintsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Complaints header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel headerTitle = new JLabel("My Complaints");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton newComplaintButton = createActionButton("+ New Complaint", new Color(0, 120, 215));
        newComplaintButton.addActionListener(e -> openComplaintForm());
        
        headerPanel.add(headerTitle, BorderLayout.WEST);
        headerPanel.add(newComplaintButton, BorderLayout.EAST);
        
        // Complaint list content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel noComplaintsLabel = new JLabel("You haven't submitted any complaints yet");
        noComplaintsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noComplaintsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        noComplaintsLabel.setForeground(new Color(100, 100, 100));
        
        JButton createFirstComplaintButton = createActionButton("Create Your First Complaint", new Color(0, 120, 215));
        createFirstComplaintButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createFirstComplaintButton.addActionListener(e -> openComplaintForm());
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(noComplaintsLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(createFirstComplaintButton);
        centerPanel.add(Box.createVerticalStrut(40));
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Add components to panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Add click listener to redirect to the full complaint table view
        createFirstComplaintButton.addActionListener(e -> {
            viewComplaints(); // This loads and shows the complaint table
        });
        
        return panel;
    }
    
    private JPanel createSolutionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245)); // Light gray background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create popular solutions panel (like in SRMIST ServiceDesk)
        JPanel solutionsPanelInner = new JPanel();
        solutionsPanelInner.setLayout(new BorderLayout());
        solutionsPanelInner.setBackground(Color.WHITE);
        solutionsPanelInner.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        // Solutions header
        JPanel solutionsHeader = new JPanel(new BorderLayout());
        solutionsHeader.setBackground(Color.WHITE);
        solutionsHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel solutionsTitle = new JLabel("Popular Solutions");
        solutionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel moreLink = new JLabel("[ More ]");
        moreLink.setForeground(new Color(0, 120, 215));
        moreLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        solutionsHeader.add(solutionsTitle, BorderLayout.WEST);
        solutionsHeader.add(moreLink, BorderLayout.EAST);
        
        // Solutions search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        
        JTextField searchField = new JTextField();
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search Solution");
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        // Solutions content
        JPanel solutionsContent = new JPanel(new BorderLayout());
        solutionsContent.setBackground(Color.WHITE);
        solutionsContent.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        JLabel noSolutionsLabel = new JLabel("No solutions available");
        noSolutionsLabel.setForeground(new Color(100, 100, 100));
        noSolutionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noSolutionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        solutionsContent.add(noSolutionsLabel, BorderLayout.CENTER);
        
        // Add components to solutions panel
        solutionsPanelInner.add(solutionsHeader, BorderLayout.NORTH);
        solutionsPanelInner.add(searchPanel, BorderLayout.CENTER);
        solutionsPanelInner.add(solutionsContent, BorderLayout.SOUTH);
        
        panel.add(solutionsPanelInner, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Profile header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // User info panel
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.X_AXIS));
        userInfoPanel.setBackground(Color.WHITE);
        
        // Profile picture
        JLabel profilePic = new JLabel();
        profilePic.setText("👤");
        profilePic.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        profilePic.setPreferredSize(new Dimension(100, 100));
        profilePic.setHorizontalAlignment(SwingConstants.CENTER);
        
        // User details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        detailsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("Student Name");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel emailLabel = new JLabel("student@example.com");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        emailLabel.setForeground(new Color(100, 100, 100));
        
        JLabel roleLabel = new JLabel("Student");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        roleLabel.setForeground(new Color(0, 120, 215));
        
        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(emailLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(roleLabel);
        
        userInfoPanel.add(profilePic);
        userInfoPanel.add(detailsPanel);
        
        headerPanel.add(userInfoPanel, BorderLayout.NORTH);
        
        // Profile content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // User info section
        JPanel userInfoEditPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        userInfoEditPanel.setBackground(Color.WHITE);
        userInfoEditPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            "Profile Information",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        // Register Number field
        JPanel registerPanel = new JPanel(new BorderLayout(5, 0));
        registerPanel.setBackground(Color.WHITE);
        JLabel registerLabel = new JLabel("Register Number:");
        registerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField registerField = new JTextField();
        registerField.setName("registerField");
        registerPanel.add(registerLabel, BorderLayout.NORTH);
        registerPanel.add(registerField, BorderLayout.CENTER);
        
        // Address field
        JPanel addressPanel = new JPanel(new BorderLayout(5, 0));
        addressPanel.setBackground(Color.WHITE);
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField addressField = new JTextField();
        addressField.setName("addressField");
        addressPanel.add(addressLabel, BorderLayout.NORTH);
        addressPanel.add(addressField, BorderLayout.CENTER);
        
        // Phone field
        JPanel phonePanel = new JPanel(new BorderLayout(5, 0));
        phonePanel.setBackground(Color.WHITE);
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField phoneField = new JTextField();
        phoneField.setName("phoneField");
        phonePanel.add(phoneLabel, BorderLayout.NORTH);
        phonePanel.add(phoneField, BorderLayout.CENTER);
        
        // Save button
        JButton saveProfileButton = createActionButton("Save Profile", new Color(0, 150, 136));
        saveProfileButton.addActionListener(e -> saveProfileInfo(
            registerField.getText().trim(),
            addressField.getText().trim(),
            phoneField.getText().trim()
        ));
        
        userInfoEditPanel.add(registerPanel);
        userInfoEditPanel.add(addressPanel);
        userInfoEditPanel.add(phonePanel);
        userInfoEditPanel.add(saveProfileButton);
        
        // Account settings section
        JLabel settingsLabel = new JLabel("Account Settings");
        settingsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        settingsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton passwordButton = createSettingsButton("Change Password");
        JButton notificationsButton = createSettingsButton("Notification Preferences");
        JButton logoutButton = createActionButton("Logout", new Color(211, 47, 47));
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(200, 40));
        logoutButton.addActionListener(e -> logout());
        
        contentPanel.add(userInfoEditPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(settingsLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(passwordButton);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(notificationsButton);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(logoutButton);
        
        // Add a scroll pane to handle overflow
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        
        headerPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(40, 50, 70)); // Same as navbar
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Set wider buttons to accommodate text completely
        int width = text.equals("Complaints") ? 135 : (text.equals("Solutions") ? 120 : 100);
        button.setPreferredSize(new Dimension(width, 40));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getBackground().equals(new Color(0, 120, 215))) {
                    button.setBackground(new Color(60, 70, 90));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.getBackground().equals(new Color(0, 120, 215))) {
                    button.setBackground(new Color(40, 50, 70));
                }
            }
        });
        
        return button;
    }
    
    private JButton createIconButton(ImageIcon icon, String tooltip) {
        JButton button = new JButton();
        if (icon != null) {
            // Scale icon to appropriate size
            Image img = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        }
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setForeground(Color.WHITE);
        
        return button;
    }
    
    private JButton createTextIconButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private JPanel createStatusPanel(String statusName, String count) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(statusName);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(80, 80, 80));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel countLabel = new JLabel(count);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        countLabel.setForeground(new Color(50, 50, 50));
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(nameLabel);
        panel.add(countLabel);
        
        return panel;
    }
    
    private JPanel createAnnouncementsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header with title and "Show All" link
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        
        // Create icon for announcements (similar to the megaphone in SRMIST)
        JLabel iconLabel = new JLabel("📢");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JLabel titleLabel = new JLabel("Announcements");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        JLabel showAllLink = new JLabel("Show All");
        showAllLink.setForeground(new Color(0, 120, 215));
        showAllLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showAllLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(showAllLink, BorderLayout.EAST);
        
        // Content panel with "no announcements" message
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Create megaphone icon similar to SRMIST ServiceDesk
        JLabel megaphoneIconLabel = new JLabel();
        try {
            ImageIcon megaphoneIcon = new ImageIcon("resources/megaphone_icon.png");
            if (megaphoneIcon.getIconWidth() > 0) {
                Image img = megaphoneIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                megaphoneIconLabel.setIcon(new ImageIcon(img));
            } else {
                // Text fallback if image not found
                megaphoneIconLabel.setText("📣");
                megaphoneIconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
            }
        } catch (Exception e) {
            // Text fallback if image not found
            megaphoneIconLabel.setText("📣");
            megaphoneIconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        }
        megaphoneIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel noAnnouncementsLabel = new JLabel("There are no new announcements today.");
        noAnnouncementsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noAnnouncementsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noAnnouncementsLabel.setForeground(new Color(100, 100, 100));
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        
        megaphoneIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noAnnouncementsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(megaphoneIconLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(noAnnouncementsLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Add all components to main panel
        panel.add(headerPanel);
        panel.add(contentPanel);
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Increase padding for buttons to ensure text is fully visible
        int horizontalPadding = Math.max(25, text.length() * 3);
        button.setBorder(BorderFactory.createEmptyBorder(10, horizontalPadding, 10, horizontalPadding));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    // Helper method to create a stat box for the dashboard
    private JPanel createStatBox(String label, String value, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 3, color),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(80, 80, 80));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Ensure minimum width to display text fully
        int minWidth = Math.max(80, label.length() * 10);
        panel.setMinimumSize(new Dimension(minWidth, 80));
        
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(nameLabel);
        
        return panel;
    }
    
    // Helper method to create settings buttons
    private JButton createSettingsButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(50, 50, 50));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(0, 120, 215));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(new Color(50, 50, 50));
            }
        });
        
        return button;
    }
    
    // Method to show search dialog
    private void showSearchDialog() {
        JDialog searchDialog = new JDialog(this, "Search", true);
        searchDialog.setSize(400, 100);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setLayout(new BorderLayout(10, 10));
        
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        
        searchDialog.add(new JLabel(" Enter search term:"), BorderLayout.NORTH);
        searchDialog.add(searchField, BorderLayout.CENTER);
        searchDialog.add(searchButton, BorderLayout.EAST);
        
        searchButton.addActionListener(e -> searchDialog.dispose());
        
        searchDialog.setVisible(true);
    }
    
    // Method to show notifications panel
    private void showNotificationsPanel() {
        // Show the back button when in a sub-panel
        backButton.setVisible(true);
        JOptionPane.showMessageDialog(this, "No new notifications", "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Method to show profile panel
    private void showProfilePanel() {
        // Switch to profile panel
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "profile");
        // Show the back button
        backButton.setVisible(true);
        // Update active button
        resetNavButtons();
    }
    
    // Method to go back to previous panel
    private void goBack() {
        // Return to home panel
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "home");
        // Hide back button
        backButton.setVisible(false);
        // Set home button as active
        setActiveNavButton(homeButton);
    }
    
    private void setupActionListeners() {
        // Navigation bar listeners
        homeButton.addActionListener(e -> {
            setActiveNavButton(homeButton);
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "home");
            backButton.setVisible(false);
        });
        
        complaintsButton.addActionListener(e -> {
            setActiveNavButton(complaintsButton);
            loadComplaintsData(); // Load latest complaint data
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "complaintTable");
            backButton.setVisible(true);
        });
        
        solutionsButton.addActionListener(e -> {
            setActiveNavButton(solutionsButton);
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "solutions");
            backButton.setVisible(false);
        });
        
        // Action button listeners
        newComplaintBtn.addActionListener(e -> openComplaintForm());
        viewComplaintsBtn.addActionListener(e -> viewComplaints());
        logoutBtn.addActionListener(e -> logout());
    }
    
    private void resetNavButtons() {
        homeButton.setBackground(new Color(40, 50, 70));
        complaintsButton.setBackground(new Color(40, 50, 70));
        solutionsButton.setBackground(new Color(40, 50, 70));
    }
    
    private void setActiveNavButton(JButton activeButton) {
        // Reset all buttons
        resetNavButtons();
        
        // Set active button
        activeButton.setBackground(new Color(0, 120, 215));
    }
    
    private void openComplaintForm() {
        if (userId > 0) {
            // Show the complaint form panel
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "complaintForm");
            // Show back button
            backButton.setVisible(true);
            // Reset nav buttons
            resetNavButtons();
            // Set active button if needed
            setActiveNavButton(complaintsButton);
        } else {
            JOptionPane.showMessageDialog(this, "Error: User ID not set!");
        }
    }

    private void viewComplaints() {
        if (userId > 0) {
            // Load the latest complaints data
            loadComplaintsData();
            // Show the complaints table panel
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "complaintTable");
            // Show back button
            backButton.setVisible(true);
            // Reset nav buttons
            resetNavButtons();
            // Set active button if needed
            setActiveNavButton(complaintsButton);
        } else {
            JOptionPane.showMessageDialog(this, "Error: User ID not set!");
        }
    }
    
    private void loadComplaintsData() {
        // This method loads complaint data into the table
        updateComplaintsTable();
    }
    
    private void updateComplaintsTable() {
        // Find the complaints table panel
        JPanel tablePanel = (JPanel) complaintTablePanel.getComponent(1);
        JScrollPane scrollPane = (JScrollPane) tablePanel.getComponent(0);
        JTable complaintsTable = (JTable) scrollPane.getViewport().getView();
        
        // Fetch data from database
        Object[][] data = fetchComplaints();
        
        // Create a new table model with the data
        DefaultTableModel model = new DefaultTableModel(data, new String[]{
            "ID", "Category", "Location", "Status", "Date"
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 4) return java.util.Date.class;
                return String.class;
            }
        };
        
        // Set the new model to the table
        complaintsTable.setModel(model);
        
        // Update the counts in the sidebar
        updateComplaintCounts();
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
            e.printStackTrace();
        }
        return data;
    }
    
    private void showComplaintDetails(int complaintId) {
        // Load complaint details into the detail panel
        loadComplaintDetails(complaintId);
        
        // Show the complaint detail panel
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "complaintDetail");
    }
    
    private void loadComplaintDetails(int complaintId) {
        // Get components from the detail panel
        JPanel detailContentPanel = (JPanel) ((JScrollPane) complaintDetailPanel.getComponent(1)).getViewport().getView();
        
        // Get info panel
        JPanel infoPanel = (JPanel) detailContentPanel.getComponent(0);
        
        // Find labeled components
        JLabel categoryValueLabel = findComponentByName(detailContentPanel, "categoryValue");
        JLabel locationValueLabel = findComponentByName(detailContentPanel, "locationValue");
        JLabel statusValueLabel = findComponentByName(detailContentPanel, "statusValue");
        JLabel dateValueLabel = findComponentByName(detailContentPanel, "dateValue");
        JTextArea descriptionArea = findComponentByName(detailContentPanel, "descriptionArea");
        JLabel imageDisplayLabel = findComponentByName(detailContentPanel, "imageDisplay");
        
        // Load data from database
        try (Connection conn = DBConnection.getConnection()) {
            // Check if image_path column exists
            boolean imagePathExists = checkImagePathColumnExists(conn);
            
            // Build query based on whether image_path column exists
            String sql;
            if (imagePathExists) {
                sql = "SELECT c.category, c.location, c.description, c.status, c.created_at, " +
                      "c.image_path FROM complaints c WHERE c.complaint_id = ?";
            } else {
                sql = "SELECT c.category, c.location, c.description, c.status, c.created_at " +
                      "FROM complaints c WHERE c.complaint_id = ?";
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, complaintId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Update labels with data
                        categoryValueLabel.setText(rs.getString("category"));
                        locationValueLabel.setText(rs.getString("location"));
                        statusValueLabel.setText(rs.getString("status"));
                        dateValueLabel.setText(rs.getTimestamp("created_at").toString());
                        descriptionArea.setText(rs.getString("description"));
                        
                        // Handle image if exists and column is available
                        String imagePath = null;
                        if (imagePathExists) {
                            try {
                                imagePath = rs.getString("image_path");
                            } catch (SQLException e) {
                                System.out.println("Image path column not found: " + e.getMessage());
                            }
                        }
                        
                        if (imagePath != null && !imagePath.isEmpty()) {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
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
                                imageDisplayLabel.setIcon(new ImageIcon(scaledImage));
                                imageDisplayLabel.setText("");
                            } else {
                                imageDisplayLabel.setIcon(null);
                                imageDisplayLabel.setText("Image not found: " + imagePath);
                            }
                        } else {
                            imageDisplayLabel.setIcon(null);
                            imageDisplayLabel.setText("No image attached");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading complaint details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to find a component by name
    private <T extends Component> T findComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return (T) comp;
            }
            
            if (comp instanceof Container) {
                T found = findComponentByName((Container) comp, name);
                if (found != null) {
                    return found;
                }
            }
            
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof Container) {
                    T found = findComponentByName((Container) view, name);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }
    
    private void submitComplaint(Location location, String category, String description) {
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "Error: Invalid user ID!");
            return;
        }

        if (description == null || description.trim().isEmpty()) {
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
            
            // Reset the form
            selectedImage = null;
            imageNameLabel.setText("No file selected");
            
            // Update complaint counts and show complaints table with the new submission
            updateComplaintCounts();
            loadComplaintsData(); // Load the updated complaints data
            ((CardLayout) contentPanel.getLayout()).show(contentPanel, "complaintTable");
            backButton.setVisible(true);
            setActiveNavButton(complaintsButton);
            
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
    
    private void updateComplaintCounts() {
        try (Connection conn = DBConnection.getConnection()) {
            // Count pending complaints
            String pendingSql = "SELECT COUNT(*) FROM complaints WHERE user_id = ? AND status = 'Pending'";
            try (PreparedStatement stmt = conn.prepareStatement(pendingSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int pendingCount = rs.getInt(1);
                        pendingCountLabel.setText(String.valueOf(pendingCount));
                        
                        // Also update the home page stats
                        JPanel homeContent = (JPanel) homePanel.getComponent(0);
                        JPanel welcomeContent = (JPanel) homeContent.getComponent(0);
                        JPanel statsPanel = (JPanel) welcomeContent.getComponent(3);
                        
                        JPanel pendingBox = (JPanel) statsPanel.getComponent(0);
                        JLabel pendingValue = (JLabel) pendingBox.getComponent(0);
                        pendingValue.setText(String.valueOf(pendingCount));
                    }
                }
            }
            
            // Count resolved complaints
            String resolvedSql = "SELECT COUNT(*) FROM complaints WHERE user_id = ? AND status = 'Resolved'";
            try (PreparedStatement stmt = conn.prepareStatement(resolvedSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int resolvedCount = rs.getInt(1);
                        
                        // Update the home page stats
                        JPanel homeContent = (JPanel) homePanel.getComponent(0);
                        JPanel welcomeContent = (JPanel) homeContent.getComponent(0);
                        JPanel statsPanel = (JPanel) welcomeContent.getComponent(3);
                        
                        JPanel resolvedBox = (JPanel) statsPanel.getComponent(1);
                        JLabel resolvedValue = (JLabel) resolvedBox.getComponent(0);
                        resolvedValue.setText(String.valueOf(resolvedCount));
                    }
                }
            }
            
            // Count in progress complaints (for need clarification in sidebar)
            String inProgressSql = "SELECT COUNT(*) FROM complaints WHERE user_id = ? AND status = 'In Progress'";
            try (PreparedStatement stmt = conn.prepareStatement(inProgressSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        needClarificationCountLabel.setText(String.valueOf(rs.getInt(1)));
                    }
                }
            }
            
            // Count total complaints
            String totalSql = "SELECT COUNT(*) FROM complaints WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(totalSql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int totalCount = rs.getInt(1);
                        
                        // Update awaiting approval in sidebar (using total count temporarily)
                        awaitingApprovalCountLabel.setText(String.valueOf(totalCount));
                        
                        // Update the home page stats
                        JPanel homeContent = (JPanel) homePanel.getComponent(0);
                        JPanel welcomeContent = (JPanel) homeContent.getComponent(0);
                        JPanel statsPanel = (JPanel) welcomeContent.getComponent(3);
                        
                        JPanel totalBox = (JPanel) statsPanel.getComponent(2);
                        JLabel totalValue = (JLabel) totalBox.getComponent(0);
                        totalValue.setText(String.valueOf(totalCount));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error updating complaint counts: " + e.getMessage());
            e.printStackTrace();
        }
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
            // Return to login page
            SwingUtilities.invokeLater(() -> {
                com.unifix.auth.LoginPage loginPage = new com.unifix.auth.LoginPage();
                loginPage.setVisible(true);
            });
        }
    }

    // Method to create complaint form panel
    private JPanel createComplaintFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel headerTitle = new JLabel("Submit New Complaint");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        headerPanel.add(headerTitle, BorderLayout.WEST);
        
        // Form content
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Create location dropdown
        JPanel locationPanel = new JPanel(new BorderLayout(5, 5));
        locationPanel.setBackground(Color.WHITE);
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JComboBox<Location> locationBox = new JComboBox<>(Location.values());
        locationBox.setBackground(Color.WHITE);
        locationBox.setPreferredSize(new Dimension(0, 35));
        locationPanel.add(locationLabel, BorderLayout.NORTH);
        locationPanel.add(locationBox, BorderLayout.CENTER);
        locationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        locationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Create category dropdown
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 5));
        categoryPanel.setBackground(Color.WHITE);
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String[] categories = {"Maintenance", "Security", "Cleanliness", "Infrastructure", "Other"};
        JComboBox<String> categoryBox = new JComboBox<>(categories);
        categoryBox.setBackground(Color.WHITE);
        categoryBox.setPreferredSize(new Dimension(0, 35));
        categoryPanel.add(categoryLabel, BorderLayout.NORTH);
        categoryPanel.add(categoryBox, BorderLayout.CENTER);
        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        categoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Create description area
        JPanel descriptionPanel = new JPanel(new BorderLayout(5, 5));
        descriptionPanel.setBackground(Color.WHITE);
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
        descriptionPanel.add(scrollPane, BorderLayout.CENTER);
        descriptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        // Image upload panel
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBackground(Color.WHITE);
        JLabel imageLabel = new JLabel("Attach Image (Optional):");
        imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel imageUploadPanel = new JPanel(new BorderLayout(10, 0));
        imageUploadPanel.setBackground(Color.WHITE);
        
        JButton uploadButton = createActionButton("Choose File", new Color(0, 120, 215));
        imageNameLabel = new JLabel("No file selected");
        imageNameLabel.setForeground(new Color(100, 100, 100));
        
        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                           name.endsWith(".png") || name.endsWith(".gif");
                }
                
                @Override
                public String getDescription() {
                    return "Image Files (*.jpg, *.jpeg, *.png, *.gif)";
                }
            });
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImage = fileChooser.getSelectedFile();
                imageNameLabel.setText(selectedImage.getName());
            }
        });
        
        imageUploadPanel.add(uploadButton, BorderLayout.WEST);
        imageUploadPanel.add(imageNameLabel, BorderLayout.CENTER);
        
        imagePanel.add(imageLabel, BorderLayout.NORTH);
        imagePanel.add(imageUploadPanel, BorderLayout.CENTER);
        imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        imagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Submit button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton submitButton = createActionButton("Submit", new Color(0, 150, 136));
        submitButton.addActionListener(e -> submitComplaint(
            (Location)locationBox.getSelectedItem(),
            (String)categoryBox.getSelectedItem(),
            descriptionArea.getText()
        ));
        
        buttonPanel.add(submitButton);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Add all components to form panel
        formPanel.add(locationPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(categoryPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(descriptionPanel);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(imagePanel);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(buttonPanel);
        
        // Add to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        
        return panel;
    }
    
    // Method to create complaint table panel
    private JPanel createComplaintTablePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Table header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel headerTitle = new JLabel("My Complaints");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton newComplaintBtn = createActionButton("+ New Complaint", new Color(0, 120, 215));
        newComplaintBtn.addActionListener(e -> openComplaintForm());
        
        headerPanel.add(headerTitle, BorderLayout.WEST);
        headerPanel.add(newComplaintBtn, BorderLayout.EAST);
        
        // Table content
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Create table with columns
        String[] columns = {"ID", "Category", "Location", "Status", "Date"};
        Object[][] data = new Object[0][5]; // Empty data initially
        
        JTable complaintsTable = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        complaintsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        complaintsTable.setRowHeight(30);
        complaintsTable.setGridColor(new Color(240, 240, 240));
        complaintsTable.getTableHeader().setReorderingAllowed(false);
        complaintsTable.getTableHeader().setBackground(new Color(0, 120, 215));
        complaintsTable.getTableHeader().setForeground(Color.WHITE);
        complaintsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Add mouse listener to show complaint details
        complaintsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = complaintsTable.getSelectedRow();
                if (row != -1) {
                    int complaintId = (Integer) complaintsTable.getValueAt(row, 0);
                    showComplaintDetails(complaintId);
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(complaintsTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Add components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Method to create complaint detail panel
    private JPanel createComplaintDetailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 240, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Detail header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel headerTitle = new JLabel("Complaint Details");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton backToListButton = createActionButton("Back to List", new Color(100, 100, 100));
        backToListButton.addActionListener(e -> viewComplaints());
        
        headerPanel.add(headerTitle, BorderLayout.WEST);
        headerPanel.add(backToListButton, BorderLayout.EAST);
        
        // Detail content
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Info panel with complaint details
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4, 2, 15, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Create labels for detail values
        JLabel categoryLabel = new JLabel("Category:");
        JLabel categoryValueLabel = new JLabel("");
        JLabel locationLabel = new JLabel("Location:");
        JLabel locationValueLabel = new JLabel("");
        JLabel statusLabel = new JLabel("Status:");
        JLabel statusValueLabel = new JLabel("");
        JLabel dateLabel = new JLabel("Submitted on:");
        JLabel dateValueLabel = new JLabel("");
        
        // Style the labels
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 14);
        
        categoryLabel.setFont(labelFont);
        categoryValueLabel.setFont(valueFont);
        locationLabel.setFont(labelFont);
        locationValueLabel.setFont(valueFont);
        statusLabel.setFont(labelFont);
        statusValueLabel.setFont(valueFont);
        dateLabel.setFont(labelFont);
        dateValueLabel.setFont(valueFont);
        
        // Add labels to info panel
        infoPanel.add(categoryLabel);
        infoPanel.add(categoryValueLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(locationValueLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(statusValueLabel);
        infoPanel.add(dateLabel);
        infoPanel.add(dateValueLabel);
        
        // Description panel
        JPanel descPanel = new JPanel(new BorderLayout(5, 5));
        descPanel.setBackground(Color.WHITE);
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Description",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(0, 150));
        descPanel.add(descScrollPane, BorderLayout.CENTER);
        
        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                "Attached Image",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel imageDisplayLabel = new JLabel("No image attached", JLabel.CENTER);
        imageDisplayLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        imageDisplayLabel.setForeground(new Color(100, 100, 100));
        imagePanel.add(imageDisplayLabel, BorderLayout.CENTER);
        
        // Add all components to detail panel with proper spacing
        detailPanel.add(infoPanel);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(descPanel);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(imagePanel);
        
        // Add scroll support
        JScrollPane detailScrollPane = new JScrollPane(detailPanel);
        detailScrollPane.setBorder(null);
        
        // Add components to main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(detailScrollPane, BorderLayout.CENTER);
        
        // Tag components with names for later retrieval when updating
        categoryValueLabel.setName("categoryValue");
        locationValueLabel.setName("locationValue");
        statusValueLabel.setName("statusValue");
        dateValueLabel.setName("dateValue");
        descriptionArea.setName("descriptionArea");
        imageDisplayLabel.setName("imageDisplay");
        
        return panel;
    }
    
    // Method to set user info when logging in
    public void setUserInfo(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        
        // Update the title with user name
        setTitle("UniFix ServiceDesk - " + userName);
        
        try {
            // Update user name in the top navigation bar
            updateTopNavUserName();
            
            // Update profile panel with user name
            updateProfilePanelUserName(userName);
            
            // Update user name in profile panel (safely with error checking)
            Component[] profileComponents = profilePanel.getComponents();
            if (profileComponents.length > 0 && profileComponents[0] instanceof JPanel) {
                JPanel profilePanelContent = (JPanel) profileComponents[0];
                Component[] contentComponents = profilePanelContent.getComponents();
                
                if (contentComponents.length > 0 && contentComponents[0] instanceof JPanel) {
                    JPanel userInfoContainer = (JPanel) contentComponents[0];
                    Component[] userComponents = userInfoContainer.getComponents();
                    
                    if (userComponents.length > 0 && userComponents[0] instanceof JPanel) {
                        JPanel userInfoPanel = (JPanel) userComponents[0];
                        Component[] infoComponents = userInfoPanel.getComponents();
                        
                        if (infoComponents.length > 1 && infoComponents[1] instanceof JPanel) {
                            JPanel detailsPanel = (JPanel) infoComponents[1];
                            Component[] detailComponents = detailsPanel.getComponents();
                            
                            if (detailComponents.length > 0 && detailComponents[0] instanceof JLabel) {
                                JLabel nameLabel = (JLabel) detailComponents[0];
                                nameLabel.setText(userName);
                            }
                        }
                    }
                }
            }
            
            // Update name in home panel (safely with error checking)
            Component[] homeComponents = homePanel.getComponents();
            if (homeComponents.length > 0 && homeComponents[0] instanceof JPanel) {
                JPanel homeContent = (JPanel) homeComponents[0];
                Component[] contentComponents = homeContent.getComponents();
                
                if (contentComponents.length > 0 && contentComponents[0] instanceof JPanel) {
                    JPanel welcomeContent = (JPanel) contentComponents[0];
                    Component[] welcomeComponents = welcomeContent.getComponents();
                    
                    if (welcomeComponents.length > 0 && welcomeComponents[0] instanceof JLabel) {
                        JLabel welcomeTitle = (JLabel) welcomeComponents[0];
                        welcomeTitle.setText("Welcome, " + userName);
                    }
                    
                    // Update stats on home page
                    if (welcomeComponents.length > 3 && welcomeComponents[3] instanceof JPanel) {
                        JPanel statsPanel = (JPanel) welcomeComponents[3];
                        Component[] statComponents = statsPanel.getComponents();
                        
                        if (statComponents.length > 0 && statComponents[0] instanceof JPanel) {
                            JPanel pendingBox = (JPanel) statComponents[0];
                            Component[] pendingComponents = pendingBox.getComponents();
                            
                            if (pendingComponents.length > 0 && pendingComponents[0] instanceof JLabel) {
                                JLabel pendingValue = (JLabel) pendingComponents[0];
                                pendingValue.setText("0");
                            }
                        }
                        
                        if (statComponents.length > 1 && statComponents[1] instanceof JPanel) {
                            JPanel resolvedBox = (JPanel) statComponents[1];
                            Component[] resolvedComponents = resolvedBox.getComponents();
                            
                            if (resolvedComponents.length > 0 && resolvedComponents[0] instanceof JLabel) {
                                JLabel resolvedValue = (JLabel) resolvedComponents[0];
                                resolvedValue.setText("0");
                            }
                        }
                        
                        if (statComponents.length > 2 && statComponents[2] instanceof JPanel) {
                            JPanel totalBox = (JPanel) statComponents[2];
                            Component[] totalComponents = totalBox.getComponents();
                            
                            if (totalComponents.length > 0 && totalComponents[0] instanceof JLabel) {
                                JLabel totalValue = (JLabel) totalComponents[0];
                                totalValue.setText("0");
                            }
                        }
                    }
                }
            }
            
            // Update complaint counts in sidebar
            needClarificationCountLabel.setText("0");
            pendingCountLabel.setText("0");
            awaitingApprovalCountLabel.setText("0");
            
            // Update the complaint counts from the database
            updateComplaintCounts();
            
            // Load user profile information
            loadProfileInfo();
            
        } catch (Exception e) {
            System.out.println("Error updating UI with user info: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the user name in the top navigation bar
     */
    // Add this method to update the profile panel user name
    private void updateProfilePanelUserName(String userName) {
        try {
            // Get the profile header panel components
            JPanel headerPanel = (JPanel) profilePanel.getComponent(0);
            JPanel userInfoPanel = (JPanel) headerPanel.getComponent(0);
            
            // Find the details panel (second component in the userInfoPanel)
            if (userInfoPanel.getComponentCount() > 1) {
                Component detailsComponent = userInfoPanel.getComponent(1);
                
                if (detailsComponent instanceof JPanel) {
                    JPanel detailsPanel = (JPanel) detailsComponent;
                    
                    // Get the name label (first component in details panel)
                    if (detailsPanel.getComponentCount() > 0) {
                        Component nameComponent = detailsPanel.getComponent(0);
                        
                        if (nameComponent instanceof JLabel) {
                            JLabel nameLabel = (JLabel) nameComponent;
                            nameLabel.setText(userName);
                            System.out.println("Profile panel user name updated to: " + userName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating profile panel user name: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTopNavUserName() {
        try {
            // Loop through all components to find the nav bar
            for (Component component : mainPanel.getComponents()) {
                if (component instanceof JPanel) {
                    JPanel possibleNavBar = (JPanel) component;
                    
                    // Check if this might be the nav bar (by checking background color)
                    if (possibleNavBar.getBackground().equals(new Color(40, 50, 70))) {
                        // This is likely the nav bar, now find the user panel
                        Component eastComponent = null;
                        
                        // Try to get the EAST component using BorderLayout
                        try {
                            BorderLayout layout = (BorderLayout) possibleNavBar.getLayout();
                            eastComponent = layout.getLayoutComponent(possibleNavBar, BorderLayout.EAST);
                        } catch (Exception ex) {
                            // Not BorderLayout or other issue, try component index
                            if (possibleNavBar.getComponentCount() >= 3) {
                                eastComponent = possibleNavBar.getComponent(2);
                            }
                        }
                        
                        if (eastComponent instanceof JPanel) {
                            JPanel userPanel = (JPanel) eastComponent;
                            
                            // Try to find the user info display panel (should be the last panel)
                            Component lastComponent = userPanel.getComponent(userPanel.getComponentCount() - 1);
                            
                            if (lastComponent instanceof JPanel) {
                                JPanel userInfoPanel = (JPanel) lastComponent;
                                
                                // Look for a JLabel in this panel
                                for (Component infoComp : userInfoPanel.getComponents()) {
                                    if (infoComp instanceof JLabel) {
                                        JLabel nameLabel = (JLabel) infoComp;
                                        nameLabel.setText("Welcome, " + userName);
                                        return;  // Found and updated, exit method
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating top nav user name: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves updated profile information to the database
     */
    private void saveProfileInfo(String registerNo, String address, String phone) {
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "User ID not set. Cannot save profile.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            // First check if columns exist
            boolean columnsExist = checkProfileColumnsExist(conn);
            
            if (!columnsExist) {
                JOptionPane.showMessageDialog(this, 
                    "Database schema needs to be updated. Please run the database update script first.", 
                    "Schema Update Required", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String sql = "UPDATE users SET register_no = ?, address = ?, phone = ? WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, registerNo);
                stmt.setString(2, address);
                stmt.setString(3, phone);
                stmt.setInt(4, userId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No changes were made to your profile.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving profile: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Loads profile information from the database
     */
    private void loadProfileInfo() {
        if (userId <= 0) {
            return;
        }
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            // First check if columns exist
            boolean columnsExist = checkProfileColumnsExist(conn);
            
            if (!columnsExist) {
                System.out.println("Profile columns don't exist in the database. Run update script.");
                return;
            }
            
            String sql = "SELECT email, register_no, address, phone FROM users WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String email = rs.getString("email");
                        String registerNo = rs.getString("register_no");
                        String address = rs.getString("address");
                        String phone = rs.getString("phone");
                        
                        // Update the profile panel
                        updateProfileFields(email, registerNo, address, phone);
                        
                        // Update the email in the profile panel header
                        updateProfileHeader(email);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading profile: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Updates the profile form fields with loaded data
     */
    private void updateProfileFields(String email, String registerNo, String address, String phone) {
        try {
            // Get the profile panel
            JPanel profileContentPanel = (JPanel) ((JScrollPane) ((JPanel) profilePanel.getComponent(0)).getComponent(1)).getViewport().getView();
            
            // Get the user info edit panel (first component in content panel)
            JPanel userInfoEditPanel = null;
            for (Component comp : profileContentPanel.getComponents()) {
                if (comp instanceof JPanel && ((JPanel) comp).getBorder() instanceof javax.swing.border.TitledBorder) {
                    userInfoEditPanel = (JPanel) comp;
                    break;
                }
            }
            
            if (userInfoEditPanel != null) {
                // Update the form fields
                for (Component comp : userInfoEditPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel fieldPanel = (JPanel) comp;
                        Component fieldComp = fieldPanel.getComponent(1);
                        if (fieldComp instanceof JTextField) {
                            JTextField field = (JTextField) fieldComp;
                            if ("registerField".equals(field.getName())) {
                                field.setText(registerNo != null ? registerNo : "");
                            } else if ("addressField".equals(field.getName())) {
                                field.setText(address != null ? address : "");
                            } else if ("phoneField".equals(field.getName())) {
                                field.setText(phone != null ? phone : "");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating profile fields: " + e.getMessage());
        }
    }
    
    /**
     * Updates the email display in the profile header
     */
    private void updateProfileHeader(String email) {
        try {
            // Get the profile panel header
            JPanel headerPanel = (JPanel) profilePanel.getComponent(0);
            JPanel userInfoPanel = (JPanel) headerPanel.getComponent(0);
            
            // Find the details panel
            Component[] infoComponents = userInfoPanel.getComponents();
            
            if (infoComponents.length > 1 && infoComponents[1] instanceof JPanel) {
                JPanel detailsPanel = (JPanel) infoComponents[1];
                Component[] detailComponents = detailsPanel.getComponents();
                
                // Update email label (should be the second label)
                if (detailComponents.length > 2 && detailComponents[2] instanceof JLabel) {
                    JLabel emailLabel = (JLabel) detailComponents[2];
                    emailLabel.setText(email);
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating profile header: " + e.getMessage());
        }
    }
    
    /**
     * Checks if the profile columns exist in the users table
     */
    private boolean checkProfileColumnsExist(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            boolean registerNoExists = false;
            boolean addressExists = false;
            boolean phoneExists = false;
            
            try (ResultSet rs = meta.getColumns(null, null, "users", "register_no")) {
                registerNoExists = rs.next();
            }
            
            try (ResultSet rs = meta.getColumns(null, null, "users", "address")) {
                addressExists = rs.next();
            }
            
            try (ResultSet rs = meta.getColumns(null, null, "users", "phone")) {
                phoneExists = rs.next();
            }
            
            return registerNoExists && addressExists && phoneExists;
        } catch (SQLException e) {
            System.out.println("Error checking for profile columns: " + e.getMessage());
            return false;
        }
    }
}