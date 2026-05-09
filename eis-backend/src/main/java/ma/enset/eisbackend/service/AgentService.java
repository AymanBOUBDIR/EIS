package ma.enset.eisbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.dto.AgentQueryRequest;
import ma.enset.eisbackend.dto.AgentQueryResponse;
import ma.enset.eisbackend.repository.EmployeeRepository;
import ma.enset.eisbackend.repository.DepartmentRepository;
import ma.enset.eisbackend.repository.AttendanceRepository;
import ma.enset.eisbackend.repository.PerformanceRepository;
import ma.enset.eisbackend.entity.Attendance;
import ma.enset.eisbackend.entity.Performance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final PerformanceRepository performanceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${nvidia.api.key}")
    private String nvidiaApiKey;

    @Value("${nvidia.api.url:https://integrate.api.nvidia.com/v1}")
    private String nvidiaApiUrl;

    @Value("${nvidia.model:meta/llama-3.1-405b-instruct}")
    private String nvidiaModel;

    public AgentQueryResponse processQuery(AgentQueryRequest request) {
        try {
            log.info("Processing agent query using NVIDIA Cloud API");

            // Validate API key
            if (nvidiaApiKey == null || nvidiaApiKey.isEmpty()) {
                log.error("NVIDIA API key is not configured!");
                return buildErrorResponse(request.getQuery(), "NVIDIA API key is not configured");
            }

            // Get context from database
            String context = buildContext();
            log.info("Context built successfully");

            // Build prompt
            String prompt = buildPrompt(request.getQuery(), context);

            // Call NVIDIA Cloud API
            String response = callNvidiaCloudAPI(prompt);

            log.info("Response received successfully from NVIDIA");

            return AgentQueryResponse.builder()
                    .query(request.getQuery())
                    .response(response)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Error processing agent query: {}", e.getMessage(), e);
            return buildErrorResponse(request.getQuery(), "Error: " + e.getMessage());
        }
    }

    private String buildContext() {
        StringBuilder context = new StringBuilder();
        context.append("=== EIS System Data ===\n\n");

        try {
            long totalEmployees = employeeRepository.count();
            context.append("Total Employees: ").append(totalEmployees).append("\n\n");

            context.append("--- Departments ---\n");
            departmentRepository.findAll().forEach(dept -> {
                context.append("- ").append(dept.getDeptName())
                        .append(" (Budget: $").append(dept.getBudget()).append(")\n");
            });
            context.append("\n");

            context.append("--- Employees ---\n");
            employeeRepository.findAll().forEach(emp -> {
                context.append("Employee: ").append(emp.getName())
                        .append(", Email: ").append(emp.getEmail())
                        .append(", Phone: ").append(emp.getPhone() != null ? emp.getPhone() : "N/A")
                        .append(", Hire Date: ").append(emp.getHireDate() != null ? emp.getHireDate() : "N/A")
                        .append(", Status: ").append(Boolean.TRUE.equals(emp.getIsActive()) ? "Active" : "Inactive")
                        .append("\n");

                if (emp.getDepartment() != null) {
                    context.append("  Department: ").append(emp.getDepartment().getDeptName()).append("\n");
                }

                if (emp.getSalary() != null) {
                    context.append("  Salary: $").append(emp.getSalary()).append("\n");
                }

                List<Attendance> attendances = attendanceRepository.findByEmployeeId(emp.getId());
                if (attendances != null && !attendances.isEmpty()) {
                    context.append("  Attendances:\n");
                    attendances.forEach(att -> {
                        context.append("    - Date: ").append(att.getDate())
                                .append(", Status: ").append(att.getStatus())
                                .append(", Hours: ").append(att.getHoursWorked() != null ? att.getHoursWorked() : "N/A")
                                .append("\n");
                    });
                }

                List<Performance> performances = performanceRepository.findByEmployeeId(emp.getId());
                if (performances != null && !performances.isEmpty()) {
                    context.append("  Performances:\n");
                    performances.forEach(perf -> {
                        context.append("    - Date: ").append(perf.getReviewDate())
                                .append(", Rating: ").append(perf.getRating())
                                .append("/5, Comments: ")
                                .append(perf.getComments() != null ? perf.getComments() : "N/A")
                                .append("\n");
                    });
                }
                context.append("\n");
            });

        } catch (Exception e) {
            log.warn("Could not fetch context data: {}", e.getMessage());
        }

        return context.toString();
    }

    private String buildPrompt(String userQuery, String context) {
        return "You are an AI Assistant for an Employee Information System.\n\n" +
                "Data Available:\n" + context + "\n\n" +
                "User Question: " + userQuery + "\n\n" +
                "Answer based on the provided data.";
    }

    private String callNvidiaCloudAPI(String prompt) throws Exception {
        log.info("Calling NVIDIA Cloud API at: {}", nvidiaApiUrl);
        log.info("Using model: {}", nvidiaModel);

        // Build API endpoint
        String url = nvidiaApiUrl + "/chat/completions";

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("model", nvidiaModel);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("top_p", 0.95);

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + nvidiaApiKey);
        headers.set("Accept", "application/json");

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Sending request to NVIDIA Cloud API...");
            String responseStr = restTemplate.postForObject(url, httpEntity, String.class);

            if (responseStr == null) {
                log.error("No response from NVIDIA API");
                return "No response from NVIDIA API";
            }

            log.debug("Response received, parsing...");
            JsonNode root = objectMapper.readTree(responseStr);

            // Check for error
            if (root.has("error")) {
                String errorMsg = root.get("error").toString();
                log.error("NVIDIA API Error: {}", errorMsg);
                return "NVIDIA Error: " + errorMsg;
            }

            // Extract response
            if (root.has("choices") && root.get("choices").isArray()) {
                JsonNode choices = root.get("choices");
                if (choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message.has("content")) {
                        String content = message.get("content").asText();
                        log.info("Response parsed successfully");
                        return content.trim();
                    }
                }
            }

            log.error("Unexpected response format: {}", responseStr);
            return "Could not parse NVIDIA response";

        } catch (Exception e) {
            log.error("Exception calling NVIDIA API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private AgentQueryResponse buildErrorResponse(String query, String error) {
        return AgentQueryResponse.builder()
                .query(query)
                .response(error)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}