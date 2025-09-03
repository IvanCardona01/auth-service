package co.com.authservice.model.user.gateways;

import co.com.authservice.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<Boolean> existByEmail(String email);
    Mono<User> saveUser(User user);
    Flux<User> getAll();
    Mono<User> getByDocumentNumber(String documentNumber);
    Mono<Boolean> existByDocumentNumber(String documentNumber);
    Mono<User> findByEmail(String email);
}
