package ma.enset.eisbackend.controller;

import lombok.RequiredArgsConstructor;
import ma.enset.eisbackend.dto.TicketDTO;
import ma.enset.eisbackend.entity.Ticket;
import ma.enset.eisbackend.service.TicketService;
import ma.enset.eisbackend.scheduler.AttendanceAgentScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final AttendanceAgentScheduler agentScheduler;

    @GetMapping
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketDTO>> getTicketsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(Ticket.TicketStatus.valueOf(status.toUpperCase())));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<TicketDTO> resolveTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.resolveTicket(id));
    }

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@RequestBody TicketDTO dto) {
        return ResponseEntity.ok(ticketService.createTicket(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id, @RequestBody TicketDTO dto) {
        return ResponseEntity.ok(ticketService.updateTicket(id, dto));
    }

    @PostMapping("/run-agent")
    public ResponseEntity<List<TicketDTO>> runAgentCheck(@RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(agentScheduler.runEvaluation(employeeId));
    }
}
