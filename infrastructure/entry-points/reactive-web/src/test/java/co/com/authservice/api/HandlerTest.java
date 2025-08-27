package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.dto.response.UserResponseDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.model.role.Role;
import co.com.authservice.model.user.User;
import co.com.authservice.model.user.exceptions.user.EmailAlreadyExistsException;
import co.com.authservice.model.user.exceptions.user.InvalidAgeException;
import co.com.authservice.model.user.exceptions.user.InvalidSalaryException;
import co.com.authservice.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler - Web Entry Point Tests")
class HandlerTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private UserDTOMapper userDTOMapper;

    @Mock
    private jakarta.validation.Validator validator;

    @InjectMocks
    private Handler handler;

    private CreateUserDTO validCreateUserDTO;
    private User domainUser;
    private UserResponseDTO userResponseDTO;
    private Role defaultRole;

    @BeforeEach
    void setUp() {
        validCreateUserDTO = new CreateUserDTO(
                "Juan",
                "Pérez",
                LocalDate.of(1990, 5, 15),
                "Calle 123",
                "+57 300 123 4567",
                new BigDecimal("5000000"),
                "juan.perez@email.com"
        );

        defaultRole = Role.builder()
                .id(1L)
                .name("CLIENT")
                .description("Default client role")
                .build();

        domainUser = User.builder()
                .id(1L)
                .name("Juan")
                .lastname("Pérez")
                .email("juan.perez@email.com")
                .birthdayDate(LocalDate.of(1990, 5, 15))
                .baseSalary(new BigDecimal("5000000"))
                .address("Calle 123")
                .phoneNumber("+57 300 123 4567")
                .role(defaultRole)
                .build();

        userResponseDTO = new UserResponseDTO(
                1L,
                "Juan",
                "Pérez",
                LocalDate.of(1990, 5, 15),
                "Calle 123",
                "+57 300 123 4567",
                new BigDecimal("5000000"),
                "juan.perez@email.com",
                null // Role será seteado según el caso
        );
    }

    @Nested
    @DisplayName("createUser - Happy Path Tests")
    class CreateUserHappyPathTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfullyWithValidData() {
            ServerRequest request = MockServerRequest.builder()
                    .body(Mono.just(validCreateUserDTO));

            Set<ConstraintViolation<CreateUserDTO>> emptyViolations = Collections.emptySet();
            when(validator.validate(any(CreateUserDTO.class))).thenReturn(emptyViolations);
            when(userDTOMapper.toModel(validCreateUserDTO)).thenReturn(domainUser);
            when(userUseCase.saveUser(domainUser)).thenReturn(Mono.just(domainUser));
            when(userDTOMapper.toResponse(domainUser)).thenReturn(userResponseDTO);

            Mono<ServerResponse> result = handler.createUser(request);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                        // Verificamos headers de contenido
                        assertThat(response.headers().getFirst("Content-Type"))
                                .contains(MediaType.APPLICATION_JSON_VALUE);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("createUser - Business Logic Error Tests")
    class CreateUserBusinessLogicErrorTests {

        @Test
        @DisplayName("Should propagate InvalidAgeException from use case")
        void shouldPropagateInvalidAgeExceptionFromUseCase() {
            ServerRequest request = MockServerRequest.builder()
                    .body(Mono.just(validCreateUserDTO));

            Set<ConstraintViolation<CreateUserDTO>> emptyViolations = Collections.emptySet();
            when(validator.validate(any(CreateUserDTO.class))).thenReturn(emptyViolations);
            when(userDTOMapper.toModel(validCreateUserDTO)).thenReturn(domainUser);
            when(userUseCase.saveUser(domainUser))
                    .thenReturn(Mono.error(new InvalidAgeException(16)));

            StepVerifier.create(handler.createUser(request))
                    .expectError(InvalidAgeException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should propagate EmailAlreadyExistsException from use case")
        void shouldPropagateEmailAlreadyExistsExceptionFromUseCase() {
            ServerRequest request = MockServerRequest.builder()
                    .body(Mono.just(validCreateUserDTO));

            Set<ConstraintViolation<CreateUserDTO>> emptyViolations = Collections.emptySet();
            when(validator.validate(any(CreateUserDTO.class))).thenReturn(emptyViolations);
            when(userDTOMapper.toModel(validCreateUserDTO)).thenReturn(domainUser);
            when(userUseCase.saveUser(domainUser))
                    .thenReturn(Mono.error(new EmailAlreadyExistsException("juan.perez@email.com")));

            StepVerifier.create(handler.createUser(request))
                    .expectError(EmailAlreadyExistsException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should propagate InvalidSalaryException from use case")
        void shouldPropagateInvalidSalaryExceptionFromUseCase() {
            ServerRequest request = MockServerRequest.builder()
                    .body(Mono.just(validCreateUserDTO));

            Set<ConstraintViolation<CreateUserDTO>> emptyViolations = Collections.emptySet();
            when(validator.validate(any(CreateUserDTO.class))).thenReturn(emptyViolations);
            when(userDTOMapper.toModel(validCreateUserDTO)).thenReturn(domainUser);
            when(userUseCase.saveUser(domainUser))
                    .thenReturn(Mono.error(InvalidSalaryException.tooHigh(new BigDecimal("20000000"))));

            StepVerifier.create(handler.createUser(request))
                    .expectError(InvalidSalaryException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("getAllUsers - Retrieve Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users successfully")
        void shouldReturnAllUsersSuccessfully() {
            ServerRequest request = MockServerRequest.builder().build();

            List<User> users = List.of(
                    domainUser,
                    domainUser.toBuilder().id(2L).email("otro@email.com").build()
            );

            List<UserResponseDTO> userResponses = List.of(
                    userResponseDTO,
                    new UserResponseDTO(2L, "Juan", "Pérez", LocalDate.of(1990, 5, 15),
                            "Calle 123", "+57 300 123 4567", new BigDecimal("5000000"),
                            "otro@email.com", null)
            );

            when(userUseCase.getAll()).thenReturn(Flux.fromIterable(users));
            when(userDTOMapper.toResponse(users.get(0))).thenReturn(userResponses.get(0));
            when(userDTOMapper.toResponse(users.get(1))).thenReturn(userResponses.get(1));

            Mono<ServerResponse> result = handler.getAllUsers(request);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                        assertThat(response.headers().getFirst("Content-Type"))
                                .contains(MediaType.APPLICATION_JSON_VALUE);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsersExist() {
            ServerRequest request = MockServerRequest.builder().build();
            when(userUseCase.getAll()).thenReturn(Flux.empty());

            Mono<ServerResponse> result = handler.getAllUsers(request);

            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should propagate use case errors")
        void shouldPropagateUseCaseErrors() {
            ServerRequest request = MockServerRequest.builder().build();
            when(userUseCase.getAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

            StepVerifier.create(handler.getAllUsers(request))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Database error")
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("Request Processing Tests")
    class RequestProcessingTests {

        @Test
        @DisplayName("Should handle malformed JSON in request body")
        void shouldHandleMalformedJsonInRequestBody() {
            ServerRequest request = MockServerRequest.builder()
                    .body(Mono.error(new RuntimeException("Invalid JSON format")));

            StepVerifier.create(handler.createUser(request))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().contains("Invalid JSON format")
                    )
                    .verify();
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() {
            ServerRequest request = MockServerRequest.builder()
                    .body(Mono.error(new RuntimeException("Required request body is missing")));

            StepVerifier.create(handler.createUser(request))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().contains("Required request body is missing")
                    )
                    .verify();
        }
    }

}
