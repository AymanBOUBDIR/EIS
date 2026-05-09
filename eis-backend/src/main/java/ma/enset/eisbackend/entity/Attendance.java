package ma.enset.eisbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "attendance", indexes = {
        @Index(name = "idx_att_emp_id", columnList = "emp_id"),
        @Index(name = "idx_att_date", columnList = "date"),
        @Index(name = "idx_emp_date", columnList = "emp_id,date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status; // PRESENT, ABSENT, LATE, REMOTE

    @Column
    private Double hoursWorked;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE, REMOTE
    }
}