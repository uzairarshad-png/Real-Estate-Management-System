package exceptions;

/**
 * Thrown when a user attempts to log in but their account has been
 * locked after 3 failed attempts and requires Admin approval to unlock.
 */
public class AccountLockedException extends Exception {

    private final String email;

    public AccountLockedException(String email) {
        super("Account [" + email + "] is locked. Please contact Admin to unlock your account.");
        this.email = email;
    }

    public String getEmail() { return email; }
}
