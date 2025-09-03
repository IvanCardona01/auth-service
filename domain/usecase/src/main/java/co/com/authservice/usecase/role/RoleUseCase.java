package co.com.authservice.usecase.role;

import co.com.authservice.model.role.Role;
import co.com.authservice.model.role.gateways.RoleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RoleUseCase {
    
    private final RoleRepository roleRepository;

    public Flux<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Mono<Role> findRoleByName(String name) {
        if (name == null || name.isBlank()) {
            return Mono.error(new IllegalArgumentException("Role name is required"));
        }
        return roleRepository.findByName(name);
    }
}
