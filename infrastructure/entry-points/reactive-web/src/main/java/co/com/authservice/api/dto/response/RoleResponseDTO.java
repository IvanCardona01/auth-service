package co.com.authservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO response with role information")
public record RoleResponseDTO(
        @Schema(description = "Unique role ID", example = "2")
        Long id,
        
        @Schema(description = "Role name", example = "CLIENT")
        String name,
        
        @Schema(description = "Role description", example = "Regular client user")
        String description
) {}
