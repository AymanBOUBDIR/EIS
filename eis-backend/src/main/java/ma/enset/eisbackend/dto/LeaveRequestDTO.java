package ma.enset.eisbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long empId;
    private String empName;
    private String empRole; // department name as role
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String status;
    private String reason;
}
