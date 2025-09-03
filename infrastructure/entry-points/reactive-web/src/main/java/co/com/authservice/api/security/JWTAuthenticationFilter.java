package co.com.authservice.api.security;

import co.com.authservice.jwt.JWTTokenServiceImpl;
import co.com.authservice.model.user.User;
import co.com.authservice.usecase.auth.LoginUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter implements WebFilter {
    
    private final JWTTokenServiceImpl jwtTokenService;
    private final LoginUseCase loginUseCase;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange);
        }
        
        String token = authHeader.substring(7);
        
        return validateTokenAndExtractUser(token)
                .flatMap(user -> {
                    log.debug("✅ [AUTH] User authenticated: {} with role: {}", user.getEmail(), user.getRole().getName());
                    exchange.getAttributes().put("authenticated_user", user);
                    return chain.filter(exchange);
                })
                .onErrorResume(error -> {
                    log.warn("❌ [AUTH] Authentication failed for path {}: {}", path, error.getMessage());
                    if (error.getMessage().contains("Invalid credentials") || error.getMessage().contains("Token validation failed")) {
                        return unauthorizedResponse(exchange);
                    }
                    return forbiddenResponse(exchange, error.getMessage());
                });
    }
    
    private boolean isPublicPath(String path) {
        return path.equals("/api/v1/auth/login") || 
               path.equals("/api/v1/roles") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/webjars") ||
               path.startsWith("/v3/api-docs");
    }
    
    private Mono<User> validateTokenAndExtractUser(String token) {
        if (!jwtTokenService.isValidToken(token)) {
            return Mono.error(new RuntimeException("Invalid or expired token"));
        }
        
        String email = jwtTokenService.extractEmail(token);
        return loginUseCase.validateUserCredentials(email)
                .onErrorMap(e -> new RuntimeException("Token validation failed", e));
    }
    
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        String path = exchange.getRequest().getPath().value();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String errorJson = String.format(
            "{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required. Please provide a valid JWT token.\",\"timestamp\":\"%s\",\"path\":\"%s\"}",
            timestamp, path
        );
        
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    
    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        String path = exchange.getRequest().getPath().value();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String errorJson = String.format(
            "{\"code\":\"FORBIDDEN\",\"message\":\"%s\",\"timestamp\":\"%s\",\"path\":\"%s\"}",
            message != null ? message : "Access denied. Insufficient permissions.", timestamp, path
        );
        
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
