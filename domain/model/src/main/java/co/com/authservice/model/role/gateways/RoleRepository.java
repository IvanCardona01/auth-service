package co.com.authservice.model.role.gateways;

import co.com.authservice.model.role.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository {
    Mono<Role> findByName(String name);
    Flux<Role> findAll();
}
