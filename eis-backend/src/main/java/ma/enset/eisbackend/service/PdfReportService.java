package ma.enset.eisbackend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.eisbackend.entity.Department;
import ma.enset.eisbackend.entity.Employee;
import ma.enset.eisbackend.repository.DepartmentRepository;
import ma.enset.eisbackend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public byte[] generateHrReport() {
        log.info("Generating dynamic HR report in PDF format...");
        
        List<Employee> activeEmployees = employeeRepository.findByIsActiveTrue();
        List<Department> departments = departmentRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.saveState();
                    cb.beginText();
                    try {
                        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 8);
                    } catch (IOException | DocumentException e) {
                        log.error("Error setting footer font: ", e);
                    }
                    cb.setColorFill(new java.awt.Color(148, 163, 184));
                    cb.showTextAligned(PdfContentByte.ALIGN_CENTER, 
                            "Portail EIS - Rapport de Synthèse RH Généré le " + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + 
                            " | Page " + writer.getPageNumber(), 
                            (document.right() - document.left()) / 2 + document.leftMargin(), 
                            20, 0);
                    cb.endText();
                    cb.restoreState();
                }
            });

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new java.awt.Color(0, 91, 127));
            Paragraph title = new Paragraph("Rapport de Synthese de l'Information Employe (EIS)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Font metaLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new java.awt.Color(71, 85, 105));
            
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);
            
            PdfPCell cell1 = new PdfPCell(new Paragraph("Type de document: Rapport RH Mensuel", metaLabelFont));
            cell1.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(cell1);
            
            PdfPCell cell2 = new PdfPCell(new Paragraph("Genere par: Administration EIS", metaLabelFont));
            cell2.setBorder(Rectangle.NO_BORDER);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaTable.addCell(cell2);
            
            document.add(metaTable);

            Paragraph line = new Paragraph(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100, new java.awt.Color(226, 232, 240), Element.ALIGN_CENTER, -5)));
            line.setSpacingAfter(15);
            document.add(line);

            Font sectionHeading = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new java.awt.Color(15, 23, 42));
            Paragraph kpiHeading = new Paragraph("Indicateurs Cles de Performance (KPI)", sectionHeading);
            kpiHeading.setSpacingAfter(10);
            document.add(kpiHeading);

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(20);

            long totalEmps = activeEmployees.size();
            double totalBudget = departments.stream().mapToDouble(d -> d.getBudget() != null ? d.getBudget().doubleValue() : 0.0).sum();
            double avgSalary = activeEmployees.stream().mapToDouble(e -> e.getSalary() != null ? e.getSalary().doubleValue() : 0.0).average().orElse(0.0);

            Font kpiValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new java.awt.Color(0, 91, 127));
            Font kpiLblFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new java.awt.Color(100, 116, 139));

            PdfPCell kpi1 = new PdfPCell();
            kpi1.setBackgroundColor(new java.awt.Color(248, 250, 252));
            kpi1.setPadding(10);
            kpi1.setBorderColor(new java.awt.Color(226, 232, 240));
            kpi1.addElement(new Paragraph(String.valueOf(totalEmps), kpiValFont));
            kpi1.addElement(new Paragraph("Employes Actifs", kpiLblFont));
            kpiTable.addCell(kpi1);

            PdfPCell kpi2 = new PdfPCell();
            kpi2.setBackgroundColor(new java.awt.Color(248, 250, 252));
            kpi2.setPadding(10);
            kpi2.setBorderColor(new java.awt.Color(226, 232, 240));
            kpi2.addElement(new Paragraph(String.format("%,.2f MAD", avgSalary), kpiValFont));
            kpi2.addElement(new Paragraph("Salaire Moyen Mensuel", kpiLblFont));
            kpiTable.addCell(kpi2);

            PdfPCell kpi3 = new PdfPCell();
            kpi3.setBackgroundColor(new java.awt.Color(248, 250, 252));
            kpi3.setPadding(10);
            kpi3.setBorderColor(new java.awt.Color(226, 232, 240));
            kpi3.addElement(new Paragraph(String.format("%,.0f MAD", totalBudget), kpiValFont));
            kpi3.addElement(new Paragraph("Masse Budgetaire Totale", kpiLblFont));
            kpiTable.addCell(kpi3);

            document.add(kpiTable);

            Paragraph deptHeading = new Paragraph("Repartition Budgetaire par Departement", sectionHeading);
            deptHeading.setSpacingAfter(10);
            document.add(deptHeading);

            PdfPTable deptTable = new PdfPTable(3);
            deptTable.setWidthPercentage(100);
            deptTable.setSpacingAfter(20);
            deptTable.setWidths(new float[]{2f, 1f, 1f});

            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
            Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new java.awt.Color(51, 65, 85));

            String[] deptHeaders = {"Departement", "Budget Mensuel", "Masse Salariale"};
            for (String headerText : deptHeaders) {
                PdfPCell hCell = new PdfPCell(new Paragraph(headerText, tableHeaderFont));
                hCell.setBackgroundColor(new java.awt.Color(0, 91, 127));
                hCell.setPadding(6);
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                deptTable.addCell(hCell);
            }

            for (Department d : departments) {
                double deptSal = activeEmployees.stream()
                        .filter(e -> e.getDepartment() != null && e.getDepartment().getId().equals(d.getId()))
                        .mapToDouble(e -> e.getSalary() != null ? e.getSalary().doubleValue() : 0.0)
                        .sum();

                PdfPCell c1 = new PdfPCell(new Paragraph(d.getDeptName(), tableCellFont));
                c1.setPadding(6);
                deptTable.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Paragraph(String.format("%,.2f MAD", d.getBudget()), tableCellFont));
                c2.setPadding(6);
                c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                deptTable.addCell(c2);

                PdfPCell c3 = new PdfPCell(new Paragraph(String.format("%,.2f MAD", deptSal), tableCellFont));
                c3.setPadding(6);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                deptTable.addCell(c3);
            }
            document.add(deptTable);

            Paragraph empHeading = new Paragraph("Synthese Globale des Employes", sectionHeading);
            empHeading.setSpacingAfter(10);
            document.add(empHeading);

            PdfPTable empTable = new PdfPTable(4);
            empTable.setWidthPercentage(100);
            empTable.setWidths(new float[]{2f, 2f, 1.5f, 1.5f});

            String[] empHeaders = {"Nom", "Departement", "Salaire Mensuel", "Risque d'Attrition"};
            for (String headerText : empHeaders) {
                PdfPCell hCell = new PdfPCell(new Paragraph(headerText, tableHeaderFont));
                hCell.setBackgroundColor(new java.awt.Color(0, 91, 127));
                hCell.setPadding(6);
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                empTable.addCell(hCell);
            }

            for (Employee e : activeEmployees) {
                PdfPCell c1 = new PdfPCell(new Paragraph(e.getName(), tableCellFont));
                c1.setPadding(6);
                empTable.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Paragraph(e.getDepartment() != null ? e.getDepartment().getDeptName() : "N/A", tableCellFont));
                c2.setPadding(6);
                empTable.addCell(c2);

                PdfPCell c3 = new PdfPCell(new Paragraph(String.format("%,.2f MAD", e.getSalary() != null ? e.getSalary().doubleValue() : 0.0), tableCellFont));
                c3.setPadding(6);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                empTable.addCell(c3);

                double risk = e.getAttritionRisk() != null ? e.getAttritionRisk() : 0.0;
                Font riskFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, 
                        risk >= 60.0 ? new java.awt.Color(220, 38, 38) : new java.awt.Color(22, 163, 74));
                PdfPCell c4 = new PdfPCell(new Paragraph(String.format("%.1f%%", risk), riskFont));
                c4.setPadding(6);
                c4.setHorizontalAlignment(Element.ALIGN_CENTER);
                empTable.addCell(c4);
            }
            document.add(empTable);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating HR PDF report: ", e);
            throw new RuntimeException("Echec de la generation du rapport PDF", e);
        }
    }
}
