package ma.enset.eisbackend.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.TicketDTO;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.service.AttendanceService;
import ma.enset.eisbackend.service.TicketService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceAgentScheduler {

    private final EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;
    private final TicketService ticketService;

    // Manual trigger method (replaces @Scheduled)
    public List<TicketDTO> runEvaluation() {
        log.info("🤖 Agent: Starting manual attendance evaluation...");
        
        List<Employee> activeEmployees = employeeRepository.findByIsActiveTrue();
        List<TicketDTO> newTickets = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            // Calculate attendance rate for the last 30 days
            double rate = attendanceService.getAttendanceRate(emp.getId(), 30);
            
            if (rate < 50.0) {
                TicketDTO ticket = ticketService.createFollowUpTicket(emp, rate);
                if (ticket != null) {
                    newTickets.add(ticket);
                }
            }
        }
        
        log.info("🤖 Agent: Evaluation complete. Created {} new tickets.", newTickets.size());
        return newTickets;
    }
}
