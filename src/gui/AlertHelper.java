package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for reusable JavaFX alert dialogs.
 * Provides error, success, warning, info and confirm dialogs.
 */
public class AlertHelper {

    private AlertHelper() {}

    // ─── Error ────────────────────────────────────────────────

    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    // ─── Success ──────────────────────────────────────────────

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅  " + title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    // ─── Info ─────────────────────────────────────────────────

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    // ─── Warning ──────────────────────────────────────────────

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("⚠️  " + title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    // ─── Confirm ──────────────────────────────────────────────

    /**
     * Show a confirmation dialog with OK/Cancel buttons.
     * @return true if user clicked OK, false if cancelled
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ─── Input Validation Error ───────────────────────────────

    /**
     * Show a validation error — used for form field errors.
     */
    public static void showValidationError(String fieldName, String message) {
        showError("Validation Error — " + fieldName, message);
    }

    // ─── Style ────────────────────────────────────────────────

    private static void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle(
            "-fx-background-color: white; "
          + "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
    }
}
