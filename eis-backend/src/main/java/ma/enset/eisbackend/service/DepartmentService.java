package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.DepartmentDTO;
import ma.enset.eisbackend.entity.Department;
import ma.enset.eisbackend.repository.DepartmentRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // Removed @Cacheable - causing issues
    public List<DepartmentDTO> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Removed @Cacheable
    public DepartmentDTO getDepartmentById(Long id) {
        log.info("Fetching department with ID: {}", id);
        return departmentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }

    @CacheEvict(value = {"departments", "department"}, allEntries = true)
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        log.info("Creating new department: {}", dto.getDeptName());

        if (dto.getBudget() != null && dto.getBudget().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Department budget cannot be negative.");
        }

        Department department = Department.builder()
                .deptName(dto.getDeptName())
                .description(dto.getDescription())
                .budget(dto.getBudget())
                .build();

        Department saved = departmentRepository.save(department);
        return toDTO(saved);
    }

    @CacheEvict(value = {"departments", "department"}, allEntries = true)
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        log.info("Updating department: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (dto.getDeptName() != null) department.setDeptName(dto.getDeptName());
        if (dto.getDescription() != null) department.setDescription(dto.getDescription());
        if (dto.getBudget() != null) {
            if (dto.getBudget().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Department budget cannot be negative.");
            }
            department.setBudget(dto.getBudget());
        }

        Department updated = departmentRepository.save(department);
        return toDTO(updated);
    }

    @CacheEvict(value = {"departments", "department"}, allEntries = true)
    public void deleteDepartment(Long id) {
        log.info("Deleting department: {}", id);
        if (employeeRepository.countByDepartmentId(id) > 0) {
            throw new RuntimeException("Cannot delete department because it still has active employees assigned to it.");
        }
        departmentRepository.deleteById(id);
    }

    @CacheEvict(value = {"employees", "employee", "departments", "department"}, allEntries = true)
    public void applyRaise(Long id, double percentage) {
        log.info("Applying raise of {}% to department ID: {}", percentage * 100, id);
        List<ma.enset.eisbackend.entity.Employee> employees = employeeRepository.findByDepartmentId(id);
        for (ma.enset.eisbackend.entity.Employee emp : employees) {
            if (Boolean.TRUE.equals(emp.getIsActive()) && emp.getSalary() != null) {
                BigDecimal increment = emp.getSalary().multiply(BigDecimal.valueOf(1 + percentage));
                emp.setSalary(increment);
                employeeRepository.save(emp);
            }
        }
    }

    private DepartmentDTO toDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .budget(department.getBudget())
                .employeeCount(employeeRepository.countByDepartmentId(department.getId()))
                .build();
    }
}