package co.com.authservice.api.security;

import co.com.authservice.model.user.User;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class AuthorizationUtils {

    public static Mono<User> getAuthenticatedUser(ServerWebExchange exchange) {
        User user = exchange.getAttribute("authenticated_user");
        if (user == null) {
            return Mono.error(new RuntimeException("No authenticated user found"));
        }
        return Mono.just(user);
    }

    public static Mono<User> requireAdminOrAdvisorRole(User user) {
        String role = user.getRole().getName();
        if (!"ADMIN".equals(role) && !"ADVISOR".equals(role)) {
            return Mono.error(new RuntimeException("Access denied. Admin or Advisor role required."));
        }
        return Mono.just(user);
    }

    public static Mono<User> requireClientOwnership(User user, Long resourceOwnerId) {
        String role = user.getRole().getName();
        if ("CLIENT".equals(role)) {
            if (!user.getId().equals(resourceOwnerId)) {
                return Mono.error(new RuntimeException("Access denied. You can only access your own resources."));
            }
        }

        return Mono.just(user);
    }
}
