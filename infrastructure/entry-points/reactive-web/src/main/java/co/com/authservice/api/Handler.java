package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;
    private final Validator validator;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.info("🔵 [REQUEST] Creating user with email: {}", dto.email()))
                .flatMap(this::validateDTO)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::saveUser)
                .doOnNext(user -> log.info("✅ [RESPONSE] User created successfully with ID: {}", user.getId()))
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

    private Mono<CreateUserDTO> validateDTO(CreateUserDTO dto) {
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            return Mono.error(new ValidationException(errorMessage));
        }
        return Mono.just(dto);
    }
}
