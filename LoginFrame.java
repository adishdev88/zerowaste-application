package com.zerowaste;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * LoginFrame.java — The login window shown when the application starts.
 *
 * Features:
 * - Email and password fields
 * - Radio buttons: User Login, Hotel Partner Login, Admin Login
 * - A Login button that checks credentials against the database
 * - A Register button that opens RegisterFrame for new accounts
 *
 * Demo accounts:
 *   User:  email=user@gmail.com   password=user123
 *   Hotel: email=hotel@gmail.com  password=hotel123
 *   Admin: email=admin@gmail.com  password=admin123
 */
public class LoginFrame extends JFrame {

    // Swing components declared here so action listeners can access them
    private JTextField emailField;
    private JPasswordField passwordField;
    private JRadioButton userRadio;
    private JRadioButton hotelRadio;
    private JRadioButton adminRadio;

    // Colors — simple green theme
    private static final Color GREEN_DARK  = new Color(34, 139, 34);
    private static final Color GREEN_LIGHT = new Color(144, 238, 144);
    private static final Color WHITE       = Color.WHITE;

    public LoginFrame() {
        setTitle("ZeroWaste — Food Redistribution");
        setSize(460, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    /** Creates and arranges all components on the login screen. */
    private void buildUI() {

        // ---- Top header panel ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(GREEN_DARK);
        headerPanel.setPreferredSize(new Dimension(460, 120));

        JLabel titleLabel = new JLabel("🌿 ZeroWaste", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 5, 10));

        JLabel subtitleLabel = new JLabel("Reducing Food Waste, One Meal at a Time", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        subtitleLabel.setForeground(GREEN_LIGHT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 15, 10));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // ---- Centre form panel ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 5, 7, 5);

        // "Login as:" label
        JLabel loginAsLabel = new JLabel("Login as:");
        loginAsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(loginAsLabel, gbc);

        // Three radio buttons for account type
        userRadio  = new JRadioButton("User");
        hotelRadio = new JRadioButton("Hotel Partner");
        adminRadio = new JRadioButton("Admin");
        userRadio.setSelected(true);
        userRadio.setBackground(WHITE);
        hotelRadio.setBackground(WHITE);
        adminRadio.setBackground(WHITE);
        userRadio.setFont(new Font("Arial", Font.PLAIN, 13));
        hotelRadio.setFont(new Font("Arial", Font.PLAIN, 13));
        adminRadio.setFont(new Font("Arial", Font.PLAIN, 13));

        // Group so only one can be selected at a time
        ButtonGroup group = new ButtonGroup();
        group.add(userRadio);
        group.add(hotelRadio);
        group.add(adminRadio);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radioPanel.setBackground(WHITE);
        radioPanel.add(userRadio);
        radioPanel.add(hotelRadio);
        radioPanel.add(adminRadio);

        gbc.gridy = 1;
        formPanel.add(radioPanel, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridy = 2;
        formPanel.add(emailLabel, gbc);

        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(300, 35));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GREEN_DARK, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        gbc.gridy = 3;
        formPanel.add(emailField, gbc);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridy = 4;
        formPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setPreferredSize(new Dimension(300, 35));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GREEN_DARK, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        gbc.gridy = 5;
        formPanel.add(passwordField, gbc);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(GREEN_DARK);
        loginButton.setForeground(WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(300, 42));
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setFocusPainted(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(16, 5, 4, 5);
        formPanel.add(loginButton, gbc);

        // Register button (below Login)
        JButton registerButton = new JButton("New here? Register an Account");
        registerButton.setBackground(WHITE);
        registerButton.setForeground(GREEN_DARK);
        registerButton.setFont(new Font("Arial", Font.PLAIN, 13));
        registerButton.setPreferredSize(new Dimension(300, 36));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GREEN_DARK, 1),
                BorderFactory.createEmptyBorder(4, 0, 4, 0)));
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 5, 8, 5);
        formPanel.add(registerButton, gbc);

        // Demo credentials hint
        JLabel hintLabel = new JLabel(
                "<html><center>" +
                "User: user@gmail.com / user123<br>" +
                "Hotel: hotel@gmail.com / hotel123<br>" +
                "Admin: admin@gmail.com / admin123" +
                "</center></html>",
                SwingConstants.CENTER);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        gbc.gridy = 8;
        gbc.insets = new Insets(4, 5, 5, 5);
        formPanel.add(hintLabel, gbc);

        // ---- Button actions ----
        loginButton.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        registerButton.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame(this);
            registerFrame.setVisible(true);
        });

        // ---- Add panels to frame ----
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    /**
     * Called when Login is clicked.
     * Reads fields, checks DB, opens the correct dashboard.
     */
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Determine account type from radio selection
        String type;
        if (userRadio.isSelected()) {
            type = "user";
        } else if (hotelRadio.isSelected()) {
            type = "hotel";
        } else {
            type = "admin";
        }

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your email and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = checkLogin(email, password, type);

        if (userId > 0) {
            if (type.equals("user")) {
                new UserDashboard(userId).setVisible(true);
            } else if (type.equals("hotel")) {
                new HotelDashboard(userId).setVisible(true);
            } else {
                // Admin
                new AdminDashboard().setVisible(true);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid email, password, or account type.\nPlease try again.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Checks credentials in the database.
     *
     * @return user ID if correct, -1 if wrong
     */
    private int checkLogin(String email, String password, String type) {
        String sql = "SELECT id FROM users WHERE email = ? AND password = ? AND account_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return -1;
    }
}
