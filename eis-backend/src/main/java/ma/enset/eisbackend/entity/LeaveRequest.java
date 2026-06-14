package ma.enset.eisbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests", indexes = {
        @Index(name = "idx_leave_emp_id", columnList = "emp_id"),
        @Index(name = "idx_leave_dates", columnList = "start_date,end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeaveType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.APPROVED;

    @Column(columnDefinition = "TEXT")
    private String reason;

    public enum LeaveType {
        VACANCES, CONGE_MALADIE, CONGE_PERSONNEL, FORMATION, MATERNITE, PATERNITE, SANS_SOLDE
    }

    public enum LeaveStatus {
        EN_ATTENTE, APPROVED, REJECTED, CANCELLED
    }
}
