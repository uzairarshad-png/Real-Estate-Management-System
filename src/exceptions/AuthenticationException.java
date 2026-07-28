package exceptions;

/**
 * Thrown when login credentials are incorrect or a duplicate
 * email is used during registration.
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }
}
