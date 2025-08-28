package co.com.authservice.usecase.user;

import co.com.authservice.model.role.gateways.RoleRepository;
import co.com.authservice.model.user.User;
import co.com.authservice.model.user.exceptions.user.*;
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
    private final RoleRepository roleRepository;

    public Flux<User> getAll() {
        return userRepository.getAll();
    }

    public Mono<User> getByDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isEmpty()) {
            return Mono.error(new UserValidationException("documentNumber", "The Document Number is required"));
        }

        return userRepository.existByDocumentNumber(documentNumber)
                .flatMap(exist -> {
                    if (exist) {
                        return userRepository.getByDocumentNumber(documentNumber);
                    }
                    return Mono.error(new UserNotFoundException("User not found"));
                });
    }

    public Mono<User> saveUser(User user) {
        return validateUserBusinessRules(user)
                .then(validateIfEmailAlreadyInUse(user.getEmail()))
                .then(validateIfDocumentNumberAlreadyInUse(user.getDocumentNumber()))
                .then(assignDefaultRoleIfNeeded(user))
                .flatMap(userRepository::saveUser);
    }

    private Mono<Void> validateUserBusinessRules(User user) {
        return validateFields(user)
                .then(validateAge(user))
                .then(validateSalary(user.getBaseSalary()));
    }

    private Mono<Void> validateFields(User user) {
        if (isBlank(user.getName())) {
            return Mono.error(new UserValidationException("name", "name is required"));
        }
        if (isBlank(user.getLastname())) {
            return Mono.error(new UserValidationException("lastname", "lastname is required"));
        }
        if (isBlank(user.getEmail())) {
            return Mono.error(new UserValidationException("email", "email is required"));
        }
        if (user.getBaseSalary() == null) {
            return Mono.error(new UserValidationException("baseSalary", "baseSalary is required"));
        }

        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!user.getEmail().matches(regex)) {
            return Mono.error(new UserValidationException("email", "email format is invalid"));
        }

        return Mono.empty();
    }

    private Mono<Void> validateAge(User user) {
        if (user.getBirthdayDate() != null) {
            int age = Period.between(user.getBirthdayDate(), LocalDate.now()).getYears();
            if (age < 18) {
                return Mono.error(new InvalidAgeException(age));
            }
        }
        return Mono.empty();
    }

    private Mono<Void> validateSalary(BigDecimal salary) {
        if (salary.compareTo(MIN_SALARY) < 0) {
            return Mono.error(InvalidSalaryException.tooLow(salary));
        }
        if (salary.compareTo(MAX_SALARY) > 0) {
            return Mono.error(InvalidSalaryException.tooHigh(salary));
        }
        return Mono.empty();
    }

    private Mono<Void> validateIfEmailAlreadyInUse(String email) {
        return userRepository.existByEmail(email)
                .flatMap(exists -> exists
                        ? Mono.error(new EmailAlreadyExistsException(email))
                        : Mono.empty()
                );
    }

    private Mono<Void> validateIfDocumentNumberAlreadyInUse(String documentNumber) {
        if (isBlank(documentNumber)) {
            return Mono.error(new UserValidationException("documentNumber", "documentNumber is required"));
        }
        return userRepository.existByDocumentNumber(documentNumber)
                .flatMap(exists -> exists
                        ? Mono.error(new UserValidationException("documentNumber", "documentNumber already exists"))
                        : Mono.empty()
                );
    }

    private Mono<User> assignDefaultRoleIfNeeded(User user) {
        if (user.getRole() != null) {
            return Mono.just(user);
        }

        return roleRepository.findByName("CLIENT")
                .doOnNext(user::setRole)
                .thenReturn(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
