package co.com.authservice.model.user;
import co.com.authservice.model.role.Role;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private Long id;

    private String documentNumber;
    private String name;
    private String lastname;
    private LocalDate birthdayDate;
    private String address;
    private String phoneNumber;
    private String email;
    private String password;
    private BigDecimal baseSalary;
    private Role role;
}
