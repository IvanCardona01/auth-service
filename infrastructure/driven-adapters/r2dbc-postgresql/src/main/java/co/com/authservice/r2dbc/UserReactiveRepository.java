package co.com.authservice.r2dbc;

import co.com.authservice.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, Long>, ReactiveQueryByExampleExecutor<UserEntity> {
    Mono<Boolean> existsByEmail(String email);
    Mono<UserEntity> findByEmail(String email);
    Mono<UserEntity> getByDocumentNumber(String documentNumber);
    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
