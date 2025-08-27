package co.com.authservice.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateUserDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        String name,

        @NotBlank(message = "El apellido no puede estar vacío")
        String lastname,

        LocalDate birthdayDate,

        String address,

        String phoneNumber,

        @NotNull(message = "El salario base no puede ser nulo")
        BigDecimal baseSalary,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El email debe tener un formato válido")
        String email
) {
}
