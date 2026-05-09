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
}
