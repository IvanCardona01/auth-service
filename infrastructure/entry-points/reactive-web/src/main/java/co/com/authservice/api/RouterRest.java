package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.dto.response.ErrorResponseDTO;
import co.com.authservice.api.dto.response.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/user",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getAllUsers",
                    operation = @Operation(
                            operationId = "getAllUsers",
                            summary = "Get all users",
                            description = "Retrieves the complete list of users registered in the system",
                            tags = {"Users"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Users retrieved successfully",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class))
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDTO.class,
                                                            example = "{\"code\": \"INTERNAL_SERVER_ERROR\", \"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\", \"path\": \"/api/v1/user\"}")
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/user",
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createUser",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Create a new user",
                            description = "Registers a new user in the system with the provided information. User must be at least 18 years old and have a valid salary between 0 and 15,000,000.",
                            tags = {"Users"},
                            requestBody = @RequestBody(
                                    description = "User data to be created",
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = CreateUserDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "User created successfully",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = UserResponseDTO.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input data. Possible errors: INVALID_AGE (user under 18), INVALID_SALARY (negative or exceeding 15,000,000), USER_VALIDATION_ERROR (validation data issues), VALIDATION_ERROR (format errors), BAD_REQUEST (malformed data), INVALID_JSON (invalid JSON format)",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDTO.class,
                                                            example = "{\"code\": \"INVALID_AGE\", \"message\": \"User must be at least 18 years old, but was 16\", \"timestamp\": \"2024-01-15T10:30:00\", \"path\": \"/api/v1/user\"}")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "405",
                                            description = "HTTP method not allowed",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDTO.class,
                                                            example = "{\"code\": \"METHOD_NOT_ALLOWED\", \"message\": \"Method not supported\", \"timestamp\": \"2024-01-15T10:30:00\", \"path\": \"/api/v1/user\"}")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "409",
                                            description = "Conflict: Email is already registered in the system",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDTO.class,
                                                            example = "{\"code\": \"EMAIL_ALREADY_EXISTS\", \"message\": \"Email 'juan.perez@email.com' is already registered\", \"timestamp\": \"2024-01-15T10:30:00\", \"path\": \"/api/v1/user\"}")
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = ErrorResponseDTO.class,
                                                            example = "{\"code\": \"INTERNAL_SERVER_ERROR\", \"message\": \"An unexpected error occurred\", \"timestamp\": \"2024-01-15T10:30:00\", \"path\": \"/api/v1/user\"}")
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(GET("/api/v1/user").and(accept(MediaType.APPLICATION_JSON)), handler::getAllUsers)
                .andRoute(POST("/api/v1/user").and(accept(MediaType.APPLICATION_JSON)), handler::createUser);
    }
}
