package co.com.authservice.api.config;

import co.com.authservice.api.Handler;
import co.com.authservice.api.RouterRest;
import co.com.authservice.api.dto.response.UserResponseDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.model.user.User;
import co.com.authservice.usecase.user.UserUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private UserUseCase userUseCase;
    
    @MockBean
    private UserDTOMapper userDTOMapper;
    
    @MockBean
    private Validator validator;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        when(userUseCase.getAll()).thenReturn(Flux.empty());
        when(userDTOMapper.toResponse(any(User.class))).thenReturn(new UserResponseDTO(
                1L, "Test", "User", LocalDate.now(), null, null, BigDecimal.ZERO, "test@email.com", null));
        
        webTestClient.get()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}