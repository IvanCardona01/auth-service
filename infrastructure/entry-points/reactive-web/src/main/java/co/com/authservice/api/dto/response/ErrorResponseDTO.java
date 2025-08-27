package co.com.authservice.api.dto.response;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String code,
        String message,
        LocalDateTime timestamp,
        String path
) {
    public static ErrorResponseDTO of(String code, String message, String path) {
        return new ErrorResponseDTO(code, message, LocalDateTime.now(), path);
    }

    public static ErrorResponseDTO of(String code, String message) {
        return new ErrorResponseDTO(code, message, LocalDateTime.now(), null);
    }
}
