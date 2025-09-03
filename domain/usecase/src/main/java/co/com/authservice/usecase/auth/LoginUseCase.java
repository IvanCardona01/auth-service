package co.com.authservice.usecase.auth;

import co.com.authservice.model.user.User;
import co.com.authservice.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoginUseCase {
    
    private final UserRepository userRepository;

    public Mono<User> validateUserCredentials(String email, String password) {
        return validateLoginInputs(email, password)
                .then(userRepository.findByEmail(email)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid credentials"))));
    }

    public Mono<User> validateUserCredentials(String email) {
        if (isBlank(email)) {
            return Mono.error(new IllegalArgumentException("Email is required"));
        }
        
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid credentials")));
    }

    private Mono<Void> validateLoginInputs(String email, String password) {
        if (isBlank(email)) {
            return Mono.error(new IllegalArgumentException("Email is required and cannot be blank"));
        }
        
        if (isBlank(password)) {
            return Mono.error(new IllegalArgumentException("Password is required and cannot be blank"));
        }
        
        return Mono.empty();
    }
    
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
