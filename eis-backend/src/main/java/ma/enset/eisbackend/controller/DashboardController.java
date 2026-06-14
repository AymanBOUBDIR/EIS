package ma.enset.eisbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.entity.Attendance.AttendanceStatus;
import ma.enset.eisbackend.repository.AttendanceRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.repository.PerformanceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class DashboardController {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PerformanceRepository performanceRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Fetching dashboard stats");
        Map<String, Object> stats = new HashMap<>();

        long totalEmployees = employeeRepository.count();
        stats.put("totalEmployees", totalEmployees);

        // Average performance from real data
        Double avgPerformance = performanceRepository.getOverallAverageRating();
        stats.put("avgPerformance", avgPerformance != null ? Math.round(avgPerformance * 10.0) / 10.0 : 0.0);

        // Attendance rate: distinct employees who were PRESENT or REMOTE in last 30 days / total employees
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        if (totalEmployees > 0) {
            long presentCount = attendanceRepository.countDistinctPresentEmployees(thirtyDaysAgo, today);
            double rate = (presentCount * 100.0) / totalEmployees;
            stats.put("attendanceRate", Math.round(rate * 10.0) / 10.0);
        } else {
            stats.put("attendanceRate", 0.0);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/attendance-today")
    public ResponseEntity<List<Map<String, Object>>> getAttendanceToday() {
        log.info("Fetching today's attendance summary");
        LocalDate today = LocalDate.now();
        long totalEmployees = employeeRepository.count();

        long present = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long remote = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.REMOTE);
        long late = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE);
        long totalRecorded = attendanceRepository.countByDate(today);
        long absent = Math.max(0, totalEmployees - totalRecorded);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(Map.of("status", "Présent", "count", present));
        result.add(Map.of("status", "Télétravail", "count", remote));
        result.add(Map.of("status", "En retard", "count", late));
        result.add(Map.of("status", "Absent", "count", absent));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/performance-trend")
    public ResponseEntity<List<Map<String, Object>>> getPerformanceTrend() {
        log.info("Fetching performance trend");
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate now = LocalDate.now();
        String[] monthNames = {"", "Janv.", "Févr.", "Mars", "Avril", "Mai", "Juin", "Juil.", "Août", "Sept.", "Oct.", "Nov.", "Déc."};

        // Get last 6 months of performance data
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int month = date.getMonthValue();
            int year = date.getYear();
            Double avg = performanceRepository.getAverageRatingByMonth(month, year);

            Map<String, Object> entry = new HashMap<>();
            entry.put("month", monthNames[month]);
            entry.put("rating", avg != null ? Math.round(avg * 10.0) / 10.0 : null);
            trend.add(entry);
        }

        return ResponseEntity.ok(trend);
    }
}
