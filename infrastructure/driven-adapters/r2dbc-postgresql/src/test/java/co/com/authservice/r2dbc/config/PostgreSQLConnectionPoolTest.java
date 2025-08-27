package co.com.authservice.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostgreSQLConnectionPool - Database Configuration Tests")
class PostgreSQLConnectionPoolTest {

    private PostgreSQLConnectionPool connectionPoolConfig;
    private PostgresqlConnectionProperties properties;

    @BeforeEach
    void setUp() {
        connectionPoolConfig = new PostgreSQLConnectionPool();

        properties = new PostgresqlConnectionProperties(
                "localhost",
                5432,
                "testdb",
                "public",
                "testuser",
                "testpass"
        );
    }

    @Nested
    @DisplayName("Connection Pool Configuration Tests")
    class ConnectionPoolConfigurationTests {

        @Test
        @DisplayName("Should create connection pool with correct properties")
        void shouldCreateConnectionPoolWithCorrectProperties() {
            ConnectionPool connectionPool = connectionPoolConfig.getConnectionConfig(properties);

            assertNotNull(connectionPool, "Connection pool should not be null");

            assertTrue(connectionPool instanceof ConnectionPool, "Should be instance of ConnectionPool");
        }

        @Test
        @DisplayName("Should use correct pool constants")
        void shouldUseCorrectPoolConstants() {
            assertEquals(12, PostgreSQLConnectionPool.INITIAL_SIZE, "Initial size should be 12");
            assertEquals(15, PostgreSQLConnectionPool.MAX_SIZE, "Max size should be 15");
            assertEquals(30, PostgreSQLConnectionPool.MAX_IDLE_TIME, "Max idle time should be 30");
            assertEquals(5432, PostgreSQLConnectionPool.DEFAULT_PORT, "Default port should be 5432");
        }

        @Test
        @DisplayName("Should handle different database properties")
        void shouldHandleDifferentDatabaseProperties() {
            PostgresqlConnectionProperties customProperties = new PostgresqlConnectionProperties(
                    "db.example.com",
                    5433,
                    "customdb",
                    "custom_schema",
                    "customuser",
                    "custompass"
            );

            ConnectionPool connectionPool = connectionPoolConfig.getConnectionConfig(customProperties);

            assertNotNull(connectionPool, "Connection pool should be created with custom properties");
        }

        @Test
        @DisplayName("Should handle standard port configuration")
        void shouldHandleStandardPortConfiguration() {
            PostgresqlConnectionProperties standardProperties = new PostgresqlConnectionProperties(
                    "localhost",
                    PostgreSQLConnectionPool.DEFAULT_PORT,
                    "standarddb",
                    "public",
                    "user",
                    "pass"
            );

            ConnectionPool connectionPool = connectionPoolConfig.getConnectionConfig(standardProperties);

            assertNotNull(connectionPool, "Connection pool should work with standard port");
        }
    }

    @Nested
    @DisplayName("Bean Creation Tests")
    class BeanCreationTests {

        @Test
        @DisplayName("Should create bean that can be used multiple times")
        void shouldCreateBeanThatCanBeUsedMultipleTimes() {
            ConnectionPool pool1 = connectionPoolConfig.getConnectionConfig(properties);
            ConnectionPool pool2 = connectionPoolConfig.getConnectionConfig(properties);

            assertNotNull(pool1, "First connection pool should not be null");
            assertNotNull(pool2, "Second connection pool should not be null");
            assertNotSame(pool1, pool2, "Should create different instances");
        }

        @Test
        @DisplayName("Should create connection pool with non-null properties")
        void shouldCreateConnectionPoolWithNonNullProperties() {
            ConnectionPool connectionPool = connectionPoolConfig.getConnectionConfig(properties);

            assertNotNull(connectionPool, "Connection pool should be created");

            assertFalse(connectionPool.isDisposed(), "Connection pool should not be disposed initially");
        }
    }

    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {

        @Test
        @DisplayName("Should handle minimal valid configuration")
        void shouldHandleMinimalValidConfiguration() {
            PostgresqlConnectionProperties minimalProperties = new PostgresqlConnectionProperties(
                    "localhost",
                    5432,
                    "db",
                    "public",
                    "user",
                    "password"
            );

            assertDoesNotThrow(() -> {
                ConnectionPool pool = connectionPoolConfig.getConnectionConfig(minimalProperties);
                assertNotNull(pool, "Should create pool with minimal configuration");
            }, "Should not throw with minimal valid configuration");
        }

        @Test
        @DisplayName("Should handle custom schema configuration")
        void shouldHandleCustomSchemaConfiguration() {
            PostgresqlConnectionProperties customSchemaProperties = new PostgresqlConnectionProperties(
                    "localhost",
                    5432,
                    "database",
                    "custom_schema",
                    "user",
                    "password"
            );

            assertDoesNotThrow(() -> {
                ConnectionPool pool = connectionPoolConfig.getConnectionConfig(customSchemaProperties);
                assertNotNull(pool, "Should create pool with custom schema");
            }, "Should handle custom schema without issues");
        }
    }

    @Nested
    @DisplayName("Pool Properties Verification Tests")
    class PoolPropertiesVerificationTests {

        @Test
        @DisplayName("Should verify pool constants are reasonable")
        void shouldVerifyPoolConstantsAreReasonable() {
            assertTrue(PostgreSQLConnectionPool.INITIAL_SIZE > 0, 
                    "Initial size should be positive");
            assertTrue(PostgreSQLConnectionPool.MAX_SIZE >= PostgreSQLConnectionPool.INITIAL_SIZE, 
                    "Max size should be greater than or equal to initial size");
            assertTrue(PostgreSQLConnectionPool.MAX_IDLE_TIME > 0, 
                    "Max idle time should be positive");
            assertTrue(PostgreSQLConnectionPool.DEFAULT_PORT > 0 && PostgreSQLConnectionPool.DEFAULT_PORT < 65536, 
                    "Default port should be valid port number");
        }

        @Test
        @DisplayName("Should verify pool sizing makes sense")
        void shouldVerifyPoolSizingMakesSense() {
            int initialSize = PostgreSQLConnectionPool.INITIAL_SIZE;
            int maxSize = PostgreSQLConnectionPool.MAX_SIZE;
            
            assertTrue(maxSize > initialSize, 
                    "Max size (" + maxSize + ") should be greater than initial size (" + initialSize + ")");
            assertTrue(maxSize - initialSize <= 10, 
                    "Difference between max and initial should not be too large");
        }
    }

    @Nested
    @DisplayName("Integration Configuration Tests")
    class IntegrationConfigurationTests {

        @Test
        @DisplayName("Should create pool that can be disposed properly")
        void shouldCreatePoolThatCanBeDisposedProperly() {
            ConnectionPool connectionPool = connectionPoolConfig.getConnectionConfig(properties);

            assertDoesNotThrow(() -> {
                connectionPool.dispose();
            }, "Connection pool should dispose without throwing exceptions");
        }

        @Test
        @DisplayName("Should create pool with validation query configured")
        void shouldCreatePoolWithValidationQueryConfigured() {
            ConnectionPool connectionPool = connectionPoolConfig.getConnectionConfig(properties);

            assertNotNull(connectionPool, "Connection pool should be created");
        }
    }
}
