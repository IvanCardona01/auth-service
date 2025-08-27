package co.com.authservice.usecase.user;

import co.com.authservice.model.user.User;
import co.com.authservice.model.user.exceptions.user.EmailAlreadyExistsException;
import co.com.authservice.model.user.exceptions.user.InvalidAgeException;
import co.com.authservice.model.user.exceptions.user.InvalidSalaryException;
import co.com.authservice.model.user.exceptions.user.UserValidationException;
import co.com.authservice.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@RequiredArgsConstructor
public class UserUseCase {
    private static final BigDecimal MIN_SALARY = BigDecimal.ZERO;
    private static final BigDecimal MAX_SALARY = new BigDecimal("15000000");

    private final UserRepository userRepository;

    public Mono<User> saveUser(User user) {
        return Mono.fromRunnable(() -> validateUserBusinessRules(user))
                .then(validateIfEmailAlreadyInUse(user.getEmail()))
                .then(userRepository.saveUser(user));
    }

    public Flux<User> getAll() {
        return userRepository.getAll();
    }

    private void validateUserBusinessRules(User user) {
        validateFields(user);
        validateAge(user);
        validateSalary(user.getBaseSalary());
    }

    private void validateFields(User user) {
        requireNonBlank(user.getName(), "name");
        requireNonBlank(user.getLastname(), "lastname");
        requireNonBlank(user.getEmail(), "email");

        if (user.getBaseSalary() == null) {
            throw new UserValidationException("baseSalary", "baseSalary is required");
        }

        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!user.getEmail().matches(regex)) {
            throw new UserValidationException("email", "email format is invalid");
        }
    }

    private void requireNonBlank(String fieldValue, String fieldName) {
        if (fieldValue == null || fieldValue.isBlank()) {
            throw new UserValidationException(fieldName, fieldName + " is required");
        }
    }

    private void validateAge(User user) {
        if (user.getBirthdayDate() != null) {
            int age = Period.between(user.getBirthdayDate(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new InvalidAgeException(age);
            }
        }
    }

    private void validateSalary(BigDecimal salary) {
        if (salary.compareTo(MIN_SALARY) < 0) {
            throw InvalidSalaryException.tooLow(salary);
        }

        if (salary.compareTo(MAX_SALARY) > 0) {
            throw InvalidSalaryException.tooHigh(salary);
        }
    }

    private Mono<Void> validateIfEmailAlreadyInUse(String email) {
        return userRepository.existByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new EmailAlreadyExistsException(email));
                    }
                    return Mono.empty();
                });
    }
}
