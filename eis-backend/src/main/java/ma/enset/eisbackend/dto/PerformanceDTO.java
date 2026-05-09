package ma.enset.eisbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceDTO {
    private Long id;
    private Long empId;
    private String empName;
    private LocalDate reviewDate;
    private Double rating;
    private Long managerId;
    private String managerName;
    private String comments;
    private String status;
}