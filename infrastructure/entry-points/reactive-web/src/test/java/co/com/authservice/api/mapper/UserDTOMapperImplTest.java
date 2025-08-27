package co.com.authservice.api.mapper;

import co.com.authservice.api.dto.request.CreateUserDTO;
import co.com.authservice.api.dto.response.RoleResponseDTO;
import co.com.authservice.api.dto.response.UserResponseDTO;
import co.com.authservice.model.role.Role;
import co.com.authservice.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("UserDTOMapperImpl - MapStruct Generated Mapper Tests")
class UserDTOMapperImplTest {

    private UserDTOMapperImpl mapper;

    private final Long USER_ID = 1L;
    private final String USER_NAME = "Juan";
    private final String USER_LASTNAME = "Pérez";
    private final LocalDate USER_BIRTHDAY = LocalDate.of(1990, 5, 15);
    private final String USER_ADDRESS = "Calle 123 #45-67";
    private final String USER_PHONE = "+57 300 123 4567";
    private final BigDecimal USER_SALARY = new BigDecimal("5000000.00");
    private final String USER_EMAIL = "juan.perez@email.com";

    private final Long ROLE_ID = 2L;
    private final String ROLE_NAME = "CLIENT";
    private final String ROLE_DESCRIPTION = "Regular client user";

    @BeforeEach
    void setUp() {
        mapper = new UserDTOMapperImpl();
    }

    @Nested
    @DisplayName("CreateUserDTO to User Conversion Tests")
    class CreateUserDTOToUserTests {

        @Test
        @DisplayName("Should convert CreateUserDTO to User with all fields correctly mapped")
        void shouldConvertCreateUserDTOToUserSuccessfully() {
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    USER_NAME, 
                    USER_LASTNAME, 
                    USER_BIRTHDAY, 
                    USER_ADDRESS, 
                    USER_PHONE, 
                    USER_SALARY, 
                    USER_EMAIL
            );

            User result = mapper.toModel(createUserDTO);

            assertNotNull(result, "Result should not be null");
            assertEquals(USER_NAME, result.getName(), "Name should be mapped correctly");
            assertEquals(USER_LASTNAME, result.getLastname(), "Lastname should be mapped correctly");
            assertEquals(USER_BIRTHDAY, result.getBirthdayDate(), "Birthday date should be mapped correctly");
            assertEquals(USER_ADDRESS, result.getAddress(), "Address should be mapped correctly");
            assertEquals(USER_PHONE, result.getPhoneNumber(), "Phone number should be mapped correctly");
            assertEquals(USER_SALARY, result.getBaseSalary(), "Base salary should be mapped correctly");
            assertEquals(USER_EMAIL, result.getEmail(), "Email should be mapped correctly");

            assertNull(result.getId(), "ID should be null (not in CreateUserDTO)");
            assertNull(result.getRole(), "Role should be null (not in CreateUserDTO)");
        }

        @Test
        @DisplayName("Should handle null CreateUserDTO input")
        void shouldHandleNullCreateUserDTOInput() {
            User result = mapper.toModel(null);

            assertNull(result, "Result should be null when input is null");
        }

        @Test
        @DisplayName("Should convert CreateUserDTO with null optional fields")
        void shouldConvertCreateUserDTOWithNullOptionalFields() {
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    USER_NAME, 
                    USER_LASTNAME, 
                    USER_BIRTHDAY, 
                    null,
                    null,
                    USER_SALARY, 
                    USER_EMAIL
            );

            User result = mapper.toModel(createUserDTO);

            assertNotNull(result);
            assertEquals(USER_NAME, result.getName());
            assertEquals(USER_LASTNAME, result.getLastname());
            assertEquals(USER_BIRTHDAY, result.getBirthdayDate());
            assertNull(result.getAddress());
            assertNull(result.getPhoneNumber());
            assertEquals(USER_SALARY, result.getBaseSalary());
            assertEquals(USER_EMAIL, result.getEmail());
        }
    }

    @Nested
    @DisplayName("User to UserResponseDTO Conversion Tests")
    class UserToResponseDTOTests {

        @Test
        @DisplayName("Should convert User to UserResponseDTO with all fields correctly mapped")
        void shouldConvertUserToUserResponseDTOSuccessfully() {
            Role role = Role.builder()
                    .id(ROLE_ID)
                    .name(ROLE_NAME)
                    .description(ROLE_DESCRIPTION)
                    .build();

            User user = User.builder()
                    .id(USER_ID)
                    .name(USER_NAME)
                    .lastname(USER_LASTNAME)
                    .birthdayDate(USER_BIRTHDAY)
                    .address(USER_ADDRESS)
                    .phoneNumber(USER_PHONE)
                    .baseSalary(USER_SALARY)
                    .email(USER_EMAIL)
                    .role(role)
                    .build();

            UserResponseDTO result = mapper.toResponse(user);

            assertNotNull(result, "Result should not be null");
            assertEquals(USER_ID, result.id(), "ID should be mapped correctly");
            assertEquals(USER_NAME, result.name(), "Name should be mapped correctly");
            assertEquals(USER_LASTNAME, result.lastname(), "Lastname should be mapped correctly");
            assertEquals(USER_BIRTHDAY, result.birthdayDate(), "Birthday date should be mapped correctly");
            assertEquals(USER_ADDRESS, result.address(), "Address should be mapped correctly");
            assertEquals(USER_PHONE, result.phoneNumber(), "Phone number should be mapped correctly");
            assertEquals(USER_SALARY, result.baseSalary(), "Base salary should be mapped correctly");
            assertEquals(USER_EMAIL, result.email(), "Email should be mapped correctly");

            assertNotNull(result.role(), "Role should not be null");
            assertEquals(ROLE_ID, result.role().id(), "Role ID should be mapped correctly");
            assertEquals(ROLE_NAME, result.role().name(), "Role name should be mapped correctly");
            assertEquals(ROLE_DESCRIPTION, result.role().description(), "Role description should be mapped correctly");
        }

        @Test
        @DisplayName("Should handle null User input")
        void shouldHandleNullUserInput() {
            UserResponseDTO result = mapper.toResponse((User) null);

            assertNull(result, "Result should be null when input is null");
        }

        @Test
        @DisplayName("Should convert User with null role")
        void shouldConvertUserWithNullRole() {
            User user = User.builder()
                    .id(USER_ID)
                    .name(USER_NAME)
                    .lastname(USER_LASTNAME)
                    .birthdayDate(USER_BIRTHDAY)
                    .address(USER_ADDRESS)
                    .phoneNumber(USER_PHONE)
                    .baseSalary(USER_SALARY)
                    .email(USER_EMAIL)
                    .role(null)
                    .build();

            UserResponseDTO result = mapper.toResponse(user);

            assertNotNull(result);
            assertEquals(USER_ID, result.id());
            assertEquals(USER_NAME, result.name());
            assertEquals(USER_LASTNAME, result.lastname());
            assertNull(result.role(), "Role should be null when user has no role");
        }

        @Test
        @DisplayName("Should convert User with some null fields")
        void shouldConvertUserWithSomeNullFields() {
            User user = User.builder()
                    .id(USER_ID)
                    .name(USER_NAME)
                    .lastname(USER_LASTNAME)
                    .birthdayDate(USER_BIRTHDAY)
                    .address(null)
                    .phoneNumber(null)
                    .baseSalary(USER_SALARY)
                    .email(USER_EMAIL)
                    .role(null)
                    .build();

            UserResponseDTO result = mapper.toResponse(user);

            assertNotNull(result);
            assertEquals(USER_ID, result.id());
            assertEquals(USER_NAME, result.name());
            assertEquals(USER_LASTNAME, result.lastname());
            assertEquals(USER_BIRTHDAY, result.birthdayDate());
            assertNull(result.address());
            assertNull(result.phoneNumber());
            assertEquals(USER_SALARY, result.baseSalary());
            assertEquals(USER_EMAIL, result.email());
            assertNull(result.role());
        }
    }

    @Nested
    @DisplayName("Role to RoleResponseDTO Conversion Tests")
    class RoleToResponseDTOTests {

        @Test
        @DisplayName("Should convert Role to RoleResponseDTO with all fields correctly mapped")
        void shouldConvertRoleToRoleResponseDTOSuccessfully() {
            Role role = Role.builder()
                    .id(ROLE_ID)
                    .name(ROLE_NAME)
                    .description(ROLE_DESCRIPTION)
                    .build();

            RoleResponseDTO result = mapper.toResponse(role);

            assertNotNull(result, "Result should not be null");
            assertEquals(ROLE_ID, result.id(), "Role ID should be mapped correctly");
            assertEquals(ROLE_NAME, result.name(), "Role name should be mapped correctly");
            assertEquals(ROLE_DESCRIPTION, result.description(), "Role description should be mapped correctly");
        }

        @Test
        @DisplayName("Should handle null Role input")
        void shouldHandleNullRoleInput() {
            RoleResponseDTO result = mapper.toResponse((Role) null);

            assertNull(result, "Result should be null when input is null");
        }

        @Test
        @DisplayName("Should convert Role with null optional fields")
        void shouldConvertRoleWithNullOptionalFields() {
            Role role = Role.builder()
                    .id(ROLE_ID)
                    .name(ROLE_NAME)
                    .description(null)
                    .build();

            RoleResponseDTO result = mapper.toResponse(role);

            assertNotNull(result);
            assertEquals(ROLE_ID, result.id());
            assertEquals(ROLE_NAME, result.name());
            assertNull(result.description(), "Description should be null when role description is null");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle extreme BigDecimal values")
        void shouldHandleExtremeBigDecimalValues() {
            BigDecimal extremeSalary = new BigDecimal("999999999999.99");
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    USER_NAME, 
                    USER_LASTNAME, 
                    USER_BIRTHDAY, 
                    USER_ADDRESS, 
                    USER_PHONE, 
                    extremeSalary, 
                    USER_EMAIL
            );

            User result = mapper.toModel(createUserDTO);

            assertNotNull(result);
            assertEquals(extremeSalary, result.getBaseSalary(), "Extreme BigDecimal value should be preserved");
        }

        @Test
        @DisplayName("Should handle extreme LocalDate values")
        void shouldHandleExtremeLocalDateValues() {
            LocalDate extremeDate = LocalDate.of(1900, 1, 1);
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    USER_NAME, 
                    USER_LASTNAME, 
                    extremeDate, 
                    USER_ADDRESS, 
                    USER_PHONE, 
                    USER_SALARY, 
                    USER_EMAIL
            );

            User result = mapper.toModel(createUserDTO);

            assertNotNull(result);
            assertEquals(extremeDate, result.getBirthdayDate(), "Extreme LocalDate value should be preserved");
        }

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            String longName = "A".repeat(1000);
            String longEmail = "test" + "@".repeat(100) + "domain.com";
            
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    longName, 
                    USER_LASTNAME, 
                    USER_BIRTHDAY, 
                    USER_ADDRESS, 
                    USER_PHONE, 
                    USER_SALARY, 
                    longEmail
            );

            User result = mapper.toModel(createUserDTO);

            assertNotNull(result);
            assertEquals(longName, result.getName(), "Long name should be preserved");
            assertEquals(longEmail, result.getEmail(), "Long email should be preserved");
        }

        @Test
        @DisplayName("Should work correctly in complete round-trip conversion")
        void shouldWorkCorrectlyInCompleteRoundTripConversion() {
            CreateUserDTO originalDTO = new CreateUserDTO(
                    USER_NAME, 
                    USER_LASTNAME, 
                    USER_BIRTHDAY, 
                    USER_ADDRESS, 
                    USER_PHONE, 
                    USER_SALARY, 
                    USER_EMAIL
            );

            Role role = Role.builder()
                    .id(ROLE_ID)
                    .name(ROLE_NAME)
                    .description(ROLE_DESCRIPTION)
                    .build();

            User user = mapper.toModel(originalDTO);

            user = User.builder()
                    .id(USER_ID)
                    .name(user.getName())
                    .lastname(user.getLastname())
                    .birthdayDate(user.getBirthdayDate())
                    .address(user.getAddress())
                    .phoneNumber(user.getPhoneNumber())
                    .baseSalary(user.getBaseSalary())
                    .email(user.getEmail())
                    .role(role)
                    .build();
            
            UserResponseDTO responseDTO = mapper.toResponse(user);

            assertNotNull(responseDTO);
            assertEquals(USER_ID, responseDTO.id());
            assertEquals(originalDTO.name(), responseDTO.name());
            assertEquals(originalDTO.lastname(), responseDTO.lastname());
            assertEquals(originalDTO.birthdayDate(), responseDTO.birthdayDate());
            assertEquals(originalDTO.address(), responseDTO.address());
            assertEquals(originalDTO.phoneNumber(), responseDTO.phoneNumber());
            assertEquals(originalDTO.baseSalary(), responseDTO.baseSalary());
            assertEquals(originalDTO.email(), responseDTO.email());

            assertNotNull(responseDTO.role());
            assertEquals(ROLE_ID, responseDTO.role().id());
            assertEquals(ROLE_NAME, responseDTO.role().name());
            assertEquals(ROLE_DESCRIPTION, responseDTO.role().description());
        }
    }
}
