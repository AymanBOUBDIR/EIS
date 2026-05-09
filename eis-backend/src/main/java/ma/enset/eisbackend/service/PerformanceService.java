package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.PerformanceDTO;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.entity.Performance;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.repository.PerformanceRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final EmployeeRepository employeeRepository;

    public List<PerformanceDTO> getEmployeePerformance(Long empId) {
        log.info("Fetching performance for employee: {}", empId);
        return performanceRepository.findByEmployeeId(empId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "employeePerformance", allEntries = true)
    public PerformanceDTO createPerformance(PerformanceDTO dto) {
        log.info("Creating performance review for employee: {}", dto.getEmpId());

        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Employee manager = null;
        if (dto.getManagerId() != null) {
            manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
        }

        Performance performance = Performance.builder()
                .employee(employee)
                .reviewDate(dto.getReviewDate())
                .rating(dto.getRating())
                .manager(manager)
                .comments(dto.getComments())
                .status(dto.getStatus() != null ? dto.getStatus() : "COMPLETED")
                .build();

        Performance saved = performanceRepository.save(performance);
        return toDTO(saved);
    }

    @CacheEvict(value = "employeePerformance", allEntries = true)
    public PerformanceDTO updatePerformance(Long id, PerformanceDTO dto) {
        log.info("Updating performance: {}", id);

        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Performance record not found"));

        if (dto.getRating() != null) performance.setRating(dto.getRating());
        if (dto.getComments() != null) performance.setComments(dto.getComments());
        if (dto.getStatus() != null) performance.setStatus(dto.getStatus());

        Performance updated = performanceRepository.save(performance);
        return toDTO(updated);
    }

    private PerformanceDTO toDTO(Performance performance) {
        return PerformanceDTO.builder()
                .id(performance.getId())
                .empId(performance.getEmployee().getId())
                .empName(performance.getEmployee().getName())
                .reviewDate(performance.getReviewDate())
                .rating(performance.getRating())
                .managerId(performance.getManager() != null ? performance.getManager().getId() : null)
                .managerName(performance.getManager() != null ? performance.getManager().getName() : null)
                .comments(performance.getComments())
                .status(performance.getStatus())
                .build();
    }
}