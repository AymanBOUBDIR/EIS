package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.TicketDTO;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.entity.Ticket;
import ma.enset.eisbackend.repository.TicketRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;

    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TicketDTO> getTicketsByStatus(Ticket.TicketStatus status) {
        return ticketRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TicketDTO createFollowUpTicket(Employee employee, double attendanceRate) {
        // Prevent duplicate OPEN tickets for the same employee
        if (ticketRepository.findOpenTicketByEmployeeId(employee.getId()).isPresent()) {
            log.info("An OPEN ticket already exists for employee ID: {}", employee.getId());
            return null;
        }

        Ticket ticket = Ticket.builder()
                .employee(employee)
                .shortDescription("Low Attendance Alert")
                .description("Automated Alert: Employee attendance rate has dropped to " + 
                             String.format("%.1f", attendanceRate) + "%. Follow-up required.")
                .status(Ticket.TicketStatus.OPEN)
                .priority(Ticket.TicketPriority.HIGH)
                .build();

        log.warn("Created new OPEN ticket for employee ID: {} due to low attendance", employee.getId());
        return toDTO(ticketRepository.save(ticket));
    }

    public TicketDTO createTicket(TicketDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmpId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Employee assignedTo = null;
        if (dto.getAssignedToId() != null) {
            assignedTo = employeeRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned employee not found"));
        }

        Ticket.TicketPriority priority = Ticket.TicketPriority.MEDIUM;
        if (dto.getPriority() != null) {
            priority = Ticket.TicketPriority.valueOf(dto.getPriority().toUpperCase());
        }

        Ticket ticket = Ticket.builder()
                .employee(employee)
                .shortDescription(dto.getShortDescription())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? Ticket.TicketStatus.valueOf(dto.getStatus().toUpperCase()) : Ticket.TicketStatus.OPEN)
                .priority(priority)
                .assignedTo(assignedTo)
                .build();

        if (ticket.getStatus() == Ticket.TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        return toDTO(ticketRepository.save(ticket));
    }

    public TicketDTO updateTicket(Long id, TicketDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (dto.getShortDescription() != null) {
            ticket.setShortDescription(dto.getShortDescription());
        }
        if (dto.getDescription() != null) {
            ticket.setDescription(dto.getDescription());
        }
        if (dto.getPriority() != null) {
            ticket.setPriority(Ticket.TicketPriority.valueOf(dto.getPriority().toUpperCase()));
        }
        if (dto.getStatus() != null) {
            Ticket.TicketStatus newStatus = Ticket.TicketStatus.valueOf(dto.getStatus().toUpperCase());
            if (newStatus == Ticket.TicketStatus.RESOLVED && ticket.getStatus() != Ticket.TicketStatus.RESOLVED) {
                ticket.setResolvedAt(LocalDateTime.now());
            } else if (newStatus != Ticket.TicketStatus.RESOLVED) {
                ticket.setResolvedAt(null);
            }
            ticket.setStatus(newStatus);
        }

        if (dto.getAssignedToId() != null) {
            Employee assignedTo = employeeRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned employee not found"));
            ticket.setAssignedTo(assignedTo);
        } else if (dto.getAssignedToId() == null) {
            ticket.setAssignedTo(null);
        }

        return toDTO(ticketRepository.save(ticket));
    }

    public TicketDTO resolveTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        ticket.setStatus(Ticket.TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        return toDTO(ticketRepository.save(ticket));
    }

    private TicketDTO toDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .empId(ticket.getEmployee().getId())
                .empName(ticket.getEmployee().getName())
                .description(ticket.getDescription())
                .shortDescription(ticket.getShortDescription())
                .status(ticket.getStatus().name())
                .priority(ticket.getPriority() != null ? ticket.getPriority().name() : null)
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getName() : null)
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }
}
