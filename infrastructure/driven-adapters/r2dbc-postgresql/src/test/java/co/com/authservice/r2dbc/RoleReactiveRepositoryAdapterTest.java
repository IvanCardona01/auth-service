package co.com.authservice.r2dbc;

import co.com.authservice.model.role.Role;
import co.com.authservice.r2dbc.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleReactiveRepositoryAdapter - Infrastructure Tests")
class RoleReactiveRepositoryAdapterTest {

    @Mock
    private RoleReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private RoleReactiveRepositoryAdapter repositoryAdapter;

    private Role domainRole;
    private RoleEntity roleEntity;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize adapter correctly")
        void shouldInitializeAdapterCorrectly() {
            when(repository.findById(1L)).thenReturn(Mono.just(roleEntity));
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);

            StepVerifier.create(repositoryAdapter.findById(1L))
                    .expectNext(domainRole)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findByName - Role Lookup Tests")
    class FindByNameTests {

        @Test
        @DisplayName("Should find role by name successfully")
        void shouldFindRoleByNameSuccessfully() {
            when(repository.findByName("CLIENT")).thenReturn(Mono.just(roleEntity));
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);

            StepVerifier.create(repositoryAdapter.findByName("CLIENT"))
                    .expectNextMatches(role -> 
                            role.getName().equals("CLIENT") &&
                            role.getDescription().equals("Default client role")
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when role name not found")
        void shouldReturnEmptyWhenRoleNameNotFound() {
            when(repository.findByName("NONEXISTENT")).thenReturn(Mono.empty());

            StepVerifier.create(repositoryAdapter.findByName("NONEXISTENT"))
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle database error during findByName")
        void shouldHandleDatabaseErrorDuringFindByName() {
            when(repository.findByName("CLIENT"))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            StepVerifier.create(repositoryAdapter.findByName("CLIENT"))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Database error")
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("findAll - List All Roles Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all roles successfully")
        void shouldReturnAllRolesSuccessfully() {
            RoleEntity adminRoleEntity = new RoleEntity();
            adminRoleEntity.setId(2L);
            adminRoleEntity.setName("ADMIN");
            adminRoleEntity.setDescription("Administrator role");

            Role adminRole = Role.builder()
                    .id(2L)
                    .name("ADMIN")
                    .description("Administrator role")
                    .build();

            when(repository.findAll()).thenReturn(Flux.just(roleEntity, adminRoleEntity));
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);
            when(mapper.map(adminRoleEntity, Role.class)).thenReturn(adminRole);

            StepVerifier.create(repositoryAdapter.findAll())
                    .expectNext(domainRole)
                    .expectNext(adminRole)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty flux when no roles exist")
        void shouldReturnEmptyFluxWhenNoRolesExist() {
            when(repository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(repositoryAdapter.findAll())
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle database error during findAll")
        void shouldHandleDatabaseErrorDuringFindAll() {
            when(repository.findAll())
                    .thenReturn(Flux.error(new RuntimeException("Connection lost")));

            StepVerifier.create(repositoryAdapter.findAll())
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Connection lost")
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("Inherited Operations Tests")
    class InheritedOperationsTests {

        @Test
        @DisplayName("Should save role successfully")
        void shouldSaveRoleSuccessfully() {
            when(mapper.map(domainRole, RoleEntity.class)).thenReturn(roleEntity);
            when(repository.save(any(RoleEntity.class))).thenReturn(Mono.just(roleEntity));
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);

            StepVerifier.create(repositoryAdapter.save(domainRole))
                    .expectNext(domainRole)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should find role by id successfully")
        void shouldFindRoleByIdSuccessfully() {
            when(repository.findById(1L)).thenReturn(Mono.just(roleEntity));
            when(mapper.map(roleEntity, Role.class)).thenReturn(domainRole);

            StepVerifier.create(repositoryAdapter.findById(1L))
                    .expectNext(domainRole)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when role id not found")
        void shouldReturnEmptyWhenRoleIdNotFound() {
            when(repository.findById(999L)).thenReturn(Mono.empty());

            StepVerifier.create(repositoryAdapter.findById(999L))
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should handle mapping error")
        void shouldHandleMappingError() {
            when(repository.findById(1L)).thenReturn(Mono.just(roleEntity));
            when(mapper.map(roleEntity, Role.class))
                    .thenThrow(new RuntimeException("Mapping failed"));

            StepVerifier.create(repositoryAdapter.findById(1L))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Mapping failed")
                    )
                    .verify();
        }
    }
}
