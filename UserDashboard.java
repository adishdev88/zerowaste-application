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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * UserDashboard.java — The main screen shown after a user logs in.
 *
 * Layout:
 *   Tab 1 — Order Food:
 *       Left panel  — list of hotels
 *       Right panel — food items for the selected hotel + ordering controls
 *   Tab 2 — My Orders:
 *       Table showing all past orders for this user
 */
public class UserDashboard extends JFrame {

    private int loggedInUserId;   // the ID of the currently logged-in user

    // Colors — green theme
    private static final Color GREEN_DARK  = new Color(34, 139, 34);
    private static final Color GREEN_LIGHT = new Color(240, 255, 240);
    private static final Color WHITE       = Color.WHITE;

    // Components for Tab 1 (Order Food)
    private JList<String> hotelList;
    private DefaultListModel<String> hotelListModel;
    private List<Integer> hotelIdList;   // parallel list of hotel IDs

    private JTable foodTable;
    private DefaultTableModel foodTableModel;
    private List<Food> currentFoods;    // food items shown in the table

    private JTextField quantityField;
    private JLabel totalPriceLabel;

    // Components for Tab 2 (My Orders)
    private JTable ordersTable;
    private DefaultTableModel ordersTableModel;

    public UserDashboard(int userId) {
        this.loggedInUserId = userId;

        setTitle("ZeroWaste — User Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        currentFoods = new ArrayList<>();
        hotelIdList  = new ArrayList<>();

        buildUI();
        loadHotels();
    }

    /** Builds the tabbed layout. */
    private void buildUI() {

        // ---- Top header bar ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(GREEN_DARK);
        headerPanel.setPreferredSize(new Dimension(900, 55));

        JLabel titleLabel = new JLabel("  🌿 ZeroWaste — Order Surplus Food");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(WHITE);

        JLabel pickupLabel = new JLabel("📦 Pickup from Hotel — No Delivery  ");
        pickupLabel.setFont(new Font("Arial", Font.BOLD, 13));
        pickupLabel.setForeground(new Color(144, 238, 144));

        JButton logoutButton = makeSmallButton("Logout");
        logoutButton.addActionListener(e -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        headerRight.setBackground(GREEN_DARK);
        headerRight.add(pickupLabel);
        headerRight.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        // ---- Tabbed pane ----
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));

        tabbedPane.addTab("🍽  Order Food",  buildOrderFoodTab());
        tabbedPane.addTab("📋  My Orders",   buildMyOrdersTab());

        // When the user clicks "My Orders" tab, refresh the orders table
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadMyOrders();
            }
        });

        // ---- Assemble frame ----
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    // ============================================================
    // TAB 1 — ORDER FOOD
    // ============================================================

    /** Builds the "Order Food" tab with hotel list on the left and food on the right. */
    private JPanel buildOrderFoodTab() {
        JPanel tab = new JPanel(new BorderLayout());

        // ---- Left panel: hotel list ----
        hotelListModel = new DefaultListModel<>();
        hotelList = new JList<>(hotelListModel);
        hotelList.setFont(new Font("Arial", Font.PLAIN, 14));
        hotelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hotelList.setBackground(GREEN_LIGHT);
        hotelList.setFixedCellHeight(40);

        JScrollPane hotelScroll = new JScrollPane(hotelList);
        hotelScroll.setPreferredSize(new Dimension(220, 0));

        JPanel hotelPanel = new JPanel(new BorderLayout());
        hotelPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GREEN_DARK),
                "Available Hotels", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), GREEN_DARK));
        hotelPanel.add(hotelScroll, BorderLayout.CENTER);

        // When the user clicks a hotel, load its food items
        hotelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && hotelList.getSelectedIndex() >= 0) {
                int selectedIndex = hotelList.getSelectedIndex();
                int hotelId = hotelIdList.get(selectedIndex);
                loadFoodsForHotel(hotelId);
            }
        });

        // ---- Right panel: food table + order controls ----
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setBackground(WHITE);

        // Food table
        String[] foodColumns = {"Food Name", "Available Qty", "Price (₹)", "Description"};
        foodTableModel = new DefaultTableModel(foodColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;   // table is read-only
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

        JScrollPane foodScroll = new JScrollPane(foodTable);
        foodScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GREEN_DARK),
                "Available Food Items", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), GREEN_DARK));

        // Order controls panel below the food table
        JPanel orderPanel = buildOrderControlsPanel();

        rightPanel.add(foodScroll, BorderLayout.CENTER);
        rightPanel.add(orderPanel, BorderLayout.SOUTH);

        // ---- Combine left and right with a split pane ----
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hotelPanel, rightPanel);
        splitPane.setDividerLocation(220);
        splitPane.setDividerSize(4);

        JPanel tab1 = new JPanel(new BorderLayout());
        tab1.add(splitPane, BorderLayout.CENTER);
        return tab1;
    }

    /** Builds the controls used to enter quantity and place an order. */
    private JPanel buildOrderControlsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 255, 245));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GREEN_DARK),
                "Place Order", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 13), GREEN_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Quantity label and field
        JLabel qtyLabel = new JLabel("Enter Quantity:");
        qtyLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(qtyLabel, gbc);

        quantityField = new JTextField(6);
        quantityField.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(quantityField, gbc);

        // Total price display
        JLabel totalLabel = new JLabel("Total Price:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(totalLabel, gbc);

        totalPriceLabel = new JLabel("₹ 0.00");
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPriceLabel.setForeground(GREEN_DARK);
        gbc.gridx = 3; gbc.gridy = 0;
        panel.add(totalPriceLabel, gbc);

        // Calculate button
        JButton calcButton = makeSmallButton("Calculate");
        calcButton.addActionListener(e -> calculateTotal());
        gbc.gridx = 4; gbc.gridy = 0;
        panel.add(calcButton, gbc);

        // Order button
        JButton orderButton = new JButton("Place Order");
        orderButton.setBackground(GREEN_DARK);
        orderButton.setForeground(WHITE);
        orderButton.setFont(new Font("Arial", Font.BOLD, 13));
        orderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        orderButton.setFocusPainted(false);
        orderButton.addActionListener(e -> placeOrder());
        gbc.gridx = 5; gbc.gridy = 0;
        panel.add(orderButton, gbc);

        // Note about pickup
        JLabel noteLabel = new JLabel("  ℹ️  This order is for PICKUP only. No delivery.");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.DARK_GRAY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 6;
        panel.add(noteLabel, gbc);

        return panel;
    }

    // ============================================================
    // TAB 2 — MY ORDERS
    // ============================================================

    /** Builds the "My Orders" tab with a table of past orders. */
    private JPanel buildMyOrdersTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tab.setBackground(WHITE);

        JLabel heading = new JLabel("Your Order History");
        heading.setFont(new Font("Arial", Font.BOLD, 16));
        heading.setForeground(GREEN_DARK);

        String[] cols = {"Order ID", "Hotel", "Food Item", "Qty", "Total Price (₹)", "Status", "Pickup"};
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
        refreshButton.addActionListener(e -> loadMyOrders());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(WHITE);
        topPanel.add(heading, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);

        tab.add(topPanel, BorderLayout.NORTH);
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    // ============================================================
    // DATA LOADING METHODS
    // ============================================================

    /** Loads all hotels from the database and populates the hotel list. */
    private void loadHotels() {
        hotelListModel.clear();
        hotelIdList.clear();

        String sql = "SELECT id, name FROM hotels ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hotelIdList.add(rs.getInt("id"));
                hotelListModel.addElement("  🏨 " + rs.getString("name"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading hotels: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Loads food items for the selected hotel and shows them in the food table. */
    private void loadFoodsForHotel(int hotelId) {
        foodTableModel.setRowCount(0);   // clear existing rows
        currentFoods.clear();
        quantityField.setText("");
        totalPriceLabel.setText("₹ 0.00");

        String sql = "SELECT id, hotel_id, name, quantity, price, description " +
                     "FROM foods WHERE hotel_id = ? ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hotelId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Food food = new Food(
                        rs.getInt("id"),
                        rs.getInt("hotel_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("description")
                );
                currentFoods.add(food);

                // Add a row to the table
                foodTableModel.addRow(new Object[]{
                        food.getName(),
                        food.getQuantity(),
                        String.format("%.2f", food.getPrice()),
                        food.getDescription()
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading food items: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Loads all orders placed by this user and populates the orders table. */
    private void loadMyOrders() {
        ordersTableModel.setRowCount(0);

        // Join orders with hotels and foods to get their names
        String sql = "SELECT o.id, h.name AS hotel_name, f.name AS food_name, " +
                     "o.quantity, o.total_price, o.status " +
                     "FROM orders o " +
                     "JOIN hotels h ON o.hotel_id = h.id " +
                     "JOIN foods  f ON o.food_id  = f.id " +
                     "WHERE o.user_id = ? " +
                     "ORDER BY o.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ordersTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("hotel_name"),
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        String.format("%.2f", rs.getDouble("total_price")),
                        rs.getString("status"),
                        "Pickup from Hotel"
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // ORDER LOGIC
    // ============================================================

    /** Reads the quantity field and calculates the total price for the selected food. */
    private void calculateTotal() {
        int foodRow = foodTable.getSelectedRow();
        if (foodRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a food item first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a quantity.",
                    "Missing Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.",
                        "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Food selectedFood = currentFoods.get(foodRow);
            double total = qty * selectedFood.getPrice();
            totalPriceLabel.setText(String.format("₹ %.2f", total));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid whole number for quantity.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Validates input and places an order. */
    private void placeOrder() {
        int foodRow = foodTable.getSelectedRow();
        if (foodRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a food item first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a quantity.",
                    "Missing Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid whole number for quantity.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.",
                    "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Food selectedFood = currentFoods.get(foodRow);

        if (qty > selectedFood.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                    "Requested quantity (" + qty + ") exceeds available quantity (" + selectedFood.getQuantity() + ").\n"
                    + "Please enter a smaller number.",
                    "Not Enough Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalPrice = qty * selectedFood.getPrice();

        // Show a confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm Order:\n\n" +
                "Food:       " + selectedFood.getName() + "\n" +
                "Quantity:   " + qty + "\n" +
                "Total Price: ₹" + String.format("%.2f", totalPrice) + "\n\n" +
                "📦 PICKUP ONLY — You will collect this from the hotel.\n\n" +
                "Proceed?",
                "Confirm Order", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;   // user cancelled
        }

        // Get the hotel ID from the selected hotel in the list
        int hotelIndex = hotelList.getSelectedIndex();
        int hotelId = hotelIdList.get(hotelIndex);

        // Save order to database and reduce food quantity
        boolean success = saveOrder(loggedInUserId, hotelId, selectedFood.getId(), qty, totalPrice);

        if (success) {
            // Update the in-memory food object so the table shows the new quantity
            selectedFood.setQuantity(selectedFood.getQuantity() - qty);

            // Refresh the food table row
            foodTableModel.setValueAt(selectedFood.getQuantity(), foodRow, 1);

            // Clear fields
            quantityField.setText("");
            totalPriceLabel.setText("₹ 0.00");

            JOptionPane.showMessageDialog(this,
                    "✅ Order placed successfully!\n\n" +
                    "Food: " + selectedFood.getName() + "\n" +
                    "Quantity: " + qty + "\n" +
                    "Total: ₹" + String.format("%.2f", totalPrice) + "\n\n" +
                    "📦 Please go to the hotel for pickup.",
                    "Order Placed", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to place order. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inserts a new order record into the database and reduces the food quantity.
     * Returns true if both operations succeed.
     */
    private boolean saveOrder(int userId, int hotelId, int foodId, int qty, double totalPrice) {
        String insertOrder = "INSERT INTO orders (user_id, hotel_id, food_id, quantity, total_price, status) " +
                             "VALUES (?, ?, ?, ?, ?, 'Placed')";
        String updateQty   = "UPDATE foods SET quantity = quantity - ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);   // use a transaction so both succeed or both fail

            try (PreparedStatement ps1 = conn.prepareStatement(insertOrder);
                 PreparedStatement ps2 = conn.prepareStatement(updateQty)) {

                ps1.setInt(1, userId);
                ps1.setInt(2, hotelId);
                ps1.setInt(3, foodId);
                ps1.setInt(4, qty);
                ps1.setDouble(5, totalPrice);
                ps1.executeUpdate();

                ps2.setInt(1, qty);
                ps2.setInt(2, foodId);
                ps2.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Order save failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // HELPER
    // ============================================================

    /** Creates a small styled button. */
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
