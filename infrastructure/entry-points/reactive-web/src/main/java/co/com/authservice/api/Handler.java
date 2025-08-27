package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Handler {
    private final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;
    private final Validator validator;

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserDTO.class)
                .flatMap(this::validateDTO)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::saveUser)
                .flatMap(savedUser -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userDTOMapper.toResponse(savedUser)));
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return userUseCase.getAll()
                .map(userDTOMapper::toResponse)
                .collectList()
                .flatMap(users -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(users));
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
