package co.com.authservice.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateUserDTO(
        String name,
        String lastname,
        LocalDate birthdayDate,
        String address,
        String phoneNumber,
        BigDecimal baseSalary,
        String email
) {
}
