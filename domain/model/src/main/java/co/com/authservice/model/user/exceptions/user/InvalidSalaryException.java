package co.com.authservice.model.user.exceptions.user;

import java.math.BigDecimal;

public class InvalidSalaryException extends RuntimeException {
    private InvalidSalaryException(String message) {
        super(message);
    }

    public static InvalidSalaryException tooLow(BigDecimal salary) {
        return new InvalidSalaryException("Base salary must be greater or equal than zero, but was " + salary);
    }

    public static InvalidSalaryException tooHigh(BigDecimal salary) {
        return new InvalidSalaryException("Base salary must not exceed 15,000,000.00, but was " + salary);
    }
}
