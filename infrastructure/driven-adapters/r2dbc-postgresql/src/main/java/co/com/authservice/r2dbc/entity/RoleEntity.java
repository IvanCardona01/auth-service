package co.com.authservice.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("roles")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
    @Id
    private Long id;
    private String name;
    private String description;

    @Column("created_at")
    private LocalDateTime createdAt;
}
