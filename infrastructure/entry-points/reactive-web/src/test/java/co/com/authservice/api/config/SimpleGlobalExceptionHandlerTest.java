package co.com.authservice.api.config;

import co.com.authservice.model.user.exceptions.user.EmailAlreadyExistsException;
import co.com.authservice.model.user.exceptions.user.InvalidAgeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Basic Coverage Tests")
class SimpleGlobalExceptionHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(objectMapper);
    }

    @Test
    @DisplayName("Should create handler instance")
    void shouldCreateHandlerInstance() {
        assertNotNull(exceptionHandler);
    }

    @Test
    @DisplayName("Should map EmailAlreadyExistsException to CONFLICT status")
    void shouldMapEmailAlreadyExistsExceptionToConflictStatus() throws Exception {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Email already exists");

        Method determineErrorMethod = GlobalExceptionHandler.class.getDeclaredMethod("determineError", Throwable.class);
        determineErrorMethod.setAccessible(true);
        Object errorInfo = determineErrorMethod.invoke(exceptionHandler, exception);

        Method statusMethod = errorInfo.getClass().getDeclaredMethod("status");
        HttpStatus status = (HttpStatus) statusMethod.invoke(errorInfo);
        
        assertEquals(HttpStatus.CONFLICT, status);
    }

    @Test
    @DisplayName("Should map InvalidAgeException to BAD_REQUEST status")
    void shouldMapInvalidAgeExceptionToBadRequestStatus() throws Exception {
        InvalidAgeException exception = new InvalidAgeException(17);

        Method determineErrorMethod = GlobalExceptionHandler.class.getDeclaredMethod("determineError", Throwable.class);
        determineErrorMethod.setAccessible(true);
        Object errorInfo = determineErrorMethod.invoke(exceptionHandler, exception);

        Method statusMethod = errorInfo.getClass().getDeclaredMethod("status");
        HttpStatus status = (HttpStatus) statusMethod.invoke(errorInfo);
        
        assertEquals(HttpStatus.BAD_REQUEST, status);
    }

    @Test
    @DisplayName("Should extract validation messages")
    void shouldExtractValidationMessages() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "Name is required"));
        WebExchangeBindException exception = new WebExchangeBindException(null, bindingResult);

        Method extractMethod = GlobalExceptionHandler.class.getDeclaredMethod("extractValidationMessage", WebExchangeBindException.class);
        extractMethod.setAccessible(true);
        String message = (String) extractMethod.invoke(exceptionHandler, exception);

        assertTrue(message.contains("name: Name is required"));
    }

    @Test
    @DisplayName("Should handle generic exceptions")
    void shouldHandleGenericExceptions() throws Exception {
        RuntimeException exception = new RuntimeException("Generic error");

        Method determineErrorMethod = GlobalExceptionHandler.class.getDeclaredMethod("determineError", Throwable.class);
        determineErrorMethod.setAccessible(true);
        Object errorInfo = determineErrorMethod.invoke(exceptionHandler, exception);

        Method statusMethod = errorInfo.getClass().getDeclaredMethod("status");
        HttpStatus status = (HttpStatus) statusMethod.invoke(errorInfo);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, status);
    }

    @Test
    @DisplayName("Should use fallback when serialization fails")
    void shouldUseFallbackWhenSerializationFails() throws Exception {
        co.com.authservice.api.dto.response.ErrorResponseDTO errorResponse = 
            co.com.authservice.api.dto.response.ErrorResponseDTO.of("TEST_CODE", "Test message", "/test/path");

        when(objectMapper.writeValueAsBytes(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new RuntimeException("Serialization failed"));

        Method serializeErrorMethod = GlobalExceptionHandler.class.getDeclaredMethod("serializeError", co.com.authservice.api.dto.response.ErrorResponseDTO.class);
        serializeErrorMethod.setAccessible(true);
        byte[] result = (byte[]) serializeErrorMethod.invoke(exceptionHandler, errorResponse);

        String resultString = new String(result);
        assertTrue(resultString.contains("SERIALIZATION_ERROR"));
    }
}
