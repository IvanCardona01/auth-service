package co.com.authservice.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    private Long id;

    @Column("document_number")
    private String documentNumber;
    private String name;
    private String lastname;

    @Column("birthday_date")
    private LocalDate birthdayDate;
    private String address;
    @Column("phone_number")
    private String phoneNumber;
    @Column("base_salary")
    private BigDecimal baseSalary;

    private String email;

    private String password;

    @Column("role_id")
    private Long roleId;
}
