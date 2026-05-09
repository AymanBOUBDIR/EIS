package ma.enset.eisbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private BigDecimal salary;
    private Long deptId;
    private String deptName;
    private Long managerId;
    private Double attritionRisk;
    private Boolean isActive;
}
