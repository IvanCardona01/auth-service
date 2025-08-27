package co.com.authservice.api;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.dto.response.UserResponseDTO;
import co.com.authservice.api.mapper.UserDTOMapper;
import co.com.authservice.model.user.User;
import co.com.authservice.usecase.user.UserUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private UserUseCase userUseCase;
    
    @MockBean
    private UserDTOMapper userDTOMapper;
    
    @MockBean
    private Validator validator;

    @Test
    void testGetAllUsersEndpoint() {
        when(userUseCase.getAll()).thenReturn(Flux.empty());
        
        webTestClient.get()
                .uri("/api/v1/user")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void testCreateUserEndpoint() {
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "Juan", "Perez", LocalDate.of(1990, 5, 15), 
            "Calle 123", "+57 300 123 4567", 
            BigDecimal.valueOf(5000000), "juan.perez@email.com"
        );
        
        User savedUser = User.builder()
                .id(1L)
                .name("Juan")
                .lastname("Perez")
                .birthdayDate(LocalDate.of(1990, 5, 15))
                .address("Calle 123")
                .phoneNumber("+57 300 123 4567")
                .baseSalary(BigDecimal.valueOf(5000000))
                .email("juan.perez@email.com")
                .build();
        
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                1L, "Juan", "Perez", LocalDate.of(1990, 5, 15),
                "Calle 123", "+57 300 123 4567", BigDecimal.valueOf(5000000),
                "juan.perez@email.com", null
        );

        Set<ConstraintViolation<CreateUserDTO>> emptyViolations = Collections.emptySet();
        when(validator.validate(any(CreateUserDTO.class))).thenReturn(emptyViolations);
        when(userDTOMapper.toModel(any(CreateUserDTO.class))).thenReturn(savedUser);
        when(userUseCase.saveUser(any(User.class))).thenReturn(Mono.just(savedUser));
        when(userDTOMapper.toResponse(any(User.class))).thenReturn(userResponseDTO);
        
        String userJson = "{" +
                "\"name\": \"Juan\"," +
                "\"lastname\": \"Perez\"," +
                "\"birthdayDate\": \"1990-05-15\"," +
                "\"address\": \"Calle 123\"," +
                "\"phoneNumber\": \"+57 300 123 4567\"," +
                "\"baseSalary\": 5000000," +
                "\"email\": \"juan.perez@email.com\"" +
                "}";
        
        webTestClient.post()
                .uri("/api/v1/user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userJson)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Juan")
                .jsonPath("$.lastname").isEqualTo("Perez")
                .jsonPath("$.email").isEqualTo("juan.perez@email.com");
    }
    
    @Test
    void testInvalidRoute() {
        webTestClient.get()
                .uri("/api/v1/nonexistent")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}
