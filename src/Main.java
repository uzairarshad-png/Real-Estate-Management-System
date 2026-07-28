import database.DatabaseManager;
import gui.LoginScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import notification.NotificationCenter;
import payment.PaymentEngine;
import security.AuditLog;
import security.Session;

/**
 * REMS — Real Estate Management System
 * Entry point for the JavaFX application.
 *
 * Startup sequence:
 *   1. Initialize SQLite database (create tables if not exist)
 *   2. Load all persisted data into memory
 *   3. Launch Login Screen
 *
 * Shutdown sequence:
 *   1. Save session state
 *   2. Close DB connection
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // ── Step 1: Initialize Database ──────────────────
            DatabaseManager db = DatabaseManager.getInstance();
            db.initializeDatabase();

            // ── Step 2: Load Persisted Data ───────────────────
            AuditLog.getInstance().loadLogs(db.loadAuditLogs());
            NotificationCenter.getInstance().loadNotifications(db.loadNotifications());
            PaymentEngine.getInstance().loadPayments(db.loadAllPayments());

            // ── Step 3: Launch Login Screen ───────────────────
            LoginScreen loginScreen = new LoginScreen(primaryStage);
            loginScreen.show();

        } catch (Exception e) {
            System.err.println("[Main] Fatal startup error: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        // Called automatically when app closes
        try {
            if (Session.getInstance().isLoggedIn()) {
                Session.getInstance().logout();
            }
            DatabaseManager.getInstance().closeConnection();
            System.out.println("[Main] Application closed cleanly.");
        } catch (Exception e) {
            System.err.println("[Main] Error during shutdown: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
