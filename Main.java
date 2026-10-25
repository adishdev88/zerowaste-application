package com.zerowaste;

import javax.swing.SwingUtilities;

/**
 * Main.java — Starting point of the ZeroWaste application.
 * This class simply launches the Login screen.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize database: create tables and insert sample data
        DBConnection.initializeDatabase();

        // Launch the login window on the Swing event thread (standard Swing practice)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
