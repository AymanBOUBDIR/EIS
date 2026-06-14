package ma.enset.eisbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.entity.Attendance;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.repository.AttendanceRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking database for seed data...");

        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            log.warn("No employees found. Please create employees first.");
            return;
        }

        if (attendanceRepository.count() > 0) {
            log.info("Attendance data already exists. Skipping seeding.");
            return;
        }

        log.info("Seeding realistic 30-day attendance data for {} employees...", employees.size());
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // Make the first employee have terrible attendance (< 50%) to trigger the agent
        boolean firstEmployeeDone = false;

        for (Employee emp : employees) {
            boolean isProblematic = !firstEmployeeDone;
            
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                // Skip weekends
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    continue;
                }

                Attendance.AttendanceStatus status;
                double hours;
                String remarks = "";

                if (isProblematic) {
                    // Problematic employee: Absent 60% of the time
                    if (Math.random() < 0.6) {
                        status = Attendance.AttendanceStatus.ABSENT;
                        hours = 0.0;
                        remarks = "Unexplained absence";
                    } else {
                        status = Attendance.AttendanceStatus.PRESENT;
                        hours = 8.0;
                    }
                } else {
                    // Normal employee: mostly present
                    double rand = Math.random();
                    if (rand < 0.85) {
                        status = Attendance.AttendanceStatus.PRESENT;
                        hours = 8.0;
                    } else if (rand < 0.95) {
                        status = Attendance.AttendanceStatus.LATE;
                        hours = 7.5;
                        remarks = "Traffic delay";
                    } else {
                        status = Attendance.AttendanceStatus.REMOTE;
                        hours = 8.0;
                        remarks = "Approved WFH";
                    }
                }

                Attendance attendance = Attendance.builder()
                        .employee(emp)
                        .date(date)
                        .status(status)
                        .hoursWorked(hours)
                        .remarks(remarks)
                        .build();

                attendanceRepository.save(attendance);
            }
            firstEmployeeDone = true;
        }

        log.info("Successfully seeded attendance data!");
    }
}
