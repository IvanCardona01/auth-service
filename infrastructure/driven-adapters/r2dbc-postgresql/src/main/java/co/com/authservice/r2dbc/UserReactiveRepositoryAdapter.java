package co.com.authservice.r2dbc;

import co.com.authservice.model.user.User;
import co.com.authservice.model.user.gateways.UserRepository;
import co.com.authservice.r2dbc.entity.UserEntity;
import co.com.authservice.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserReactiveRepositoryAdapter
        extends ReactiveAdapterOperations<User, UserEntity, String, UserReactiveRepository>
        implements UserRepository {

    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Override
    public Mono<User> saveUser(User user) {
        return repository.save(toData(user))
                .map(saved -> mapper.map(saved, User.class));
    }

    @Override
    public Mono<Boolean> existByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
