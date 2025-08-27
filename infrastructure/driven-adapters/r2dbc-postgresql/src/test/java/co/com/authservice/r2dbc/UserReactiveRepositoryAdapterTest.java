package co.com.authservice.r2dbc;

import co.com.authservice.model.role.Role;
import co.com.authservice.model.user.User;
import co.com.authservice.r2dbc.entity.RoleEntity;
import co.com.authservice.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserReactiveRepositoryAdapter - Infrastructure Tests")
class UserReactiveRepositoryAdapterTest {

    @Mock
    private UserReactiveRepository userRepository;

    @Mock
    private RoleReactiveRepository roleRepository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private UserReactiveRepositoryAdapter repositoryAdapter;

    private User domainUser;
    private UserEntity userEntity;
    private Role domainRole;
    private RoleEntity roleEntity;

    @BeforeEach
    void setUp() {
        domainUser = User.builder()
                .id(1L)
                .name("Juan")
                .lastname("Pérez")
                .email("juan.perez@email.com")
                .birthdayDate(LocalDate.of(1990, 5, 15))
                .baseSalary(new BigDecimal("5000000"))
                .address("Calle 123")
                .phoneNumber("+57 300 123 4567")
                .build();

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setName("Juan");
        userEntity.setLastname("Pérez");
        userEntity.setEmail("juan.perez@email.com");
        userEntity.setBirthdayDate(LocalDate.of(1990, 5, 15));
        userEntity.setBaseSalary(new BigDecimal("5000000"));
        userEntity.setAddress("Calle 123");
        userEntity.setPhoneNumber("+57 300 123 4567");
        userEntity.setRoleId(1L);

        domainRole = Role.builder()
                .id(1L)
                .name("CLIENT")
                .description("Default client role")
                .build();

        roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setName("CLIENT");
        roleEntity.setDescription("Default client role");
    }

    @Nested
    @DisplayName("saveUser - Transaction Management Tests")
    class SaveUserTransactionTests {

        @Test
        @DisplayName("Should save user with transaction successfully")
        void shouldSaveUserWithTransactionSuccessfully() {
            User userWithRole = domainUser.toBuilder().role(domainRole).build();
            
            when(mapper.map(userWithRole, UserEntity.class)).thenReturn(userEntity);
            when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(userEntity));
            when(roleRepository.findById(1L)).thenReturn(Mono.just(roleEntity));
            when(mapper.map(userEntity, User.class)).thenReturn(domainUser);
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);

            when(transactionalOperator.transactional(any(Mono.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            StepVerifier.create(repositoryAdapter.saveUser(userWithRole))
                    .expectNextMatches(savedUser -> 
                            savedUser.getId().equals(1L) &&
                            savedUser.getName().equals("Juan") &&
                            savedUser.getRole() != null &&
                            savedUser.getRole().getName().equals("CLIENT")
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should save user without role successfully")
        void shouldSaveUserWithoutRoleSuccessfully() {
            User userWithoutRole = domainUser.toBuilder().role(null).build();
            UserEntity entityWithoutRole = new UserEntity();
            entityWithoutRole.setId(1L);
            entityWithoutRole.setName("Juan");
            entityWithoutRole.setRoleId(null);
            
            when(mapper.map(userWithoutRole, UserEntity.class)).thenReturn(entityWithoutRole);
            when(userRepository.save(any(UserEntity.class))).thenReturn(Mono.just(entityWithoutRole));
            when(mapper.map(entityWithoutRole, User.class)).thenReturn(userWithoutRole);
            
            when(transactionalOperator.transactional(any(Mono.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            StepVerifier.create(repositoryAdapter.saveUser(userWithoutRole))
                    .expectNextMatches(savedUser -> 
                            savedUser.getId().equals(1L) &&
                            savedUser.getRole() == null
                    )
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("existByEmail - Email Validation Tests")
    class ExistByEmailTests {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            when(userRepository.existsByEmail("juan.perez@email.com")).thenReturn(Mono.just(true));

            StepVerifier.create(repositoryAdapter.existByEmail("juan.perez@email.com"))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            when(userRepository.existsByEmail("nonexistent@email.com")).thenReturn(Mono.just(false));

            StepVerifier.create(repositoryAdapter.existByEmail("nonexistent@email.com"))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getAll - Retrieve Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users with their roles")
        void shouldReturnAllUsersWithTheirRoles() {
            UserEntity entity1 = new UserEntity();
            entity1.setId(1L);
            entity1.setName("Juan");
            entity1.setEmail("juan@email.com");
            entity1.setRoleId(1L);

            UserEntity entity2 = new UserEntity();
            entity2.setId(2L);
            entity2.setName("María");
            entity2.setEmail("maria@email.com");
            entity2.setRoleId(1L);

            User user1 = User.builder().id(1L).name("Juan").email("juan@email.com").build();
            User user2 = User.builder().id(2L).name("María").email("maria@email.com").build();

            when(userRepository.findAll()).thenReturn(Flux.just(entity1, entity2));
            when(mapper.map(entity1, User.class)).thenReturn(user1);
            when(mapper.map(entity2, User.class)).thenReturn(user2);
            when(roleRepository.findById(1L)).thenReturn(Mono.just(roleEntity));
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);

            StepVerifier.create(repositoryAdapter.getAll())
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return users without roles when role is null")
        void shouldReturnUsersWithoutRolesWhenRoleIsNull() {
            UserEntity entityWithoutRole = new UserEntity();
            entityWithoutRole.setId(1L);
            entityWithoutRole.setName("Juan");
            entityWithoutRole.setRoleId(null);

            User userWithoutRole = User.builder()
                    .id(1L)
                    .name("Juan")
                    .build();

            when(userRepository.findAll()).thenReturn(Flux.just(entityWithoutRole));
            when(mapper.map(entityWithoutRole, User.class)).thenReturn(userWithoutRole);

            StepVerifier.create(repositoryAdapter.getAll())
                    .expectNextMatches(user -> 
                            user.getName().equals("Juan") &&
                            user.getRole() == null
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty flux when no users exist")
        void shouldReturnEmptyFluxWhenNoUsersExist() {
            when(userRepository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(repositoryAdapter.getAll())
                    .expectNextCount(0)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle database error during save")
        void shouldHandleDatabaseErrorDuringSave() {
            when(mapper.map(any(User.class), eq(UserEntity.class))).thenReturn(userEntity);
            when(userRepository.save(any(UserEntity.class)))
                    .thenReturn(Mono.error(new RuntimeException("Database connection failed")));
            
            when(transactionalOperator.transactional(any(Mono.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            StepVerifier.create(repositoryAdapter.saveUser(domainUser))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Database connection failed")
                    )
                    .verify();
        }

        @Test
        @DisplayName("Should handle role mapping error")
        void shouldHandleRoleMappingError() {
            when(userRepository.findAll()).thenReturn(Flux.just(userEntity));
            when(mapper.map(userEntity, User.class)).thenReturn(domainUser);
            when(roleRepository.findById(1L))
                    .thenReturn(Mono.error(new RuntimeException("Role not found")));

            StepVerifier.create(repositoryAdapter.getAll())
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Role not found")
                    )
                    .verify();
        }
    }
}
