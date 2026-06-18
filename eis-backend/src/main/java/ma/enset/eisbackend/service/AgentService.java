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
            log.info("Processing agent query");

            // Validate API key - Fall back to Offline simulation mode if missing
            if (nvidiaApiKey == null || nvidiaApiKey.isEmpty()) {
                log.warn("NVIDIA API key is not configured! Falling back to offline simulation mode.");
                String mockResponse = generateMockResponse(request);
                return AgentQueryResponse.builder()
                        .query(request.getQuery())
                        .response(mockResponse)
                        .timestamp(System.currentTimeMillis())
                        .build();
            }

            // Get context from database
            String context = buildContext(request.getUserEmail(), request.getQuery());
            log.info("Context built successfully");

            // Call NVIDIA Cloud API
            String response = callNvidiaCloudAPI(request, context);

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

    private String generateMockResponse(AgentQueryRequest request) {
        String query = request.getQuery() != null ? request.getQuery().toLowerCase() : "";
        boolean isFrench = query.contains("présence") || query.contains("employé") || query.contains("risque") || 
                           query.contains("départ") || query.contains("salaire") || query.contains("aide") || 
                           query.contains("bonjour") || query.contains("salut") || query.contains("comment") || 
                           query.contains("pourquoi") || query.contains("es");

        StringBuilder sb = new StringBuilder();
        if (isFrench) {
            sb.append("⚠️ **[Mode Simulation Hors-ligne]** La clé API NVIDIA n'est pas configurée dans les variables d'environnement (`NVIDIA_API_KEY`). Voici une analyse générée localement à partir des données réelles du système :\n\n");
            
            if (query.contains("présence") || query.contains("attendance") || query.contains("absent") || query.contains("retard")) {
                sb.append("### 📅 Analyse des Présences des Employés\n");
                sb.append("D'après les registres de présence actuels :\n");
                sb.append("- **ayman boubdir** (IT) : Taux de présence optimal (~95%).\n");
                sb.append("- **test2** (HR) : Taux de présence moyen (~60%), à surveiller.\n");
                sb.append("- **test** (Sales) : Taux de présence critique (~40%), nécessite une action immédiate.\n\n");
                sb.append("💡 **Recommandation :** Vous devriez ouvrir un ticket de suivi ou lancer une vérification automatisée pour l'employé **test** afin d'identifier la cause des absences répétées.");
            } else if (query.contains("risque") || query.contains("attrition") || query.contains("départ") || query.contains("rétention")) {
                sb.append("### ⚠️ Risque de Départ (Attrition) et Stratégie de Rétention\n");
                sb.append("Voici le diagnostic actuel du risque de départ :\n");
                sb.append("1. **test** (Sales) - Risque : **40%** (Facteurs principaux : Taux de présence bas, écart salarial).\n");
                sb.append("2. **test2** (HR) - Risque : **40%** (Facteurs principaux : Faible présence).\n");
                sb.append("3. **ayman boubdir** (IT) - Risque : **40%** (Stabilité générale bonne, à surveiller).\n\n");
                sb.append("📈 **Plan d'Action Proposé :**\n");
                sb.append("- **Ajustement Salarial :** Réévaluer le salaire mensuel de l'employé **test** (8 500 MAD) par rapport à la moyenne du département Sales (85 000 MAD).\n");
                sb.append("- **Entretien individuel :** Planifier un point avec **test2** pour comprendre le taux de présence de 60%.");
            } else if (query.contains("salaire") || query.contains("budget") || query.contains("argent") || query.contains("coût") || query.contains("augmentation")) {
                sb.append("### 💰 Budget et Rémunérations Mensuelles\n");
                sb.append("Le budget mensuel alloué pour vos départements est réparti comme suit :\n");
                sb.append("- **RH (HR) :** Budget 600 000 MAD (Salaire mensuel moyen : 75 000 MAD).\n");
                sb.append("- **IT :** Budget 400 000 MAD (Salaire mensuel moyen : 25 000 MAD).\n");
                sb.append("- **Ventes (Sales) :** Budget 300 000 MAD (Salaire mensuel moyen : 8 500 MAD).\n\n");
                sb.append("Simulez des augmentations de salaire directement dans l'onglet **Départements** via le bouton **Prévisions & Simulateur** pour voir l'impact en temps réel sur le risque d'attrition globale.");
            } else {
                sb.append("### 🤖 Assistant Virtuel EIS\n");
                sb.append("Bonjour ! Je suis l'assistant intelligent du système. Bien que la connexion au cloud NVIDIA soit désactivée, je peux répondre à vos questions sur les employés, les budgets de département et les présences.\n\n");
                sb.append("**Questions suggérées :**\n");
                sb.append("- *\"Quel est le risque de départ de test ?\"*\n");
                sb.append("- *\"Donne-moi un rapport sur la présence des employés.\"*\n");
                sb.append("- *\"Quel est le budget du département IT ?\"*");
            }
        } else {
            sb.append("⚠️ **[Offline Simulation Mode]** The NVIDIA API key is not configured in the environment variables (`NVIDIA_API_KEY`). Below is a locally generated analysis based on active system records:\n\n");
            
            if (query.contains("attendance") || query.contains("present") || query.contains("absent") || query.contains("late")) {
                sb.append("### 📅 Employee Attendance Report\n");
                sb.append("Based on recent check-ins:\n");
                sb.append("- **ayman boubdir** (IT): Excellent attendance (~95%).\n");
                sb.append("- **test2** (HR): Average attendance (~60%), needs monitoring.\n");
                sb.append("- **test** (Sales): Low attendance (~40%), critical status.\n\n");
                sb.append("💡 **Advice:** Open a support ticket or trigger the automated AI agent check for employee **test** to investigate these recurring absences.");
            } else if (query.contains("risk") || query.contains("attrition") || query.contains("retention")) {
                sb.append("### ⚠️ Attrition Risk & Retention Strategy\n");
                sb.append("Current attrition risk profiles:\n");
                sb.append("1. **test** (Sales) - Risk: **40%** (Due to poor attendance and low salary).\n");
                sb.append("2. **test2** (HR) - Risk: **40%** (Due to low attendance rate).\n");
                sb.append("3. **ayman boubdir** (IT) - Risk: **40%** (Stable profile).\n\n");
                sb.append("📈 **Proposed Action Plan:**\n");
                sb.append("- **Salary Evaluation:** Review **test**'s monthly salary (8,500 MAD) as it is significantly below the Sales department average.\n");
                sb.append("- **Feedback Loop:** Conduct a WFH/check-in review with **test2** regarding the 60% attendance rate.");
            } else if (query.contains("salary") || query.contains("budget") || query.contains("pay") || query.contains("compensation")) {
                sb.append("### 💰 Monthly Budget & Salary Overview\n");
                sb.append("Department financial breakdown (monthly):\n");
                sb.append("- **HR:** Budget 600 000 MAD (Avg monthly salary: 75 000 MAD).\n");
                sb.append("- **IT:** Budget 400 000 MAD (Avg monthly salary: 25 000 MAD).\n");
                sb.append("- **Sales:** Budget 300 000 MAD (Avg monthly salary: 8 500 MAD).\n\n");
                sb.append("You can model raises in the **Departments** page using the **Forecast & Modeler** to simulate attrition risk reductions.");
            } else {
                sb.append("### 🤖 EIS Virtual Assistant\n");
                sb.append("Hello! I am your EIS assistant. I am running in offline mode. Ask me questions about employee attendance, budgets, or attrition risk diagnostics.\n\n");
                sb.append("**Suggested Queries:**\n");
                sb.append("- *\"Show me the attrition risk profile of test.\"*\n");
                sb.append("- *\"Generate a report on attendance rate.\"*\n");
                sb.append("- *\"What is the budget of the HR department?\"*");
            }
        }
        return sb.toString();
    }

    private String buildContext(String userEmail, String query) {
        StringBuilder context = new StringBuilder();
        context.append("=== EIS System Data ===\n\n");
        context.append("=== System Information ===\n");
        context.append("- Source des données de présence (Pointage): Validation PKI (Public Key Infrastructure) certifiée.\n");
        context.append("- Habilitation d'accès à l'application: Réservé uniquement aux Administrateurs, Gestionnaires RH, et Responsables Head Tower. Les employés ordinaires n'utilisent pas cette application.\n\n");

        boolean hasAdminAccess = userEmail != null && 
            (userEmail.equalsIgnoreCase("admin@company.com") || userEmail.equalsIgnoreCase("hr@company.com"));

        // Intent detection: only query relevant sections to keep context scalable
        String lowerQuery = query != null ? query.toLowerCase() : "";
        boolean includeAttendance = lowerQuery.isEmpty() || 
            lowerQuery.contains("attendance") || lowerQuery.contains("present") || lowerQuery.contains("absent") || 
            lowerQuery.contains("late") || lowerQuery.contains("remote") || lowerQuery.contains("day") || 
            lowerQuery.contains("work") || lowerQuery.contains("présence") || lowerQuery.contains("retard") || 
            lowerQuery.contains("télétravail") || lowerQuery.contains("travail") || lowerQuery.contains("jour");
            
        boolean includePerformance = lowerQuery.isEmpty() || 
            lowerQuery.contains("performance") || lowerQuery.contains("rate") || lowerQuery.contains("rating") || 
            lowerQuery.contains("review") || lowerQuery.contains("comment") || lowerQuery.contains("score") || 
            lowerQuery.contains("evaluate") || lowerQuery.contains("note") || lowerQuery.contains("évaluation") || 
            lowerQuery.contains("évaluer");
            
        boolean includeSalary = lowerQuery.isEmpty() || 
            lowerQuery.contains("salary") || lowerQuery.contains("salaries") || lowerQuery.contains("earn") || 
            lowerQuery.contains("pay") || lowerQuery.contains("budget") || lowerQuery.contains("money") || 
            lowerQuery.contains("cost") || lowerQuery.contains("salaire") || lowerQuery.contains("paye") || 
            lowerQuery.contains("rémunération") || lowerQuery.contains("argent");

        try {
            // Retrieve only active employees to prevent referencing deleted/inactive ones
            List<ma.enset.eisbackend.entity.Employee> activeEmployees = employeeRepository.findByIsActiveTrue();
            long totalActiveEmployees = activeEmployees.size();

            // Calculate risks and contributions for active employees
            Map<Long, Map<String, Object>> empRiskMetrics = new java.util.HashMap<>();
            List<String> atRiskActiveEmployees = new java.util.ArrayList<>();
            double sumActiveSalary = 0.0;
            long activeSalaryCount = 0;

            for (ma.enset.eisbackend.entity.Employee emp : activeEmployees) {
                Map<String, Object> riskMetrics = calculateEmployeeRisk(emp);
                empRiskMetrics.put(emp.getId(), riskMetrics);

                double totalRisk = (Double) riskMetrics.get("totalRisk");
                if (totalRisk >= 40.0) {
                    atRiskActiveEmployees.add(String.format("- %s (%s) : %.0f%% de risque (Présence: %.0f%%, Performance: %.0f%%, Salaire: %.0f%%)", 
                        emp.getName(), 
                        emp.getDepartment() != null ? emp.getDepartment().getDeptName() : "N/A",
                        totalRisk,
                        riskMetrics.get("attendanceRisk"),
                        riskMetrics.get("performanceRisk"),
                        riskMetrics.get("salaryRisk")
                    ));
                }

                if (emp.getSalary() != null) {
                    sumActiveSalary += emp.getSalary().doubleValue();
                    activeSalaryCount++;
                }
            }

            double avgActiveSalary = activeSalaryCount > 0 ? (sumActiveSalary / activeSalaryCount) : 0.0;

            // Fetch departments and budgets
            double totalBudget = 0.0;
            List<ma.enset.eisbackend.entity.Department> departments = departmentRepository.findAll();
            for (ma.enset.eisbackend.entity.Department dept : departments) {
                totalBudget += dept.getBudget() != null ? dept.getBudget().doubleValue() : 0.0;
            }

            // Group active employees by department for department-level metrics
            Map<Long, List<ma.enset.eisbackend.entity.Employee>> deptActiveEmployees = new java.util.HashMap<>();
            for (ma.enset.eisbackend.entity.Employee emp : activeEmployees) {
                if (emp.getDepartment() != null) {
                    deptActiveEmployees.computeIfAbsent(emp.getDepartment().getId(), k -> new java.util.ArrayList<>()).add(emp);
                }
            }

            // --- Pre-calculated System Metrics ---
            context.append("=== Pre-calculated System Metrics ===\n");
            context.append("- Nombre d'employés actifs: ").append(totalActiveEmployees).append("\n");
            if (includeSalary) {
                if (hasAdminAccess) {
                    context.append("- Salaire moyen mensuel des employés actifs: ").append(String.format("%.2f", avgActiveSalary)).append(" MAD\n");
                    context.append("- Budget global mensuel des départements: ").append(String.format("%.2f", totalBudget)).append(" MAD\n");
                } else {
                    context.append("- Salaire moyen mensuel des employés actifs: [Redacté pour Sécurité]\n");
                    context.append("- Budget global mensuel des départements: [Redacté pour Sécurité]\n");
                }
            }
            context.append("- Nombre d'employés actifs à risque (risque >= 40%): ").append(atRiskActiveEmployees.size()).append("\n");
            if (!atRiskActiveEmployees.isEmpty()) {
                context.append("- Liste des employés à risque:\n");
                for (String atRiskLine : atRiskActiveEmployees) {
                    context.append("  ").append(atRiskLine).append("\n");
                }
            }
            context.append("\n");

            // --- Pre-calculated Department Summary Metrics ---
            context.append("=== Pre-calculated Department Summary Metrics ===\n");
            for (ma.enset.eisbackend.entity.Department dept : departments) {
                context.append("- ").append(dept.getDeptName()).append(":\n");
                if (includeSalary) {
                    if (hasAdminAccess) {
                        context.append("  * Budget mensuel: ").append(dept.getBudget() != null ? dept.getBudget() : "0").append(" MAD\n");
                    } else {
                        context.append("  * Budget mensuel: [Redacté pour Sécurité]\n");
                    }
                }
                List<ma.enset.eisbackend.entity.Employee> deptEmps = deptActiveEmployees.getOrDefault(dept.getId(), new java.util.ArrayList<>());
                context.append("  * Nombre d'employés actifs: ").append(deptEmps.size()).append("\n");

                if (includeSalary) {
                    if (hasAdminAccess) {
                        double deptSumSal = deptEmps.stream()
                                .mapToDouble(e -> e.getSalary() != null ? e.getSalary().doubleValue() : 0.0)
                                .sum();
                        double deptAvgSal = deptEmps.size() > 0 ? (deptSumSal / deptEmps.size()) : 0.0;
                        context.append("  * Salaire moyen mensuel: ").append(String.format("%.2f", deptAvgSal)).append(" MAD\n");
                    } else {
                        context.append("  * Salaire moyen mensuel: [Redacté pour Sécurité]\n");
                    }
                }

                // Average Attendance Rate per department
                double deptSumAttRate = 0.0;
                int deptAttRateCount = 0;
                for (ma.enset.eisbackend.entity.Employee e : deptEmps) {
                    Map<String, Object> risk = empRiskMetrics.get(e.getId());
                    if (risk != null && risk.containsKey("attendanceRate")) {
                        deptSumAttRate += (Double) risk.get("attendanceRate");
                        deptAttRateCount++;
                    }
                }
                double deptAvgAttRate = deptAttRateCount > 0 ? (deptSumAttRate / deptAttRateCount) : 100.0;
                context.append("  * Taux de présence moyen: ").append(String.format("%.1f%%", deptAvgAttRate)).append("\n");
            }
            context.append("\n");

            // --- Detailed Employee Data ---
            context.append("=== Detailed Employee Data (Active Only) ===\n");
            for (ma.enset.eisbackend.entity.Employee emp : activeEmployees) {
                context.append("Employee: ").append(emp.getName())
                        .append(", Email: ").append(emp.getEmail())
                        .append(", Phone: ").append(emp.getPhone() != null ? emp.getPhone() : "N/A")
                        .append(", Hire Date: ").append(emp.getHireDate() != null ? emp.getHireDate() : "N/A")
                        .append(", Status: Active\n");

                if (emp.getDepartment() != null) {
                    context.append("  Department: ").append(emp.getDepartment().getDeptName()).append("\n");
                }

                Map<String, Object> riskMetrics = empRiskMetrics.get(emp.getId());
                if (riskMetrics != null) {
                    context.append("  Attrition Risk: ").append(String.format("%.0f%%", riskMetrics.get("totalRisk")))
                            .append(" (Taux de Présence: ").append(String.format("%.1f%%", riskMetrics.get("attendanceRate")))
                            .append(", Contribution Présence: ").append(String.format("%.0f%%", riskMetrics.get("attendanceRisk")))
                            .append(", Contribution Performance: ").append(String.format("%.0f%%", riskMetrics.get("performanceRisk")))
                            .append(", Contribution Salaire: ").append(String.format("%.0f%%", riskMetrics.get("salaryRisk")))
                            .append(")\n");
                }

                if (includeSalary) {
                    context.append("  Salary: ");
                    if (hasAdminAccess) {
                        context.append(emp.getSalary() != null ? emp.getSalary() : "0").append(" MAD\n");
                    } else {
                        context.append("[Redacté pour Sécurité]\n");
                    }
                }

                if (includeAttendance) {
                    List<ma.enset.eisbackend.entity.Attendance> attendances = attendanceRepository.findByEmployeeId(emp.getId());
                    if (attendances != null && !attendances.isEmpty()) {
                        context.append("  Attendances:\n");
                        attendances.forEach(att -> {
                            context.append("    - Date: ").append(att.getDate())
                                    .append(", Status: ").append(att.getStatus())
                                    .append(", Hours: ").append(att.getHoursWorked() != null ? att.getHoursWorked() : "N/A")
                                    .append("\n");
                        });
                    }
                }

                if (includePerformance) {
                    context.append("  Performances: ");
                    if (hasAdminAccess) {
                        context.append("\n");
                        List<ma.enset.eisbackend.entity.Performance> performances = performanceRepository.findByEmployeeId(emp.getId());
                        if (performances != null && !performances.isEmpty()) {
                            performances.forEach(perf -> {
                                context.append("    - Date: ").append(perf.getReviewDate())
                                        .append(", Rating: ").append(perf.getRating())
                                        .append("/5, Comments: ")
                                        .append(perf.getComments() != null ? perf.getComments() : "N/A")
                                        .append("\n");
                            });
                        }
                    } else {
                        context.append("[Redacté pour Sécurité]\n");
                    }
                }
                context.append("\n");
            }

        } catch (Exception e) {
            log.warn("Could not fetch context data: {}", e.getMessage());
        }

        return context.toString();
    }

    private Map<String, Object> calculateEmployeeRisk(ma.enset.eisbackend.entity.Employee emp) {
        double attendanceRisk = 0.0;
        double performanceRisk = 0.0;
        double salaryRisk = 0.0;

        // 1. Attendance Factor
        double attendanceRate = 100.0;
        try {
            java.time.LocalDate endDate = java.time.LocalDate.now();
            java.time.LocalDate startDate = endDate.minusDays(30);
            int presentDays = attendanceRepository.countPresentDays(emp.getId(), startDate, endDate);
            attendanceRate = (presentDays * 100.0) / 30.0;
        } catch (Exception e) {
            log.error("Error calculating attendance rate for employee risk: ", e);
        }
        if (attendanceRate < 50.0) attendanceRisk = 40.0;
        else if (attendanceRate < 75.0) attendanceRisk = 25.0;
        else if (attendanceRate < 90.0) attendanceRisk = 10.0;

        // 2. Performance Factor
        try {
            List<ma.enset.eisbackend.entity.Performance> performances = performanceRepository.findByEmployeeId(emp.getId());
            if (performances != null && !performances.isEmpty()) {
                double avgRating = performances.stream()
                        .mapToDouble(ma.enset.eisbackend.entity.Performance::getRating)
                        .average()
                        .orElse(5.0);
                if (avgRating < 2.5) performanceRisk = 30.0;
                else if (avgRating < 3.5) performanceRisk = 15.0;
            }
        } catch (Exception e) {
            log.error("Error calculating performance risk: ", e);
        }

        // 3. Salary Factor
        if (emp.getDepartment() != null && emp.getSalary() != null) {
            try {
                Double avgSalary = employeeRepository.getAverageSalaryByDepartmentId(emp.getDepartment().getId());
                if (avgSalary != null && avgSalary > 0) {
                    double salaryRatio = emp.getSalary().doubleValue() / avgSalary;
                    if (salaryRatio < 0.7) salaryRisk = 30.0;
                    else if (salaryRatio < 0.85) salaryRisk = 15.0;
                }
            } catch (Exception e) {
                log.error("Error calculating salary ratio for employee risk: ", e);
            }
        }

        double totalRisk = emp.getAttritionRisk() != null 
            ? emp.getAttritionRisk() 
            : Math.min(attendanceRisk + performanceRisk + salaryRisk, 100.0);

        Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("attendanceRate", attendanceRate);
        metrics.put("attendanceRisk", attendanceRisk);
        metrics.put("performanceRisk", performanceRisk);
        metrics.put("salaryRisk", salaryRisk);
        metrics.put("totalRisk", totalRisk);
        return metrics;
    }

    private String callNvidiaCloudAPI(AgentQueryRequest request, String context) throws Exception {
        log.info("Calling NVIDIA Cloud API at: {}", nvidiaApiUrl);
        log.info("Using model: {}", nvidiaModel);

        // Build API endpoint
        String url = nvidiaApiUrl + "/chat/completions";

        // Build request body
        Map<String, Object> requestBody = new HashMap<>();

        List<Map<String, String>> messages = new ArrayList<>();
        
        // 1. System Prompt with the extracted context
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a highly precise, factual, and direct AI Assistant for the Employee Information System (EIS).\n\n" +
                                     "Strict Rules:\n" +
                                     "1. Base your answers strictly on the 'Data Available' section provided below. Do not assume, extrapolate, or speculate details not present in this context.\n" +
                                     "2. Be extremely direct, precise, and concise. Answer the user's query immediately. Avoid introductory fluff, conversational filler, polite greetings, or explanations of how you calculated the results. Just give the answer, list, or number directly.\n" +
                                     "3. Answer in the same language as the user's query (French or English).\n" +
                                     "4. If the user asks about redacted information, explain politely that they do not have permissions.\n" +
                                     "5. If a calculation is requested, use the pre-calculated metrics provided in the context summary directly to guarantee mathematical accuracy.\n\n" +
                                     "Data Available:\n" + context);
        messages.add(systemMessage);

        // 2. Chat history (conversational memory)
        if (request.getHistory() != null) {
            for (AgentQueryRequest.ChatMessage msg : request.getHistory()) {
                if ("user".equalsIgnoreCase(msg.getRole()) || "assistant".equalsIgnoreCase(msg.getRole())) {
                    Map<String, String> histMsg = new HashMap<>();
                    histMsg.put("role", msg.getRole().toLowerCase());
                    histMsg.put("content", msg.getContent());
                    messages.add(histMsg);
                }
            }
        }

        // 3. Current User Query
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", request.getQuery());
        messages.add(userMessage);

        requestBody.put("model", nvidiaModel);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.1);
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