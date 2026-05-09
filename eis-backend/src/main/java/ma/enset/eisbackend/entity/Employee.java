package ma.enset.eisbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_dept_id", columnList = "dept_id"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_hire_date", columnList = "hire_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal salary;

    @ManyToOne
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Performance> performances;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendances;

    @Column(name = "attrition_risk")
    private Double attritionRisk;

    @Column(name = "is_active")
    private Boolean isActive = true;
}