package co.com.authservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO for creating a new user")
public record CreateUserDTO(
        @Schema(description = "User's first name", example = "Juan", required = true)
        String name,
        
        @Schema(description = "User's last name", example = "Pérez", required = true)
        String lastname,
        
        @Schema(description = "Date of birth (user must be at least 18 years old)", example = "1990-05-15", required = true)
        LocalDate birthdayDate,
        
        @Schema(description = "User's address", example = "Calle 123 #45-67")
        String address,
        
        @Schema(description = "Phone number", example = "+57 300 123 4567")
        String phoneNumber,
        
        @Schema(description = "Base salary (must be between 0 and 15,000,000)", example = "5000000.00", required = true)
        BigDecimal baseSalary,
        
        @Schema(description = "Email address (must be unique)", example = "juan.perez@email.com", required = true)
        String email
) {
}
