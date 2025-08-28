package co.com.authservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "DTO response with user information")
public record UserResponseDTO(
        @Schema(description = "Unique user ID", example = "1")
        Long id,

        @Schema(description = "Unique document number", example = "123456789")
        String  documentNumber,

        @Schema(description = "User's first name", example = "Juan")
        String name,
        
        @Schema(description = "User's last name", example = "Pérez")
        String lastname,
        
        @Schema(description = "Date of birth", example = "1990-05-15")
        LocalDate birthdayDate,
        
        @Schema(description = "User's address", example = "Calle 123 #45-67")
        String address,
        
        @Schema(description = "Phone number", example = "+57 300 123 4567")
        String phoneNumber,
        
        @Schema(description = "Base salary", example = "5000000.00")
        BigDecimal baseSalary,
        
        @Schema(description = "Email address", example = "juan.perez@email.com")
        String email,
        
        @Schema(description = "Role assigned to the user")
        RoleResponseDTO role
) {
}
