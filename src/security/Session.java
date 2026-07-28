package security;

import model.Person;
import enums.UserRole;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton class that manages the currently logged-in user session.
 *
 * Only one user can be active at a time on this machine.
 * Multi-profile support is handled by logging out and switching accounts.
 */
public class Session {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── Singleton Instance ───────────────────────────────────
    private static Session instance;

    // ─── Session State ────────────────────────────────────────
    private Person  currentUser;
    private String  loginTime;
    private boolean isLoggedIn;

    // ─── Private Constructor ──────────────────────────────────
    private Session() {
        this.currentUser = null;
        this.loginTime   = null;
        this.isLoggedIn  = false;
    }

    // ─── Singleton Accessor ───────────────────────────────────
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // ─── Login ────────────────────────────────────────────────

    /**
     * Start a session for the given user.
     * Records login time and marks session as active.
     *
     * @param user the authenticated Person object
     * @throws IllegalArgumentException if user is null
     * @throws IllegalStateException    if a session is already active
     */
    public void login(Person user) {
        if (user == null)
            throw new IllegalArgumentException("Cannot start a session with a null user.");
        if (isLoggedIn)
            throw new IllegalStateException(
                "A session is already active for: " + currentUser.getEmail()
                + ". Please log out first.");

        this.currentUser = user;
        this.loginTime   = LocalDateTime.now().format(FORMATTER);
        this.isLoggedIn  = true;
    }

    // ─── Logout ───────────────────────────────────────────────

    /**
     * End the current session and clear all session data.
     *
     * @throws IllegalStateException if no session is currently active
     */
    public void logout() {
        if (!isLoggedIn)
            throw new IllegalStateException("No active session to log out from.");

        AuditLog.getInstance().logLogout(currentUser.getEmail());
        this.currentUser = null;
        this.loginTime   = null;
        this.isLoggedIn  = false;
    }

    // ─── Getters ──────────────────────────────────────────────

    /**
     * Get the currently logged-in user.
     * @throws IllegalStateException if no session is active
     */
    public Person getCurrentUser() {
        if (!isLoggedIn)
            throw new IllegalStateException("No active session. Please log in first.");
        return currentUser;
    }

    public boolean isLoggedIn()  { return isLoggedIn; }
    public String  getLoginTime() { return loginTime; }

    /**
     * Convenience check: is the current user an Admin?
     */
    public boolean isAdmin() {
        return isLoggedIn && currentUser.getUserRole() == UserRole.ADMIN;
    }

    /**
     * Convenience check: is the current user an Agent?
     */
    public boolean isAgent() {
        return isLoggedIn && currentUser.getUserRole() == UserRole.AGENT;
    }

    /**
     * Convenience check: is the current user a Client?
     */
    public boolean isClient() {
        return isLoggedIn && currentUser.getUserRole() == UserRole.CLIENT;
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        if (!isLoggedIn) return "[Session] No active session.";
        return String.format("[Session] User: %s | Role: %s | Login Time: %s",
                currentUser.getEmail(), currentUser.getRole(), loginTime);
    }
}
