package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.EmployeeDTO;
import ma.enset.eisbackend.entity.Department;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.repository.DepartmentRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public List<EmployeeDTO> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);
        return employeeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        log.info("Creating new employee: {}", dto.getName());

        Department dept = departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Employee employee = Employee.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .hireDate(dto.getHireDate())
                .salary(dto.getSalary())
                .department(dept)
                .isActive(true)
                .attritionRisk(0.0)
                .build();

        Employee saved = employeeRepository.save(employee);
        return toDTO(saved);
    }

    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        log.info("Updating employee: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (dto.getName() != null) employee.setName(dto.getName());
        if (dto.getEmail() != null) employee.setEmail(dto.getEmail());
        if (dto.getPhone() != null) employee.setPhone(dto.getPhone());
        if (dto.getSalary() != null) employee.setSalary(dto.getSalary());
        if (dto.getHireDate() != null) employee.setHireDate(dto.getHireDate());
        if (dto.getPhotoUrl() != null) employee.setPhotoUrl(dto.getPhotoUrl());

        if (dto.getDeptId() != null) {
            Department dept = departmentRepository.findById(dto.getDeptId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employee.setDepartment(dept);
        }

        Employee updated = employeeRepository.save(employee);
        return toDTO(updated);
    }

    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void deleteEmployee(Long id) {
        log.info("Deleting employee: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    public List<EmployeeDTO> getHighEarners(BigDecimal minSalary) {
        log.info("Fetching high earners with minimum salary: {}", minSalary);
        return employeeRepository.findHighEarners(minSalary).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getAttritionRisk(Double threshold) {
        log.info("Fetching employees with attrition risk >= {}", threshold);
        return employeeRepository.findAtRiskEmployees(threshold).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getByDepartment(Long deptId) {
        log.info("Fetching employees by department: {}", deptId);
        return employeeRepository.findByDepartmentId(deptId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public EmployeeDTO uploadPhoto(Long id, MultipartFile file) {
        log.info("Uploading photo for employee: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        try {
            // Create uploads directory if it doesn't exist
            Path uploadDir = Paths.get("uploads/employee-photos");
            Files.createDirectories(uploadDir);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = "emp_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            // Save file
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update employee photo URL
            String photoUrl = "/api/v1/uploads/employee-photos/" + filename;
            employee.setPhotoUrl(photoUrl);
            Employee updated = employeeRepository.save(employee);
            return toDTO(updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store photo: " + e.getMessage());
        }
    }

    private EmployeeDTO toDTO(Employee employee) {
        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .hireDate(employee.getHireDate())
                .salary(employee.getSalary())
                .deptId(employee.getDepartment().getId())
                .deptName(employee.getDepartment().getDeptName())
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .attritionRisk(employee.getAttritionRisk())
                .isActive(employee.getIsActive())
                .photoUrl(employee.getPhotoUrl())
                .build();
    }
}
