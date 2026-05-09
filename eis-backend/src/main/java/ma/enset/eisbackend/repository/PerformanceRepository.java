package ma.enset.eisbackend.repository;

import ma.enset.eisbackend.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    List<Performance> findByEmployeeId(Long employeeId);

    @Query("SELECT p FROM Performance p WHERE p.employee.id = :empId AND p.reviewDate >= :startDate ORDER BY p.reviewDate DESC")
    List<Performance> findByEmployeeIdAndDateRange(@Param("empId") Long empId, @Param("startDate") LocalDate startDate);

    @Query("SELECT AVG(p.rating) FROM Performance p WHERE p.employee.department.id = :deptId")
    Double getAverageDepartmentRating(@Param("deptId") Long deptId);
}