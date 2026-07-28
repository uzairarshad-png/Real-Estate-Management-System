package gui;

/**
 * Centralized inline CSS styles for all REMS JavaFX screens.
 * Implements a premium design system entirely in Java.
 */
public class StyleManager {

    // ─── Colour Palette (Modern & Professional) ────────────────
    public static final String COLOR_PRIMARY      = "#3f51b5";   // Indigo
    public static final String COLOR_PRIMARY_DARK = "#303f9f";   // Dark Indigo
    public static final String COLOR_ACCENT       = "#ff4081";   // Pink Accent
    public static final String COLOR_ACCENT_DARK  = "#f50057";   // Darker Pink
    public static final String COLOR_SUCCESS      = "#4caf50";   // Material Green
    public static final String COLOR_DANGER       = "#f44336";   // Material Red
    public static final String COLOR_WARNING      = "#ff9800";   // Material Orange
    public static final String COLOR_INFO         = "#2196f3";   // Material Blue
    public static final String COLOR_BG           = "#f4f7f6";   // Soft Grey/White BG
    public static final String COLOR_SURFACE      = "#ffffff";   // Pure White
    public static final String COLOR_TEXT         = "#2c3e50";   // Dark Blue-Grey Text
    public static final String COLOR_TEXT_LIGHT   = "#7f8c8d";   // Soft Grey Text
    public static final String COLOR_BORDER       = "#dcdde1";   // Light Border

    // Prevent instantiation
    private StyleManager() {}

    // ─── Button Styles (Premium Gradients) ─────────────────────

    public static String primaryButton() {
        return "-fx-background-color: linear-gradient(to bottom, " + COLOR_PRIMARY + ", " + COLOR_PRIMARY_DARK + "); "
             + "-fx-text-fill: white; "
             + "-fx-font-size: 14px; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 12 28; "
             + "-fx-background-radius: 8; "
             + "-fx-effect: dropshadow(gaussian, rgba(63, 81, 181, 0.3), 10, 0, 0, 4); "
             + "-fx-cursor: hand;";
    }

    public static String primaryButtonHover() {
        return "-fx-background-color: linear-gradient(to bottom, #5c6bc0, #3f51b5); "
             + "-fx-text-fill: white; "
             + "-fx-font-size: 14px; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 12 28; "
             + "-fx-background-radius: 8; "
             + "-fx-effect: dropshadow(gaussian, rgba(63, 81, 181, 0.4), 12, 0, 0, 5); "
             + "-fx-cursor: hand;";
    }

    public static String accentButton() {
        return "-fx-background-color: linear-gradient(to bottom, " + COLOR_ACCENT + ", " + COLOR_ACCENT_DARK + "); "
             + "-fx-text-fill: white; "
             + "-fx-font-size: 14px; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 12 28; "
             + "-fx-background-radius: 8; "
             + "-fx-effect: dropshadow(gaussian, rgba(255, 64, 129, 0.3), 10, 0, 0, 4); "
             + "-fx-cursor: hand;";
    }

    public static String dangerButton() {
        return "-fx-background-color: " + COLOR_DANGER + "; "
             + "-fx-text-fill: white; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 10 24; "
             + "-fx-background-radius: 8; "
             + "-fx-cursor: hand;";
    }

    public static String warningButton() {
        return "-fx-background-color: " + COLOR_WARNING + "; "
             + "-fx-text-fill: white; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 10 24; "
             + "-fx-background-radius: 8; "
             + "-fx-cursor: hand;";
    }

    public static String successButton() {
        return "-fx-background-color: " + COLOR_SUCCESS + "; "
             + "-fx-text-fill: white; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 10 24; "
             + "-fx-background-radius: 8; "
             + "-fx-cursor: hand;";
    }

    public static String infoButton() {
        return "-fx-background-color: " + COLOR_INFO + "; "
             + "-fx-text-fill: white; "
             + "-fx-font-weight: bold; "
             + "-fx-padding: 10 24; "
             + "-fx-background-radius: 8; "
             + "-fx-cursor: hand;";
    }

    public static String secondaryButton() {
        return "-fx-background-color: white; "
             + "-fx-text-fill: " + COLOR_PRIMARY + "; "
             + "-fx-border-color: " + COLOR_PRIMARY + "; "
             + "-fx-border-radius: 8; "
             + "-fx-background-radius: 8; "
             + "-fx-padding: 10 24; "
             + "-fx-font-weight: bold; "
             + "-fx-cursor: hand;";
    }

    public static String linkButton() {
        return "-fx-background-color: transparent; "
             + "-fx-text-fill: " + COLOR_PRIMARY + "; "
             + "-fx-font-size: 13px; "
             + "-fx-cursor: hand; "
             + "-fx-underline: false;";
    }

    // ─── TextField Styles ─────────────────────────────────────

    public static String textField() {
        return "-fx-background-color: white; "
             + "-fx-border-color: " + COLOR_BORDER + "; "
             + "-fx-border-radius: 8; "
             + "-fx-background-radius: 8; "
             + "-fx-padding: 10 15; "
             + "-fx-font-size: 14px; "
             + "-fx-pref-height: 44px;";
    }

    public static String textFieldFocused() {
        return "-fx-background-color: white; "
             + "-fx-border-color: " + COLOR_PRIMARY + "; "
             + "-fx-border-width: 2; "
             + "-fx-border-radius: 8; "
             + "-fx-background-radius: 8; "
             + "-fx-padding: 9 14; " // Slightly less padding to account for thicker border
             + "-fx-font-size: 14px; "
             + "-fx-pref-height: 44px;";
    }

    public static String textFieldError() {
        return "-fx-background-color: #fff8f8; "
             + "-fx-border-color: " + COLOR_DANGER + "; "
             + "-fx-border-radius: 8; "
             + "-fx-background-radius: 8; "
             + "-fx-padding: 10 15; "
             + "-fx-font-size: 14px;";
    }

    // ─── Label Styles ─────────────────────────────────────────

    public static String titleLabel() {
        return "-fx-font-size: 28px; "
             + "-fx-font-weight: bold; "
             + "-fx-text-fill: " + COLOR_TEXT + "; "
             + "-fx-font-family: 'Segoe UI', system-ui, sans-serif;";
    }

    public static String sectionLabel() {
        return "-fx-font-size: 18px; "
             + "-fx-font-weight: bold; "
             + "-fx-text-fill: " + COLOR_TEXT + ";";
    }

    public static String fieldLabel() {
        return "-fx-font-size: 13px; "
             + "-fx-font-weight: bold; "
             + "-fx-text-fill: " + COLOR_TEXT_LIGHT + "; "
             + "-fx-padding: 0 0 4 0;";
    }

    public static String errorLabel() {
        return "-fx-font-size: 12px; "
             + "-fx-text-fill: " + COLOR_DANGER + ";";
    }

    public static String successLabel() {
        return "-fx-font-size: 12px; "
             + "-fx-text-fill: " + COLOR_SUCCESS + ";";
    }

    public static String subtitleLabel() {
        return "-fx-font-size: 14px; "
             + "-fx-text-fill: " + COLOR_TEXT_LIGHT + ";";
    }

    // ─── Card / Panel Styles ──────────────────────────────────

    public static String card() {
        return "-fx-background-color: white; "
             + "-fx-background-radius: 16; "
             + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 10); "
             + "-fx-padding: 32;";
    }

    public static String glassCard() {
        return "-fx-background-color: rgba(255, 255, 255, 0.85); "
             + "-fx-background-radius: 20; "
             + "-fx-border-color: rgba(255, 255, 255, 0.3); "
             + "-fx-border-radius: 20; "
             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 15);";
    }

    public static String sidebar() {
        return "-fx-background-color: #2c3e50; " // Deep navy sidebar
             + "-fx-pref-width: 260px;";
    }

    public static String sidebarButton() {
        return "-fx-background-color: transparent; "
             + "-fx-text-fill: #bdc3c7; "
             + "-fx-font-size: 14px; "
             + "-fx-alignment: CENTER_LEFT; "
             + "-fx-padding: 15 25; "
             + "-fx-pref-width: 260px; "
             + "-fx-cursor: hand;";
    }

    public static String sidebarButtonActive() {
        return "-fx-background-color: #34495e; "
             + "-fx-text-fill: white; "
             + "-fx-font-size: 14px; "
             + "-fx-font-weight: bold; "
             + "-fx-alignment: CENTER_LEFT; "
             + "-fx-padding: 15 25; "
             + "-fx-border-color: transparent transparent transparent " + COLOR_ACCENT + "; "
             + "-fx-border-width: 0 0 0 4; "
             + "-fx-pref-width: 260px; "
             + "-fx-cursor: hand;";
    }

    public static String topBar() {
        return "-fx-background-color: white; "
             + "-fx-padding: 0 30; "
             + "-fx-pref-height: 70px; "
             + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);";
    }

    public static String mainContent() {
        return "-fx-background-color: " + COLOR_BG + "; "
             + "-fx-padding: 40;";
    }

    // ─── Table Styles ─────────────────────────────────────────

    public static String tableView() {
        return "-fx-background-color: transparent; "
             + "-fx-border-color: " + COLOR_BORDER + "; "
             + "-fx-border-radius: 12; "
             + "-fx-background-radius: 12;";
    }

    // ─── Status Chip Styles (Enhanced) ────────────────────────

    public static String chipActive() {
        return "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; "
             + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold; -fx-font-size: 11px;";
    }

    public static String chipLocked() {
        return "-fx-background-color: #ffebee; -fx-text-fill: #c62828; "
             + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold; -fx-font-size: 11px;";
    }

    public static String chipPending() {
        return "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00; "
             + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold; -fx-font-size: 11px;";
    }

    public static String chipAvailable() {
        return "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; "
             + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-font-weight: bold; -fx-font-size: 11px;";
    }

    // ─── Form Elements ────────────────────────────────────────

    public static String comboBox() {
        return "-fx-background-color: white; "
             + "-fx-border-color: " + COLOR_BORDER + "; "
             + "-fx-border-radius: 8; "
             + "-fx-background-radius: 8; "
             + "-fx-pref-height: 44px; "
             + "-fx-font-size: 14px;";
    }

    public static String scrollPane() {
        return "-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0;";
    }
}
