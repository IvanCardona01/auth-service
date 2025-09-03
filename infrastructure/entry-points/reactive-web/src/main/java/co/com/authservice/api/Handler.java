package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.dto.request.LoginRequestDTO;
import co.com.authservice.api.dto.response.LoginResponseDTO;
import co.com.authservice.api.dto.response.UserSummaryDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.api.security.AuthorizationUtils;
import co.com.authservice.jwt.JWTTokenServiceImpl;
import co.com.authservice.model.user.User;
import co.com.authservice.usecase.auth.LoginUseCase;
import co.com.authservice.usecase.role.RoleUseCase;
import co.com.authservice.usecase.user.UserUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final UserUseCase userUseCase;
    private final LoginUseCase loginUseCase;
    private final RoleUseCase roleUseCase;
    private final UserDTOMapper userDTOMapper;
    private final JWTTokenServiceImpl jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return AuthorizationUtils.getAuthenticatedUser(request.exchange())
                .flatMap(AuthorizationUtils::requireAdminOrAdvisorRole)
                .doOnNext(authUser -> log.info("🔐 [AUTH] User {} (role: {}) authorized to create users", 
                        authUser.getEmail(), authUser.getRole().getName()))
                .then(request.bodyToMono(CreateUserDTO.class))
                .doOnNext(dto -> log.info("🔵 [REQUEST] Creating user with email: {} and roleId: {}", dto.email(), dto.roleId()))
                .flatMap(dto -> {
                    User user = userDTOMapper.toModel(dto);
                    if (user.getPassword() != null) {
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    if (dto.roleId() != null) {
                        return userUseCase.saveUserWithRole(user, dto.roleId());
                    } else {
                        return userUseCase.saveUser(user);
                    }
                })
                .doOnNext(user -> log.info("✅ [RESPONSE] User created successfully with ID: {} and role: {}", 
                        user.getId(), user.getRole() != null ? user.getRole().getName() : "DEFAULT"))
                .flatMap(savedUser -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(savedUser)))
                .doOnError(error -> log.error("❌ [ERROR] Failed to create user: {}", error.getMessage()));
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return userUseCase.getAll()
                .map(userDTOMapper::toResponse)
                .collectList()
                .doOnNext(users -> log.info("✅ [RESPONSE] Retrieved {} users successfully", users.size()))
                .flatMap(users -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(users))
                .doOnError(error -> log.error("❌ [ERROR] Failed to retrieve users: {}", error.getMessage()));
    }

    public Mono<ServerResponse> getUserByDocumentNumber(ServerRequest serverRequest) {
        String documentNumber = serverRequest.pathVariable("documentNumber");

        log.info("✅ [PATH] Getting user with Document Number: {}", documentNumber);

        return userUseCase.getByDocumentNumber(documentNumber)
                .doOnNext(user -> log.info("✅ [RESPONSE] User retrieved successfully with ID: {}", user.getId()))
                .flatMap(user -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(user)))
                .doOnError(error -> log.error("❌ [ERROR] Failed to retrieve user: {}", error.getMessage()));
    }
    
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequestDTO.class)
                .doOnNext(dto -> log.info("🔑 [REQUEST] Login attempt for email: {}", dto.email()))
                .flatMap(dto -> loginUseCase.validateUserCredentials(dto.email(), dto.password())
                        .flatMap(user -> validatePassword(dto.password(), user))
                        .map(user -> {
                            String token = jwtTokenService.generateToken(user);
                            UserSummaryDTO userSummary = new UserSummaryDTO(
                                    user.getId(),
                                    user.getName() + " " + user.getLastname(),
                                    user.getEmail(),
                                    userDTOMapper.toResponse(user.getRole())
                            );
                            return new LoginResponseDTO(token, jwtTokenService.getExpirationTime(), userSummary);
                        })
                )
                .doOnNext(response -> log.info("✅ [RESPONSE] Login successful for user: {}", response.user().email()))
                .flatMap(loginResponse -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loginResponse))
                .doOnError(error -> log.error("❌ [ERROR] Login failed: {}", error.getMessage()));
    }
    
    public Mono<ServerResponse> getAllRoles(ServerRequest request) {
        return roleUseCase.getAllRoles()
                .map(userDTOMapper::toResponse)
                .collectList()
                .doOnNext(roles -> log.info("✅ [RESPONSE] Retrieved {} roles successfully", roles.size()))
                .flatMap(roles -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(roles))
                .doOnError(error -> log.error("❌ [ERROR] Failed to retrieve roles: {}", error.getMessage()));
    }
    
    private Mono<User> validatePassword(String rawPassword, User user) {
        return Mono.fromCallable(() -> {
            if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new IllegalArgumentException("Invalid credentials");
            }
            return user;
        });
    }
}
