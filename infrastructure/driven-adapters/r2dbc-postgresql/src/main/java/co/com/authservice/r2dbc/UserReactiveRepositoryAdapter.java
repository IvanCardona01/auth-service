package co.com.authservice.r2dbc;

import co.com.authservice.model.role.Role;
import co.com.authservice.model.user.User;
import co.com.authservice.model.user.gateways.UserRepository;
import co.com.authservice.r2dbc.entity.UserEntity;
import co.com.authservice.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserReactiveRepositoryAdapter
        extends ReactiveAdapterOperations<User, UserEntity, Long, UserReactiveRepository>
        implements UserRepository {

    private final RoleReactiveRepository roleRepository;

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, RoleReactiveRepository roleRepository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
        this.roleRepository = roleRepository;
    }

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity userEntity = mapper.map(user, UserEntity.class);
        if (user.getRole() != null) {
            userEntity.setRoleId(user.getRole().getId());
        }
        return repository.save(userEntity)
                .flatMap(this::mapToUserWithRole);
    }

    @Override
    public Mono<Boolean> existByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Flux<User> getAll() {
        return repository.findAll()
                .flatMap(this::mapToUserWithRole);
    }

    private Mono<User> mapToUserWithRole(UserEntity entity) {
        User user = mapper.map(entity, User.class);

        if (entity.getRoleId() != null) {
            return roleRepository.findById(entity.getRoleId())
                    .map(roleEntity -> mapper.map(roleEntity, Role.class))
                    .doOnNext(user::setRole)
                    .thenReturn(user);
        }

        return Mono.just(user);
    }
}
