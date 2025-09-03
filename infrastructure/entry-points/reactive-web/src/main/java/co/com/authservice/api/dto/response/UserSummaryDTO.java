package co.com.authservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary information about the authenticated user")
public record UserSummaryDTO(
        @Schema(description = "User ID", example = "1")
        Long id,
        
        @Schema(description = "User full name", example = "Juan Perez")
        String fullName,
        
        @Schema(description = "User email", example = "juan.perez@email.com")
        String email,
        
        @Schema(description = "User role information")
        RoleResponseDTO role
) {}
