package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.AttendanceDTO;
import ma.enset.eisbackend.entity.Attendance;
import ma.enset.eisbackend.entity.Attendance.AttendanceStatus;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.repository.AttendanceRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<AttendanceDTO> getEmployeeAttendance(Long empId) {
        log.info("Fetching attendance for employee: {}", empId);
        return attendanceRepository.findByEmployeeId(empId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAttendanceByDateRange(Long empId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching attendance for employee {} from {} to {}", empId, startDate, endDate);
        return attendanceRepository.findByEmployeeAndDateRange(empId, startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "employeeAttendance", allEntries = true)
    public AttendanceDTO recordAttendance(AttendanceDTO dto) {
        log.info("Recording attendance for employee: {}", dto.getEmpId());

        if (attendanceRepository.existsByEmployeeIdAndDate(dto.getEmpId(), dto.getDate())) {
            throw new RuntimeException("Attendance has already been recorded for this employee on this date.");
        }

        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(dto.getDate())
                .status(AttendanceStatus.valueOf(dto.getStatus().toUpperCase()))
                .hoursWorked(dto.getHoursWorked())
                .remarks(dto.getRemarks())
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        AttendanceDTO savedDto = toDTO(saved);
        
        try {
            messagingTemplate.convertAndSend("/topic/attendance", savedDto);
        } catch (Exception e) {
            log.error("Failed to broadcast attendance check-in via WebSocket: {}", e.getMessage());
        }

        return savedDto;
    }

    public double getAttendanceRate(Long empId, int days) {
        log.info("Calculating attendance rate for employee: {}", empId);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        int presentDays = attendanceRepository.countPresentDays(empId, startDate, endDate);
        return (presentDays * 100.0) / days;
    }

    public AttendanceDTO getTodayAttendance(Long empId) {
        log.info("Checking today's attendance for employee: {}", empId);
        java.util.Optional<Attendance> attendance = attendanceRepository.findByEmployeeIdAndDate(empId, LocalDate.now());
        return attendance.map(this::toDTO).orElse(null);
    }

    private AttendanceDTO toDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .id(attendance.getId())
                .empId(attendance.getEmployee().getId())
                .empName(attendance.getEmployee().getName())
                .date(attendance.getDate())
                .status(attendance.getStatus().name())
                .hoursWorked(attendance.getHoursWorked())
                .remarks(attendance.getRemarks())
                .build();
    }
}