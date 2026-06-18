package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.EmployeeDTO;
import ma.enset.eisbackend.entity.Department;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.entity.Performance;
import ma.enset.eisbackend.repository.DepartmentRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

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
    private final AttendanceRepository attendanceRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    @Cacheable(value = "employees")
    public List<EmployeeDTO> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "employee", key = "#id")
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

    @Cacheable(value = "employees", key = "'high-earners-' + #minSalary")
    public List<EmployeeDTO> getHighEarners(BigDecimal minSalary) {
        log.info("Fetching high earners with minimum salary: {}", minSalary);
        return employeeRepository.findHighEarners(minSalary).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "employees", key = "'at-risk-' + #threshold")
    public List<EmployeeDTO> getAttritionRisk(Double threshold) {
        log.info("Fetching employees with attrition risk >= {}", threshold);
        return employeeRepository.findAtRiskEmployees(threshold).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"employees", "employee"}, allEntries = true)
    public void updateEmployeeAttritionRisk(Long employeeId) {
        log.info("Force recalculating and saving attrition risk for employee: {}", employeeId);
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee != null) {
            toDTO(employee); // This triggers the calculation and sets e.attritionRisk
            employeeRepository.save(employee); // Persist it to the database
        }
    }

    @Cacheable(value = "employees", key = "'dept-' + #deptId")
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
        double attendanceRisk = 0.0;
        double performanceRisk = 0.0;
        double salaryRisk = 0.0;

        // 1. Attendance Factor
        double attendanceRate = 100.0;
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            int presentDays = attendanceRepository.countPresentDays(employee.getId(), startDate, endDate);
            attendanceRate = (presentDays * 100.0) / 30.0;
        } catch (Exception e) {
            log.error("Error calculating attendance rate for employee risk: ", e);
        }
        if (attendanceRate < 50.0) attendanceRisk = 40.0;
        else if (attendanceRate < 75.0) attendanceRisk = 25.0;
        else if (attendanceRate < 90.0) attendanceRisk = 10.0;

        // 2. Performance Factor
        List<Performance> performances = employee.getPerformances();
        double avgRating = 5.0;
        if (performances != null && !performances.isEmpty()) {
            avgRating = performances.stream()
                    .mapToDouble(Performance::getRating)
                    .average()
                    .orElse(5.0);
            if (avgRating < 2.5) performanceRisk = 30.0;
            else if (avgRating < 3.5) performanceRisk = 15.0;
        }

        // 3. Salary Factor
        double salaryRatio = 1.0;
        if (employee.getDepartment() != null && employee.getSalary() != null) {
            try {
                Double avgSalary = employeeRepository.getAverageSalaryByDepartmentId(employee.getDepartment().getId());
                if (avgSalary != null && avgSalary > 0) {
                    salaryRatio = employee.getSalary().doubleValue() / avgSalary;
                    if (salaryRatio < 0.7) salaryRisk = 30.0;
                    else if (salaryRatio < 0.85) salaryRisk = 15.0;
                }
            } catch (Exception e) {
                log.error("Error calculating salary ratio for employee risk: ", e);
            }
        }

        double totalRisk = Math.min(attendanceRisk + performanceRisk + salaryRisk, 100.0);

        MlPredictionResponse mlResponse = callMlService(attendanceRate, avgRating, salaryRatio);
        if (mlResponse != null) {
            log.debug("ML predicted attrition risk for employee ID {}: {}% (previously calculated rule-based risk: {}%)", 
                employee.getId(), mlResponse.getAttrition_probability(), totalRisk);
            totalRisk = mlResponse.getAttrition_probability();
        }

        employee.setAttritionRisk(totalRisk);

        return EmployeeDTO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .hireDate(employee.getHireDate())
                .salary(employee.getSalary())
                .deptId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .deptName(employee.getDepartment() != null ? employee.getDepartment().getDeptName() : null)
                .managerId(employee.getManager() != null ? employee.getManager().getId() : null)
                .attritionRisk(totalRisk)
                .attendanceRiskContribution(attendanceRisk)
                .performanceRiskContribution(performanceRisk)
                .salaryRiskContribution(salaryRisk)
                .isActive(employee.getIsActive())
                .photoUrl(employee.getPhotoUrl())
                .build();
    }

    private MlPredictionResponse callMlService(double attendanceRate, double avgRating, double salaryRatio) {
        try {
            String url = mlServiceUrl + "/predict";
            MlPredictionRequest request = new MlPredictionRequest(attendanceRate, avgRating, salaryRatio);
            return restTemplate.postForObject(url, request, MlPredictionResponse.class);
        } catch (Exception e) {
            log.warn("ML service call failed, falling back to rule-based risk calculation. Error: {}", e.getMessage());
            return null;
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MlPredictionRequest {
        private double attendance_rate;
        private double performance_rating;
        private double salary_ratio;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MlPredictionResponse {
        private double attrition_probability;
        private String attrition_risk_class;
        private String recommendation;
    }
}
