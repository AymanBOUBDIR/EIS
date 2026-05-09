package ma.enset.eisbackend.controller;

import lombok.RequiredArgsConstructor;
import ma.enset.eisbackend.dto.EmployeeDTO;
import ma.enset.eisbackend.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(employeeService.getByDepartment(deptId));
    }

    @GetMapping("/high-earners")
    public ResponseEntity<List<EmployeeDTO>> getHighEarners(
            @RequestParam(defaultValue = "75000") BigDecimal minSalary) {
        return ResponseEntity.ok(employeeService.getHighEarners(minSalary));
    }

    @GetMapping("/attrition-risk")
    public ResponseEntity<List<EmployeeDTO>> getAttritionRisk(
            @RequestParam(defaultValue = "60") Double threshold) {
        return ResponseEntity.ok(employeeService.getAttritionRisk(threshold));
    }
}