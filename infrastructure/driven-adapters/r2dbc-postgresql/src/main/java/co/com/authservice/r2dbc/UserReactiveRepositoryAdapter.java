package co.com.authservice.r2dbc;

import co.com.authservice.model.role.Role;
import co.com.authservice.model.user.User;
import co.com.authservice.model.user.gateways.UserRepository;
import co.com.authservice.r2dbc.entity.UserEntity;
import co.com.authservice.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class UserReactiveRepositoryAdapter
        extends ReactiveAdapterOperations<User, UserEntity, Long, UserReactiveRepository>
        implements UserRepository {

    private final RoleReactiveRepository roleRepository;
    private final TransactionalOperator transactionalOperator;

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, 
                                        RoleReactiveRepository roleRepository, 
                                        ObjectMapper mapper,
                                        TransactionalOperator transactionalOperator) {
        super(repository, mapper, d -> mapper.map(d, User.class));
        this.roleRepository = roleRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<User> getByDocumentNumber(String documentNumber) {
        return repository.getByDocumentNumber(documentNumber)
                .flatMap(this::mapToUserWithRole);
    }

    @Override
    public Mono<Boolean> existByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public Flux<User> getAll() {
        return repository.findAll()
                .flatMap(this::mapToUserWithRole);
    }

    @Override
    public Mono<User> saveUser(User user) {
        return saveUserInternal(user)
                .doOnNext(u -> log.debug("✅ [PERSISTENCE] User saved in transaction"))
                .doOnError(error -> log.error("❌ [PERSISTENCE] Transaction failed: {}", error.getMessage()))
                .as(transactionalOperator::transactional);
    }
    
    private Mono<User> saveUserInternal(User user) {
        UserEntity userEntity = mapper.map(user, UserEntity.class);
        if (user.getRole() != null) {
            userEntity.setRoleId(user.getRole().getId());
            log.debug("🔗 [PERSISTENCE] Mapping role ID: {}", user.getRole().getId());
        }
        return repository.save(userEntity)
                .doOnNext(entity -> log.debug("💾 [PERSISTENCE] Entity saved with ID: {}", entity.getId()))
                .flatMap(this::mapToUserWithRole);
    }

    @Override
    public Mono<Boolean> existByEmail(String email) {
        return repository.existsByEmail(email);
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
