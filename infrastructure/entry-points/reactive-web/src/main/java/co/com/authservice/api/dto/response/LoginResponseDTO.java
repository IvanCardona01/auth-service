package co.com.authservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO for successful authentication")
public record LoginResponseDTO(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Expires In",example = "3600")
        Long expiresIn,

        @Schema(description = "User information")
        UserSummaryDTO user
) {
}
