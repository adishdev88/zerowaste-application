package com.zerowaste;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection.java — Handles all database setup and connectivity.
 *
 * This class connects to a local SQLite file called zerowaste.db.
 * It creates the required tables if they do not exist.
 * It also inserts sample accounts, hotels, and food items for demonstration.
 *
 * SQLite does not require a separate database server — the database
 * is stored as a single file in the project folder.
 */
public class DBConnection {

    // Path to the SQLite database file (relative to where the program is run from)
    private static final String DB_URL = "jdbc:sqlite:zerowaste.db";

    /**
     * Opens and returns a connection to the SQLite database.
     * Call this method whenever you need to run a SQL query.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Creates all tables and inserts sample data.
     * Called once when the application starts (from Main.java).
     */
    public static void initializeDatabase() {
        createTables();
        insertSampleData();
    }

    /**
     * Creates the four main tables if they do not already exist.
     */
    private static void createTables() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "account_type TEXT NOT NULL" +   // 'user' or 'hotel'
                ")";

        String createHotels = "CREATE TABLE IF NOT EXISTS hotels (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "contact TEXT, " +
                "user_id INTEGER" +              // links hotel to the hotel partner account
                ")";

        String createFoods = "CREATE TABLE IF NOT EXISTS foods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "hotel_id INTEGER NOT NULL, " +
                "name TEXT NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "description TEXT" +
                ")";

        String createOrders = "CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "hotel_id INTEGER NOT NULL, " +
                "food_id INTEGER NOT NULL, " +
                "quantity INTEGER NOT NULL, " +
                "total_price REAL NOT NULL, " +
                "status TEXT NOT NULL" +         // e.g. 'Placed'
                ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createHotels);
            stmt.execute(createFoods);
            stmt.execute(createOrders);

            System.out.println("Database tables created (or already exist).");

        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }

    /**
     * Inserts sample accounts, hotels, and food items.
     * Checks if records already exist before inserting to avoid duplicates.
     */
    private static void insertSampleData() {
        try (Connection conn = getConnection()) {

            // ---- Insert sample USER account ----
            if (!recordExists(conn, "users", "email", "user@gmail.com")) {
                String sql = "INSERT INTO users (name, email, password, account_type) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "Regular User");
                ps.setString(2, "user@gmail.com");
                ps.setString(3, "user123");
                ps.setString(4, "user");
                ps.executeUpdate();
            }

            // ---- Insert sample HOTEL PARTNER account ----
            if (!recordExists(conn, "users", "email", "hotel@gmail.com")) {
                String sql = "INSERT INTO users (name, email, password, account_type) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "Green Leaf Manager");
                ps.setString(2, "hotel@gmail.com");
                ps.setString(3, "hotel123");
                ps.setString(4, "hotel");
                ps.executeUpdate();
            }

            // ---- Insert ADMIN account ----
            if (!recordExists(conn, "users", "email", "admin@gmail.com")) {
                String sql = "INSERT INTO users (name, email, password, account_type) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "Administrator");
                ps.setString(2, "admin@gmail.com");
                ps.setString(3, "admin123");
                ps.setString(4, "admin");
                ps.executeUpdate();
            }


            // Get the hotel partner's user ID
            int hotelUserId = getUserId(conn, "hotel@gmail.com");

            // ---- Insert sample HOTELS ----
            int greenLeafId = 0;
            int cityBakeryId = 0;
            int freshMealsId = 0;

            if (!recordExists(conn, "hotels", "name", "Green Leaf Restaurant")) {
                String sql = "INSERT INTO hotels (name, contact, user_id) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "Green Leaf Restaurant");
                ps.setString(2, "9876543210");
                ps.setInt(3, hotelUserId);
                ps.executeUpdate();
            }
            greenLeafId = getHotelId(conn, "Green Leaf Restaurant");

            if (!recordExists(conn, "hotels", "name", "City Bakery")) {
                String sql = "INSERT INTO hotels (name, contact, user_id) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "City Bakery");
                ps.setString(2, "9123456789");
                ps.setInt(3, 0);
                ps.executeUpdate();
            }
            cityBakeryId = getHotelId(conn, "City Bakery");

            if (!recordExists(conn, "hotels", "name", "Fresh Meals Hotel")) {
                String sql = "INSERT INTO hotels (name, contact, user_id) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "Fresh Meals Hotel");
                ps.setString(2, "9012345678");
                ps.setInt(3, 0);
                ps.executeUpdate();
            }
            freshMealsId = getHotelId(conn, "Fresh Meals Hotel");

            // ---- Insert sample FOOD items for Green Leaf Restaurant ----
            insertFoodIfMissing(conn, greenLeafId, "Rice Meal",         20, 50.0,  "Steamed rice with dal and sabzi");
            insertFoodIfMissing(conn, greenLeafId, "Chapati with Curry", 15, 40.0, "Soft chapatis with mixed vegetable curry");
            insertFoodIfMissing(conn, greenLeafId, "Vegetable Biryani", 10, 70.0,  "Aromatic vegetable biryani");

            // ---- Insert sample FOOD items for City Bakery ----
            insertFoodIfMissing(conn, cityBakeryId, "Bread Loaf",       25, 30.0,  "Fresh baked whole wheat bread");
            insertFoodIfMissing(conn, cityBakeryId, "Bun Pack",         30, 20.0,  "Pack of 6 soft buns");

            // ---- Insert sample FOOD items for Fresh Meals Hotel ----
            insertFoodIfMissing(conn, freshMealsId, "Idli Sambhar",     18, 35.0,  "4 soft idlis with hot sambhar");
            insertFoodIfMissing(conn, freshMealsId, "Poha",             12, 25.0,  "Flattened rice with onion and spices");

            System.out.println("Sample data inserted (or already existed).");

        } catch (SQLException e) {
            System.out.println("Error inserting sample data: " + e.getMessage());
        }
    }

    // ---- Helper methods ----

    /** Returns true if a row exists in the given table where the column equals the value. */
    private static boolean recordExists(Connection conn, String table, String column, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, value);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    /** Returns the user ID for the given email. */
    private static int getUserId(Connection conn, String email) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return 0;
    }

    /** Returns the hotel ID for the given hotel name. */
    private static int getHotelId(Connection conn, String hotelName) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM hotels WHERE name = ?");
        ps.setString(1, hotelName);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return 0;
    }

    /** Inserts a food item only if no food with the same name exists for that hotel. */
    private static void insertFoodIfMissing(Connection conn, int hotelId, String name,
                                             int qty, double price, String desc) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM foods WHERE hotel_id = ? AND name = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, hotelId);
        checkPs.setString(2, name);
        ResultSet rs = checkPs.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
            String sql = "INSERT INTO foods (hotel_id, name, quantity, price, description) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, hotelId);
            ps.setString(2, name);
            ps.setInt(3, qty);
            ps.setDouble(4, price);
            ps.setString(5, desc);
            ps.executeUpdate();
        }
    }
}
