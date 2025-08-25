package co.com.authservice.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserResponseDTO(
        String name,
        String lastName,
        LocalDate birthdayDate,
        String address,
        String phoneNumber,
        BigDecimal baseSalary,
        String email
) {
}
