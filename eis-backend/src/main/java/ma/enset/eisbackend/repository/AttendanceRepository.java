package ma.enset.eisbackend.repository;

import ma.enset.eisbackend.entity.Attendance;
import ma.enset.eisbackend.entity.Attendance.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeId(Long employeeId);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :empId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<Attendance> findByEmployeeAndDateRange(@Param("empId") Long empId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Attendance> findByDateAndStatus(LocalDate date, AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId AND a.status = 'PRESENT' AND a.date BETWEEN :startDate AND :endDate")
    int countPresentDays(@Param("empId") Long empId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    java.util.Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // Dashboard queries
    List<Attendance> findByDate(LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.date = :date AND a.status = :status")
    long countByDateAndStatus(@Param("date") LocalDate date, @Param("status") AttendanceStatus status);

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Attendance a WHERE a.status IN ('PRESENT','REMOTE') AND a.date BETWEEN :startDate AND :endDate")
    long countDistinctPresentEmployees(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
