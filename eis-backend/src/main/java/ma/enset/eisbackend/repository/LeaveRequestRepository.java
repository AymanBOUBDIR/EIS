package ma.enset.eisbackend.repository;

import ma.enset.eisbackend.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'APPROVED' AND l.endDate >= :today ORDER BY l.startDate ASC")
    List<LeaveRequest> findUpcomingApprovedLeaves(@Param("today") LocalDate today);

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'APPROVED' AND l.startDate <= :date AND l.endDate >= :date")
    List<LeaveRequest> findActiveLeavesByDate(@Param("date") LocalDate date);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.id = :empId AND l.status = 'APPROVED' AND ((l.startDate BETWEEN :startDate AND :endDate) OR (l.endDate BETWEEN :startDate AND :endDate))")
    List<LeaveRequest> findOverlappingLeaves(@Param("empId") Long empId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
