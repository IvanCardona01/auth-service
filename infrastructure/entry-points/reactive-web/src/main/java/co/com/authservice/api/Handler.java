package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.usecase.user.UserUseCase;
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
    private final UserDTOMapper userDTOMapper;
    private final Validator validator;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.info("🔵 [REQUEST] Creating user with email: {}", dto.email()))
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
}
