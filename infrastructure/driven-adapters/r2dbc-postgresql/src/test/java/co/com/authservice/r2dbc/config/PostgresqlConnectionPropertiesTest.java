package co.com.authservice.r2dbc.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PostgresqlConnectionProperties - Configuration Record Tests")
class PostgresqlConnectionPropertiesTest {

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create record with all properties")
        void shouldCreateRecordWithAllProperties() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    "localhost",
                    5432,
                    "testdb",
                    "public",
                    "testuser",
                    "testpass"
            );

            assertNotNull(properties, "Properties should not be null");
            assertEquals("localhost", properties.host(), "Host should match");
            assertEquals(5432, properties.port(), "Port should match");
            assertEquals("testdb", properties.database(), "Database should match");
            assertEquals("public", properties.schema(), "Schema should match");
            assertEquals("testuser", properties.username(), "Username should match");
            assertEquals("testpass", properties.password(), "Password should match");
        }

        @Test
        @DisplayName("Should create record with null values")
        void shouldCreateRecordWithNullValues() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            assertNotNull(properties, "Properties object should not be null");
            assertNull(properties.host(), "Host should be null");
            assertNull(properties.port(), "Port should be null");
            assertNull(properties.database(), "Database should be null");
            assertNull(properties.schema(), "Schema should be null");
            assertNull(properties.username(), "Username should be null");
            assertNull(properties.password(), "Password should be null");
        }

        @Test
        @DisplayName("Should create record with mixed null and non-null values")
        void shouldCreateRecordWithMixedNullAndNonNullValues() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    "localhost",
                    5432,
                    "mydb",
                    null,
                    "user",
                    "pass"
            );

            assertEquals("localhost", properties.host(), "Host should be set");
            assertEquals(5432, properties.port(), "Port should be set");
            assertEquals("mydb", properties.database(), "Database should be set");
            assertNull(properties.schema(), "Schema should be null");
            assertEquals("user", properties.username(), "Username should be set");
            assertEquals("pass", properties.password(), "Password should be set");
        }
    }

    @Nested
    @DisplayName("Record Immutability Tests")
    class RecordImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - properties cannot be changed after creation")
        void shouldBeImmutablePropertiesCannotBeChangedAfterCreation() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    "originalhost",
                    3306,
                    "originaldb",
                    "originalschema",
                    "originaluser",
                    "originalpass"
            );

            assertEquals("originalhost", properties.host(), "Host should remain unchanged");
            assertEquals(3306, properties.port(), "Port should remain unchanged");
            assertEquals("originaldb", properties.database(), "Database should remain unchanged");
            assertEquals("originalschema", properties.schema(), "Schema should remain unchanged");
            assertEquals("originaluser", properties.username(), "Username should remain unchanged");
            assertEquals("originalpass", properties.password(), "Password should remain unchanged");
        }

        @Test
        @DisplayName("Should maintain same values across multiple accessor calls")
        void shouldMaintainSameValuesAcrossMultipleAccessorCalls() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    "host",
                    9999,
                    "db",
                    "schema",
                    "user",
                    "password"
            );

            assertEquals(properties.host(), properties.host(), "Host should be consistent");
            assertEquals(properties.port(), properties.port(), "Port should be consistent");
            assertEquals(properties.database(), properties.database(), "Database should be consistent");
            assertEquals(properties.schema(), properties.schema(), "Schema should be consistent");
            assertEquals(properties.username(), properties.username(), "Username should be consistent");
            assertEquals(properties.password(), properties.password(), "Password should be consistent");
        }
    }

    @Nested
    @DisplayName("Record Equality and HashCode Tests")
    class RecordEqualityAndHashCodeTests {

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            PostgresqlConnectionProperties properties1 = new PostgresqlConnectionProperties(
                    "host", 5432, "db", "schema", "user", "pass");
            PostgresqlConnectionProperties properties2 = new PostgresqlConnectionProperties(
                    "host", 5432, "db", "schema", "user", "pass");
            PostgresqlConnectionProperties properties3 = new PostgresqlConnectionProperties(
                    "differenthost", 5432, "db", "schema", "user", "pass");

            assertEquals(properties1, properties2, "Records with same values should be equal");
            assertNotEquals(properties1, properties3, "Records with different values should not be equal");
            assertEquals(properties1, properties1, "Record should be equal to itself");
        }

        @Test
        @DisplayName("Should have correct hashCode implementation")
        void shouldHaveCorrectHashCodeImplementation() {
            PostgresqlConnectionProperties properties1 = new PostgresqlConnectionProperties(
                    "host", 5432, "db", "schema", "user", "pass");
            PostgresqlConnectionProperties properties2 = new PostgresqlConnectionProperties(
                    "host", 5432, "db", "schema", "user", "pass");

            assertEquals(properties1.hashCode(), properties2.hashCode(), 
                    "Equal records should have same hash code");
        }

        @Test
        @DisplayName("Should handle null values in equals and hashCode")
        void shouldHandleNullValuesInEqualsAndHashCode() {
            PostgresqlConnectionProperties propertiesWithNulls1 = new PostgresqlConnectionProperties(
                    null, null, null, null, null, null);
            PostgresqlConnectionProperties propertiesWithNulls2 = new PostgresqlConnectionProperties(
                    null, null, null, null, null, null);

            assertEquals(propertiesWithNulls1, propertiesWithNulls2, 
                    "Records with null values should be equal");
            assertEquals(propertiesWithNulls1.hashCode(), propertiesWithNulls2.hashCode(), 
                    "Records with null values should have same hash code");
        }
    }

    @Nested
    @DisplayName("Record toString Tests")
    class RecordToStringTests {

        @Test
        @DisplayName("Should have meaningful toString representation")
        void shouldHaveMeaningfulToStringRepresentation() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    "localhost", 5432, "testdb", "public", "user", "pass");

            String toString = properties.toString();

            assertNotNull(toString, "toString should not be null");
            assertTrue(toString.contains("PostgresqlConnectionProperties"), 
                    "toString should contain class name");
            assertTrue(toString.contains("localhost"), 
                    "toString should contain host value");
            assertTrue(toString.contains("5432"), 
                    "toString should contain port value");
            assertTrue(toString.contains("testdb"), 
                    "toString should contain database value");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            PostgresqlConnectionProperties properties = new PostgresqlConnectionProperties(
                    "host", null, null, "schema", null, "pass");

            assertDoesNotThrow(() -> {
                String toString = properties.toString();
                assertNotNull(toString, "toString should not be null even with null values");
                assertTrue(toString.contains("null"), 
                        "toString should represent null values appropriately");
            }, "toString should not throw with null values");
        }
    }

    @Nested
    @DisplayName("Configuration Properties Integration Tests")
    class ConfigurationPropertiesIntegrationTests {

        @Test
        @DisplayName("Should work as configuration properties data structure")
        void shouldWorkAsConfigurationPropertiesDataStructure() {
            PostgresqlConnectionProperties devProperties = new PostgresqlConnectionProperties(
                    "localhost", 5432, "dev_db", "public", "dev_user", "dev_pass");
            
            PostgresqlConnectionProperties prodProperties = new PostgresqlConnectionProperties(
                    "prod-db.example.com", 5432, "prod_db", "app_schema", "prod_user", "secure_pass");

            assertNotNull(devProperties, "Dev properties should be created");
            assertNotNull(prodProperties, "Prod properties should be created");
            
            assertNotEquals(devProperties, prodProperties, "Different environments should have different configs");
        }

        @Test
        @DisplayName("Should support different port configurations")
        void shouldSupportDifferentPortConfigurations() {
            PostgresqlConnectionProperties standardPort = new PostgresqlConnectionProperties(
                    "host", 5432, "db", "schema", "user", "pass");
            
            PostgresqlConnectionProperties customPort = new PostgresqlConnectionProperties(
                    "host", 5433, "db", "schema", "user", "pass");

            assertEquals(5432, standardPort.port(), "Standard port should be 5432");
            assertEquals(5433, customPort.port(), "Custom port should be preserved");
            assertNotEquals(standardPort, customPort, "Different ports should make records different");
        }
    }
}
