package exceptions;

/**
 * Thrown when a user who registered from the Login Page tries to log in
 * before Admin has approved their registration request.
 */
public class PendingApprovalException extends Exception {

    private final String email;

    public PendingApprovalException(String email) {
        super("Account [" + email + "] is pending Admin approval. "
            + "You will be notified once your account is activated.");
        this.email = email;
    }

    public String getEmail() { return email; }
}
