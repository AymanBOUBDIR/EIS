package ma.enset.eisbackend.controller;

import lombok.RequiredArgsConstructor;
import ma.enset.eisbackend.dto.LeaveRequestDTO;
import ma.enset.eisbackend.service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping("/upcoming")
    public ResponseEntity<List<LeaveRequestDTO>> getUpcomingLeaves() {
        return ResponseEntity.ok(leaveRequestService.getUpcomingLeaves());
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<LeaveRequestDTO>> getEmployeeLeaves(@PathVariable Long empId) {
        return ResponseEntity.ok(leaveRequestService.getEmployeeLeaves(empId));
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDTO> createLeaveRequest(@RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.createLeaveRequest(dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LeaveRequestDTO> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(leaveRequestService.updateStatus(id, status));
    }
}
