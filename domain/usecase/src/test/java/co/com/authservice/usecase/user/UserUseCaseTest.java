package co.com.authservice.usecase.user;

import co.com.authservice.model.role.Role;
import co.com.authservice.model.role.gateways.RoleRepository;
import co.com.authservice.model.user.User;
import co.com.authservice.model.user.exceptions.user.EmailAlreadyExistsException;
import co.com.authservice.model.user.exceptions.user.InvalidAgeException;
import co.com.authservice.model.user.exceptions.user.InvalidSalaryException;
import co.com.authservice.model.user.exceptions.user.UserValidationException;
import co.com.authservice.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUseCase - Business Logic Tests")
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserUseCase userUseCase;

    private User validUser;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .name("Juan")
                .lastname("Pérez")
                .email("juan.perez@email.com")
                .birthdayDate(LocalDate.of(1990, 5, 15)) // 33 años
                .baseSalary(new BigDecimal("5000000"))
                .address("Calle 123")
                .phoneNumber("+57 300 123 4567")
                .build();

        defaultRole = Role.builder()
                .id(1L)
                .name("CLIENT")
                .description("Default client role")
                .build();
    }

    @Nested
    @DisplayName("saveUser - Happy Path Tests")
    class SaveUserHappyPathTests {

        @Test
        @DisplayName("Should save user successfully with all valid data")
        void shouldSaveUserSuccessfully() {
            when(userRepository.existByEmail(anyString())).thenReturn(Mono.just(false));
            when(roleRepository.findByName("CLIENT")).thenReturn(Mono.just(defaultRole));
            when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(validUser.toBuilder().id(1L).role(defaultRole).build()));

            StepVerifier.create(userUseCase.saveUser(validUser))
                    .expectNextMatches(savedUser -> 
                            savedUser.getId() != null && 
                            savedUser.getName().equals("Juan") &&
                            savedUser.getRole() != null &&
                            savedUser.getRole().getName().equals("CLIENT")
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should save user with existing role (not assign default)")
        void shouldSaveUserWithExistingRole() {
            Role adminRole = Role.builder().id(2L).name("ADMIN").description("Admin role").build();
            User userWithRole = validUser.toBuilder().role(adminRole).build();
            
            when(userRepository.existByEmail(anyString())).thenReturn(Mono.just(false));
            when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(userWithRole.toBuilder().id(1L).build()));

            StepVerifier.create(userUseCase.saveUser(userWithRole))
                    .expectNextMatches(savedUser -> 
                            savedUser.getRole().getName().equals("ADMIN")
                    )
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("saveUser - Validation Error Tests")
    class SaveUserValidationErrorTests {

        @Test
        @DisplayName("Should throw UserValidationException when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            User userWithNullName = validUser.toBuilder().name(null).build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    UserValidationException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, userWithNullName);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "Should throw UserValidationException when name is null"
            );
        }

        @Test
        @DisplayName("Should throw UserValidationException when name is blank")
        void shouldThrowExceptionWhenNameIsBlank() {
            User userWithBlankName = validUser.toBuilder().name("   ").build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    UserValidationException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, userWithBlankName);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        @Test
        @DisplayName("Should throw UserValidationException when email format is invalid")
        void shouldThrowExceptionWhenEmailFormatIsInvalid() {
            User userWithInvalidEmail = validUser.toBuilder().email("invalid-email").build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    UserValidationException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, userWithInvalidEmail);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        @Test
        @DisplayName("Should throw UserValidationException when baseSalary is null")
        void shouldThrowExceptionWhenBaseSalaryIsNull() {
            User userWithNullSalary = validUser.toBuilder().baseSalary(null).build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    UserValidationException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, userWithNullSalary);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }

    @Nested
    @DisplayName("saveUser - Age Validation Tests")
    class SaveUserAgeValidationTests {

        @Test
        @DisplayName("Should throw InvalidAgeException when user is under 18")
        void shouldThrowExceptionWhenUserIsUnder18() {
            LocalDate under18Date = LocalDate.now().minusYears(17); // 17 años
            User underageUser = validUser.toBuilder().birthdayDate(under18Date).build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    InvalidAgeException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, underageUser);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        @Test
        @DisplayName("Should save user when exactly 18 years old")
        void shouldSaveUserWhenExactly18YearsOld() {
            LocalDate exactly18Date = LocalDate.now().minusYears(18);
            User exactly18User = validUser.toBuilder().birthdayDate(exactly18Date).build();
            
            when(userRepository.existByEmail(anyString())).thenReturn(Mono.just(false));
            when(roleRepository.findByName("CLIENT")).thenReturn(Mono.just(defaultRole));
            when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(exactly18User.toBuilder().id(1L).role(defaultRole).build()));

            StepVerifier.create(userUseCase.saveUser(exactly18User))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("saveUser - Salary Validation Tests")
    class SaveUserSalaryValidationTests {

        @Test
        @DisplayName("Should throw InvalidSalaryException when salary is negative")
        void shouldThrowExceptionWhenSalaryIsNegative() {
            User userWithNegativeSalary = validUser.toBuilder()
                    .baseSalary(new BigDecimal("-1000"))
                    .build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    InvalidSalaryException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, userWithNegativeSalary);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        @Test
        @DisplayName("Should throw InvalidSalaryException when salary exceeds maximum")
        void shouldThrowExceptionWhenSalaryExceedsMaximum() {
            User userWithHighSalary = validUser.toBuilder()
                    .baseSalary(new BigDecimal("15000001")) // Excede el máximo
                    .build();

            org.junit.jupiter.api.Assertions.assertThrows(
                    InvalidSalaryException.class,
                    () -> {
                        try {
                            java.lang.reflect.Method validateMethod = UserUseCase.class
                                    .getDeclaredMethod("validateUserBusinessRules", User.class);
                            validateMethod.setAccessible(true);
                            validateMethod.invoke(userUseCase, userWithHighSalary);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw (RuntimeException) e.getCause();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }

        @Test
        @DisplayName("Should save user with zero salary")
        void shouldSaveUserWithZeroSalary() {
            User userWithZeroSalary = validUser.toBuilder()
                    .baseSalary(BigDecimal.ZERO)
                    .build();
            
            when(userRepository.existByEmail(anyString())).thenReturn(Mono.just(false));
            when(roleRepository.findByName("CLIENT")).thenReturn(Mono.just(defaultRole));
            when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(userWithZeroSalary.toBuilder().id(1L).role(defaultRole).build()));

            StepVerifier.create(userUseCase.saveUser(userWithZeroSalary))
                    .expectNextCount(1)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should save user with maximum allowed salary")
        void shouldSaveUserWithMaximumAllowedSalary() {
            User userWithMaxSalary = validUser.toBuilder()
                    .baseSalary(new BigDecimal("15000000"))
                    .build();
            
            when(userRepository.existByEmail(anyString())).thenReturn(Mono.just(false));
            when(roleRepository.findByName("CLIENT")).thenReturn(Mono.just(defaultRole));
            when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(userWithMaxSalary.toBuilder().id(1L).role(defaultRole).build()));

            StepVerifier.create(userUseCase.saveUser(userWithMaxSalary))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("saveUser - Email Uniqueness Tests")
    class SaveUserEmailUniquenessTests {

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            when(userRepository.existByEmail("juan.perez@email.com")).thenReturn(Mono.just(true));
            when(roleRepository.findByName("CLIENT")).thenReturn(Mono.just(defaultRole));

            StepVerifier.create(userUseCase.saveUser(validUser))
                    .expectError(EmailAlreadyExistsException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("getAll - Retrieve Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users successfully")
        void shouldReturnAllUsersSuccessfully() {
            List<User> users = List.of(
                    validUser.toBuilder().id(1L).build(),
                    validUser.toBuilder().id(2L).email("other@email.com").build()
            );
            when(userRepository.getAll()).thenReturn(Flux.fromIterable(users));

            StepVerifier.create(userUseCase.getAll())
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty flux when no users exist")
        void shouldReturnEmptyFluxWhenNoUsersExist() {
            when(userRepository.getAll()).thenReturn(Flux.empty());

            StepVerifier.create(userUseCase.getAll())
                    .expectNextCount(0)
                    .verifyComplete();
        }
    }
}
