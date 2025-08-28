package co.com.authservice.api.config;

import co.com.authservice.api.dto.response.ErrorResponseDTO;
import co.com.authservice.model.user.exceptions.user.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        ServerRequest request = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        log.error("🚨 Exception caught: {} - Message: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        
        ErrorInfo errorInfo = determineError(ex);

        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
                errorInfo.code(),
                errorInfo.message(),
                request.path()
        );

        response.setStatusCode(errorInfo.status());
        response.getHeaders().add("Content-Type", "application/json");

        DataBuffer buffer = response.bufferFactory().wrap(serializeError(errorResponse));
        return response.writeWith(Mono.just(buffer));
    }

    private ErrorInfo determineError(Throwable ex) {
        return switch (ex) {
            case UserNotFoundException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", e.getMessage());
            case EmailAlreadyExistsException e ->
                    new ErrorInfo(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", e.getMessage());
            case InvalidAgeException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "INVALID_AGE", e.getMessage());
            case InvalidSalaryException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "INVALID_SALARY", e.getMessage());
            case UserValidationException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "USER_VALIDATION_ERROR", e.getMessage());
            case WebExchangeBindException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", extractValidationMessage(e));
            case ServerWebInputException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid request data: " + e.getMessage());
            case DecodingException e ->
                    new ErrorInfo(HttpStatus.BAD_REQUEST, "INVALID_JSON", "Invalid JSON format: " + e.getMessage());
            case MethodNotAllowedException e ->
                    new ErrorInfo(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Method not supported");

            default ->
                    new ErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred");
        };
    }

    private String extractValidationMessage(WebExchangeBindException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }

    private byte[] serializeError(ErrorResponseDTO error) {
        try {
            return objectMapper.writeValueAsBytes(error);
        } catch (Exception e) {

            //Fallback
            String fallback = """
                {
                    "code": "SERIALIZATION_ERROR",
                    "message": "Error serializing response",
                    "timestamp": "%s"
                }
                """.formatted(LocalDateTime.now());
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }

    private record ErrorInfo(HttpStatus status, String code, String message) {}
}
