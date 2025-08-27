package co.com.authservice.r2dbc.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactiveAdapterOperations - Base Adapter Tests")
class ReactiveAdapterOperationsTest {

    static class TestDomain {
        private Long id;
        private String name;
        
        public TestDomain() {}
        
        public TestDomain(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestDomain)) return false;
            TestDomain that = (TestDomain) obj;
            return id != null ? id.equals(that.id) : that.id == null;
        }
        
        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }

    static class TestData {
        private Long id;
        private String name;
        
        public TestData() {}
        
        public TestData(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    interface TestRepository extends ReactiveCrudRepository<TestData, Long>, 
                                   ReactiveQueryByExampleExecutor<TestData> {}

    static class TestReactiveAdapterOperations 
            extends ReactiveAdapterOperations<TestDomain, TestData, Long, TestRepository> {
        
        public TestReactiveAdapterOperations(TestRepository repository, ObjectMapper mapper) {
            super(repository, mapper, data -> {
                TestDomain domain = new TestDomain();
                domain.setId(data.getId());
                domain.setName(data.getName());
                return domain;
            });
        }
    }

    @Mock
    private TestRepository repository;

    @Mock
    private ObjectMapper mapper;

    private TestReactiveAdapterOperations adapterOperations;
    private TestDomain domainEntity;
    private TestData dataEntity;

    @BeforeEach
    void setUp() {
        adapterOperations = new TestReactiveAdapterOperations(repository, mapper);
        
        domainEntity = new TestDomain(1L, "Test Entity");
        dataEntity = new TestData(1L, "Test Entity");
    }

    @Nested
    @DisplayName("Entity Conversion Tests")
    class EntityConversionTests {

        @Test
        @DisplayName("Should convert domain entity to data entity")
        void shouldConvertDomainEntityToDataEntity() {
            when(mapper.map(domainEntity, TestData.class)).thenReturn(dataEntity);

            TestData result = adapterOperations.toData(domainEntity);

            verify(mapper).map(domainEntity, TestData.class);
        }

        @Test
        @DisplayName("Should convert data entity to domain entity using function")
        void shouldConvertDataEntityToDomainEntityUsingFunction() {
            TestDomain result = adapterOperations.toEntity(dataEntity);

            assert result != null;
            assert result.getId().equals(1L);
            assert result.getName().equals("Test Entity");
        }

        @Test
        @DisplayName("Should handle null data entity")
        void shouldHandleNullDataEntity() {
            TestDomain result = adapterOperations.toEntity(null);

            assert result == null;
        }
    }

    @Nested
    @DisplayName("Save Operations Tests")
    class SaveOperationsTests {

        @Test
        @DisplayName("Should save single entity successfully")
        void shouldSaveSingleEntitySuccessfully() {
            when(mapper.map(domainEntity, TestData.class)).thenReturn(dataEntity);
            when(repository.save(any(TestData.class))).thenReturn(Mono.just(dataEntity));

            StepVerifier.create(adapterOperations.save(domainEntity))
                    .expectNextMatches(saved -> 
                            saved.getId().equals(1L) && 
                            saved.getName().equals("Test Entity"))
                    .verifyComplete();

            verify(repository).save(dataEntity);
        }

        @Test
        @DisplayName("Should save multiple entities successfully")
        void shouldSaveMultipleEntitiesSuccessfully() {
            TestDomain secondDomain = new TestDomain(2L, "Second Entity");
            TestData secondData = new TestData(2L, "Second Entity");

            lenient().when(mapper.map(domainEntity, TestData.class)).thenReturn(dataEntity);
            lenient().when(mapper.map(secondDomain, TestData.class)).thenReturn(secondData);
            lenient().when(repository.saveAll(any(Flux.class))).thenReturn(Flux.just(dataEntity, secondData));

            assertDoesNotThrow(() -> {
                adapterOperations.saveAllEntities(Flux.just(domainEntity, secondDomain))
                        .blockLast();
            });
        }

        @Test
        @DisplayName("Should handle save error")
        void shouldHandleSaveError() {
            when(mapper.map(domainEntity, TestData.class)).thenReturn(dataEntity);
            when(repository.save(any(TestData.class)))
                    .thenReturn(Mono.error(new RuntimeException("Save failed")));

            StepVerifier.create(adapterOperations.save(domainEntity))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Save failed"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find by id successfully")
        void shouldFindByIdSuccessfully() {
            when(repository.findById(1L)).thenReturn(Mono.just(dataEntity));

            StepVerifier.create(adapterOperations.findById(1L))
                    .expectNextMatches(found -> 
                            found.getId().equals(1L) && 
                            found.getName().equals("Test Entity"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when id not found")
        void shouldReturnEmptyWhenIdNotFound() {
            when(repository.findById(999L)).thenReturn(Mono.empty());

            StepVerifier.create(adapterOperations.findById(999L))
                    .expectNextCount(0)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should find all entities successfully")
        void shouldFindAllEntitiesSuccessfully() {
            TestData secondData = new TestData(2L, "Second Entity");
            when(repository.findAll()).thenReturn(Flux.just(dataEntity, secondData));

            StepVerifier.create(adapterOperations.findAll())
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should find by example successfully")
        void shouldFindByExampleSuccessfully() {
            when(mapper.map(domainEntity, TestData.class)).thenReturn(dataEntity);
            when(repository.findAll(any(Example.class))).thenReturn(Flux.just(dataEntity));

            StepVerifier.create(adapterOperations.findByExample(domainEntity))
                    .expectNextMatches(found -> 
                            found.getId().equals(1L))
                    .verifyComplete();

            verify(repository).findAll(any(Example.class));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle mapping error during conversion")
        void shouldHandleMappingErrorDuringConversion() {
            when(mapper.map(domainEntity, TestData.class))
                    .thenThrow(new RuntimeException("Mapping error"));

            assertThrows(RuntimeException.class, () -> {
                adapterOperations.save(domainEntity).block();
            });
        }

        @Test
        @DisplayName("Should handle repository error during findAll")
        void shouldHandleRepositoryErrorDuringFindAll() {
            when(repository.findAll())
                    .thenReturn(Flux.error(new RuntimeException("Database error")));

            StepVerifier.create(adapterOperations.findAll())
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Database error"))
                    .verify();
        }

        @Test
        @DisplayName("Should handle repository error during findByExample")
        void shouldHandleRepositoryErrorDuringFindByExample() {
            when(mapper.map(domainEntity, TestData.class)).thenReturn(dataEntity);
            when(repository.findAll(any(Example.class)))
                    .thenReturn(Flux.error(new RuntimeException("Query error")));

            StepVerifier.create(adapterOperations.findByExample(domainEntity))
                    .expectErrorMatches(error -> 
                            error instanceof RuntimeException &&
                            error.getMessage().equals("Query error"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Protected Methods Tests")
    class ProtectedMethodsTests {

        @Test
        @DisplayName("Should save data using repository")
        void shouldSaveDataUsingRepository() {
            when(repository.save(dataEntity)).thenReturn(Mono.just(dataEntity));

            StepVerifier.create(adapterOperations.saveData(dataEntity))
                    .expectNext(dataEntity)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should save flux of data using repository")
        void shouldSaveFluxOfDataUsingRepository() {
            TestData secondData = new TestData(2L, "Second");
            when(repository.saveAll(any(Flux.class))).thenReturn(Flux.just(dataEntity, secondData));

            StepVerifier.create(adapterOperations.saveData(Flux.just(dataEntity, secondData)))
                    .expectNextCount(2)
                    .verifyComplete();
        }
    }
}
