
package ma.enset.eisbackend.controller;
import lombok.RequiredArgsConstructor;
import ma.enset.eisbackend.dto.AttendanceDTO;
import ma.enset.eisbackend.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/{empId}")
    public ResponseEntity<List<AttendanceDTO>> getEmployeeAttendance(@PathVariable Long empId) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(empId));
    }

    @GetMapping("/{empId}/range")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByRange(
            @PathVariable Long empId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDateRange(empId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<AttendanceDTO> recordAttendance(@RequestBody AttendanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.recordAttendance(dto));
    }

    @GetMapping("/{empId}/rate")
    public ResponseEntity<Double> getAttendanceRate(
            @PathVariable Long empId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(attendanceService.getAttendanceRate(empId, days));
    }
}
