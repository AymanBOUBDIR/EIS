package ma.enset.eisbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.entity.Attendance;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.entity.LeaveRequest;
import ma.enset.eisbackend.repository.AttendanceRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.repository.LeaveRequestRepository;
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
    private final LeaveRequestRepository leaveRequestRepository;

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
            seedLeaveRequests(employees);
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
        seedLeaveRequests(employees);
    }

    private void seedLeaveRequests(List<Employee> employees) {
        if (leaveRequestRepository.count() > 0) {
            log.info("Leave request data already exists. Skipping seeding.");
            return;
        }

        if (employees.size() < 3) {
            log.warn("Not enough employees to seed leave requests.");
            return;
        }

        log.info("Seeding leave request data...");
        LocalDate today = LocalDate.now();

        // Employee 1: Vacances (starts tomorrow, 7 days)
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(employees.get(0))
                .startDate(today.plusDays(1))
                .endDate(today.plusDays(8))
                .type(LeaveRequest.LeaveType.VACANCES)
                .status(LeaveRequest.LeaveStatus.APPROVED)
                .reason("Vacances annuelles programmées")
                .build());

        // Employee 2: Congé maladie (current, 2 days)
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(employees.get(1))
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(1))
                .type(LeaveRequest.LeaveType.CONGE_MALADIE)
                .status(LeaveRequest.LeaveStatus.APPROVED)
                .reason("Certificat médical fourni")
                .build());

        // Employee 3: Congé personnel (future, 2 days)
        if (employees.size() > 2) {
            leaveRequestRepository.save(LeaveRequest.builder()
                    .employee(employees.get(2))
                    .startDate(today.plusDays(4))
                    .endDate(today.plusDays(5))
                    .type(LeaveRequest.LeaveType.CONGE_PERSONNEL)
                    .status(LeaveRequest.LeaveStatus.APPROVED)
                    .reason("Raison personnelle")
                    .build());
        }

        // Employee 4: Formation (future, 3 days)
        if (employees.size() > 3) {
            leaveRequestRepository.save(LeaveRequest.builder()
                    .employee(employees.get(3))
                    .startDate(today.plusDays(7))
                    .endDate(today.plusDays(9))
                    .type(LeaveRequest.LeaveType.FORMATION)
                    .status(LeaveRequest.LeaveStatus.APPROVED)
                    .reason("Formation certifiante AWS")
                    .build());
        }

        log.info("Successfully seeded leave request data!");
    }
}
