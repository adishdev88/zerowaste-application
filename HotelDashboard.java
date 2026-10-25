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
import javax.swing.border.TitledBorder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * HotelDashboard.java — The dashboard shown after a hotel partner logs in.
 *
 * Layout:
 *   Tab 1 — Manage Food:
 *       Table of food items for this hotel.
 *       Form to add a new food item.
 *       Button to update the quantity of a selected item.
 *   Tab 2 — Hotel Orders:
 *       Table of orders placed for this hotel.
 */
public class HotelDashboard extends JFrame {

    private int loggedInUserId;   // the user ID of the hotel partner account
    private int hotelId;          // the hotel ID belonging to this partner
    private String hotelName;     // displayed in the header

    // Colors
    private static final Color GREEN_DARK  = new Color(34, 139, 34);
    private static final Color GREEN_LIGHT = new Color(240, 255, 240);
    private static final Color WHITE       = Color.WHITE;

    // Tab 1 components
    private JTable foodTable;
    private DefaultTableModel foodTableModel;

    private JTextField addNameField;
    private JTextField addQtyField;
    private JTextField addPriceField;
    private JTextField addDescField;

    // Tab 2 components
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;

    public HotelDashboard(int userId) {
        this.loggedInUserId = userId;

        // Find out which hotel belongs to this user
        loadHotelInfo();

        setTitle("ZeroWaste — Hotel Dashboard: " + hotelName);
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
        loadFoodItems();
    }

    /**
     * Queries the database to find the hotel record linked to this user account.
     * Sets hotelId and hotelName.
     */
    private void loadHotelInfo() {
        String sql = "SELECT id, name FROM hotels WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                hotelId   = rs.getInt("id");
                hotelName = rs.getString("name");
            } else {
                hotelId   = 0;
                hotelName = "Unknown Hotel";
            }

        } catch (SQLException e) {
            hotelId   = 0;
            hotelName = "Error";
            System.out.println("Error loading hotel info: " + e.getMessage());
        }
    }

    /** Builds the tabbed layout. */
    private void buildUI() {

        // ---- Top header bar ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(GREEN_DARK);
        headerPanel.setPreferredSize(new Dimension(900, 55));

        JLabel titleLabel = new JLabel("  🏨 Hotel Dashboard — " + hotelName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setForeground(WHITE);

        JButton logoutButton = makeSmallButton("Logout");
        logoutButton.addActionListener(e -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        headerRight.setBackground(GREEN_DARK);
        headerRight.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        // ---- Tabbed pane ----
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));

        tabbedPane.addTab("🥗  Manage Food",   buildManageFoodTab());
        tabbedPane.addTab("📋  Hotel Orders",  buildHotelOrdersTab());

        // Refresh orders when the tab is selected
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadHotelOrders();
            }
        });

        // ---- Assemble frame ----
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ============================================================
    // TAB 1 — MANAGE FOOD
    // ============================================================

    private JPanel buildManageFoodTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tab.setBackground(WHITE);

        // ---- Food table ----
        String[] cols = {"ID", "Food Name", "Available Qty", "Price (₹)", "Description"};
        foodTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        foodTable = new JTable(foodTableModel);
        foodTable.setFont(new Font("Arial", Font.PLAIN, 13));
        foodTable.setRowHeight(30);
        foodTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        foodTable.getTableHeader().setBackground(GREEN_DARK);
        foodTable.getTableHeader().setForeground(WHITE);
        foodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        foodTable.setGridColor(new Color(200, 230, 200));

        // Hide the ID column — it is used internally but not shown
        foodTable.getColumnModel().getColumn(0).setMinWidth(0);
        foodTable.getColumnModel().getColumn(0).setMaxWidth(0);
        foodTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane foodScroll = new JScrollPane(foodTable);
        foodScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GREEN_DARK),
                "Your Food Items", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), GREEN_DARK));

        // ---- Bottom panel with two sections: Add Food + Update Quantity ----
        JPanel bottomPanel = new JPanel(new BorderLayout(15, 0));
        bottomPanel.setBackground(WHITE);

        bottomPanel.add(buildAddFoodPanel(),      BorderLayout.CENTER);
        bottomPanel.add(buildUpdateQtyPanel(),    BorderLayout.EAST);

        tab.add(foodScroll, BorderLayout.CENTER);
        tab.add(bottomPanel, BorderLayout.SOUTH);

        return tab;
    }

    /** Form for adding a new food item. */
    private JPanel buildAddFoodPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(GREEN_LIGHT);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GREEN_DARK),
                "Add New Food Item", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), GREEN_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Food Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        panel.add(boldLabel("Food Name:"), gbc);
        addNameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(addNameField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(boldLabel("Quantity:"), gbc);
        addQtyField = new JTextField(8);
        gbc.gridx = 1; gbc.gridwidth = 1;
        panel.add(addQtyField, gbc);

        // Price
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(boldLabel("Price (₹):"), gbc);
        addPriceField = new JTextField(8);
        gbc.gridx = 3;
        panel.add(addPriceField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(boldLabel("Description:"), gbc);
        addDescField = new JTextField(22);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(addDescField, gbc);

        // Add button
        JButton addButton = new JButton("Add Food Item");
        addButton.setBackground(GREEN_DARK);
        addButton.setForeground(WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 13));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addFoodItem());

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(addButton, gbc);

        return panel;
    }

    /** Panel for updating the quantity of a selected food item. */
    private JPanel buildUpdateQtyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(GREEN_LIGHT);
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GREEN_DARK),
                "Update Quantity", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), GREEN_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel instruction = new JLabel("<html>Select a row in the table,<br>then enter new quantity:</html>");
        instruction.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(instruction, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(boldLabel("New Qty:"), gbc);

        JTextField updateQtyField = new JTextField(6);
        gbc.gridx = 1;
        panel.add(updateQtyField, gbc);

        JButton updateButton = new JButton("Update");
        updateButton.setBackground(GREEN_DARK);
        updateButton.setForeground(WHITE);
        updateButton.setFont(new Font("Arial", Font.BOLD, 12));
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateButton.setFocusPainted(false);
        updateButton.addActionListener(e -> updateFoodQuantity(updateQtyField));

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(updateButton, gbc);

        return panel;
    }

    // ============================================================
    // TAB 2 — HOTEL ORDERS
    // ============================================================

    private JPanel buildHotelOrdersTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tab.setBackground(WHITE);

        JLabel heading = new JLabel("Orders for " + hotelName);
        heading.setFont(new Font("Arial", Font.BOLD, 16));
        heading.setForeground(GREEN_DARK);

        String[] cols = {"Order ID", "Food Item", "Qty Ordered", "Total Price (₹)", "Status"};
        ordersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        ordersTable = new JTable(ordersTableModel);
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 13));
        ordersTable.setRowHeight(30);
        ordersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        ordersTable.getTableHeader().setBackground(GREEN_DARK);
        ordersTable.getTableHeader().setForeground(WHITE);
        ordersTable.setGridColor(new Color(200, 230, 200));

        JScrollPane scroll = new JScrollPane(ordersTable);

        JButton refreshButton = makeSmallButton("Refresh");
        refreshButton.addActionListener(e -> loadHotelOrders());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.add(heading, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        tab.add(topPanel, BorderLayout.NORTH);
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    // ============================================================
    // DATA LOADING
    // ============================================================

    /** Loads all food items belonging to this hotel into the food table. */
    private void loadFoodItems() {
        foodTableModel.setRowCount(0);

        String sql = "SELECT id, name, quantity, price, description FROM foods WHERE hotel_id = ? ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                foodTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        String.format("%.2f", rs.getDouble("price")),
                        rs.getString("description")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading food items: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Loads all orders for this hotel into the orders table. */
    private void loadHotelOrders() {
        ordersTableModel.setRowCount(0);

        String sql = "SELECT o.id, f.name AS food_name, o.quantity, o.total_price, o.status " +
                     "FROM orders o " +
                     "JOIN foods f ON o.food_id = f.id " +
                     "WHERE o.hotel_id = ? " +
                     "ORDER BY o.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ordersTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        String.format("%.2f", rs.getDouble("total_price")),
                        rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // ACTIONS
    // ============================================================

    /** Reads the add-food form and inserts a new food item into the database. */
    private void addFoodItem() {
        String name  = addNameField.getText().trim();
        String qtyTx = addQtyField.getText().trim();
        String priceTx = addPriceField.getText().trim();
        String desc  = addDescField.getText().trim();

        // Validation
        if (name.isEmpty() || qtyTx.isEmpty() || priceTx.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in Food Name, Quantity, and Price.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty;
        double price;

        try {
            qty = Integer.parseInt(qtyTx);
            if (qty <= 0) throw new NumberFormatException("Qty must be positive");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            price = Double.parseDouble(priceTx);
            if (price <= 0) throw new NumberFormatException("Price must be positive");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price must be a positive number.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Insert into database
        String sql = "INSERT INTO foods (hotel_id, name, quantity, price, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
            ps.setString(2, name);
            ps.setInt(3, qty);
            ps.setDouble(4, price);
            ps.setString(5, desc);
            ps.executeUpdate();

            // Clear the form fields
            addNameField.setText("");
            addQtyField.setText("");
            addPriceField.setText("");
            addDescField.setText("");

            // Refresh the food table
            loadFoodItems();

            JOptionPane.showMessageDialog(this,
                    "✅ Food item \"" + name + "\" added successfully!",
                    "Item Added", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding food item: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Updates the quantity of the selected food item in the database. */
    private void updateFoodQuantity(JTextField updateQtyField) {
        int selectedRow = foodTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a food item from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyTx = updateQtyField.getText().trim();

        if (qtyTx.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a new quantity.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newQty;
        try {
            newQty = Integer.parseInt(qtyTx);
            if (newQty < 0) throw new NumberFormatException("Cannot be negative");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantity must be a non-negative whole number.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the food ID from the hidden first column (column index 0)
        int foodId = (int) foodTableModel.getValueAt(selectedRow, 0);
        String foodName = (String) foodTableModel.getValueAt(selectedRow, 1);

        String sql = "UPDATE foods SET quantity = ? WHERE id = ? AND hotel_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setInt(2, foodId);
            ps.setInt(3, hotelId);
            ps.executeUpdate();

            updateQtyField.setText("");
            loadFoodItems();   // refresh table

            JOptionPane.showMessageDialog(this,
                    "✅ Quantity for \"" + foodName + "\" updated to " + newQty + ".",
                    "Quantity Updated", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating quantity: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private JLabel boldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        return label;
    }

    private JButton makeSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(WHITE);
        btn.setForeground(GREEN_DARK);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GREEN_DARK, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return btn;
    }

}
