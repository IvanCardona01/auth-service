package co.com.authservice.model.user.exceptions.user;

public class UserValidationException extends RuntimeException {
    public UserValidationException(String field, String reason) {
        super("Invalid " + field + ": " + reason);
    }
}
