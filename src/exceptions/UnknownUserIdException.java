package exceptions;

/**
 * Thrown when a login is attempted with an email/ID that does not
 * exist in the system. Triggers REST API alert to Admin.
 */
public class UnknownUserIdException extends Exception {

    private final String attemptedId;

    public UnknownUserIdException(String attemptedId) {
        super("No account found for [" + attemptedId + "]. "
            + "This attempt has been flagged and reported to Admin.");
        this.attemptedId = attemptedId;
    }

    public String getAttemptedId() { return attemptedId; }
}
