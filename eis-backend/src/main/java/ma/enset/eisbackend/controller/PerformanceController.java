package ma.enset.eisbackend.controller;

import lombok.RequiredArgsConstructor;
import ma.enset.eisbackend.dto.PerformanceDTO;
import ma.enset.eisbackend.service.PerformanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping("/{empId}")
    public ResponseEntity<List<PerformanceDTO>> getEmployeePerformance(@PathVariable Long empId) {
        return ResponseEntity.ok(performanceService.getEmployeePerformance(empId));
    }

    @PostMapping
    public ResponseEntity<PerformanceDTO> createPerformance(@RequestBody PerformanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(performanceService.createPerformance(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerformanceDTO> updatePerformance(@PathVariable Long id, @RequestBody PerformanceDTO dto) {
        return ResponseEntity.ok(performanceService.updatePerformance(id, dto));
    }
}
