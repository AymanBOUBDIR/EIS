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
public class AttendanceDTO {
    private Long id;
    private Long empId;
    private String empName;
    private LocalDate date;
    private String status;
    private Double hoursWorked;
    private String remarks;
}