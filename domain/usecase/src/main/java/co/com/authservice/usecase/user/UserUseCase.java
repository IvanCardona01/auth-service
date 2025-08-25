package co.com.authservice.usecase.user;

import co.com.authservice.model.user.User;
import co.com.authservice.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {
    private final UserRepository userRepository;

    public Mono<User> saveUser(User user) {
        return userRepository.saveUser(user)
                .map(savedUser -> {
                    return savedUser;
                });
    }


}
