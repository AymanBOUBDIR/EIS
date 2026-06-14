package ma.enset.eisbackend.repository;

import ma.enset.eisbackend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(Long departmentId);

    @Query("SELECT e FROM Employee e WHERE e.salary >= :minSalary ORDER BY e.salary DESC")
    List<Employee> findHighEarners(@Param("minSalary") BigDecimal minSalary);

    @Query("SELECT e FROM Employee e WHERE e.attritionRisk >= :threshold ORDER BY e.attritionRisk DESC")
    List<Employee> findAtRiskEmployees(@Param("threshold") Double threshold);

    List<Employee> findByIsActiveTrue();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :deptId")
    int countByDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department.id = :deptId AND e.isActive = true")
    Double getAverageSalaryByDepartmentId(@Param("deptId") Long deptId);
}
