package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.LeaveRequestDTO;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.entity.LeaveRequest;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public List<LeaveRequestDTO> getUpcomingLeaves() {
        log.info("Fetching upcoming approved leaves");
        return leaveRequestRepository.findUpcomingApprovedLeaves(LocalDate.now()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDTO> getEmployeeLeaves(Long empId) {
        log.info("Fetching leaves for employee: {}", empId);
        return leaveRequestRepository.findByEmployeeId(empId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO dto) {
        log.info("Creating leave request for employee: {}", dto.getEmpId());

        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check for overlapping leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                dto.getEmpId(), dto.getStartDate(), dto.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Un congé est déjà approuvé pour cette période.");
        }

        LeaveRequest leave = LeaveRequest.builder()
                .employee(employee)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .type(LeaveRequest.LeaveType.valueOf(dto.getType()))
                .status(LeaveRequest.LeaveStatus.APPROVED)
                .reason(dto.getReason())
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return toDTO(saved);
    }

    public LeaveRequestDTO updateStatus(Long id, String status) {
        log.info("Updating leave request {} status to {}", id, status);
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        leave.setStatus(LeaveRequest.LeaveStatus.valueOf(status));
        return toDTO(leaveRequestRepository.save(leave));
    }

    private LeaveRequestDTO toDTO(LeaveRequest leave) {
        return LeaveRequestDTO.builder()
                .id(leave.getId())
                .empId(leave.getEmployee().getId())
                .empName(leave.getEmployee().getName())
                .empRole(leave.getEmployee().getDepartment() != null ? leave.getEmployee().getDepartment().getDeptName() : "")
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .type(leave.getType().name())
                .status(leave.getStatus().name())
                .reason(leave.getReason())
                .build();
    }
}
