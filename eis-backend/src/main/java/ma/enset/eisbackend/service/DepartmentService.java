package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.DepartmentDTO;
import ma.enset.eisbackend.entity.Department;
import ma.enset.eisbackend.repository.DepartmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

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
        if (dto.getBudget() != null) department.setBudget(dto.getBudget());

        Department updated = departmentRepository.save(department);
        return toDTO(updated);
    }

    @CacheEvict(value = {"departments", "department"}, allEntries = true)
    public void deleteDepartment(Long id) {
        log.info("Deleting department: {}", id);
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO toDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .deptName(department.getDeptName())
                .description(department.getDescription())
                .budget(department.getBudget())
                .build();
    }
}