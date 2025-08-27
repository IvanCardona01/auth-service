package co.com.authservice.model.user.exceptions.user;

public class InvalidAgeException extends RuntimeException {
    public InvalidAgeException(int age) {
        super("User must be at least 18 years old, but was " + age);
    }
}
