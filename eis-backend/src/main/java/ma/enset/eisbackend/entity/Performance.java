package ma.enset.eisbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "performance", indexes = {
        @Index(name = "idx_emp_id", columnList = "emp_id"),
        @Index(name = "idx_review_date", columnList = "review_date"),
        @Index(name = "idx_manager_id", columnList = "manager_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Performance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    private Employee employee;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(nullable = false)
    private Double rating; // 1-5

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(length = 50)
    private String status; // PENDING, COMPLETED, etc.
}