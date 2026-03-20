package com.example.project_manager.service;

import com.example.project_manager.dto.PortfolioReportDTO;
import com.example.project_manager.model.ProjectStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JasperReportServiceTest {

    private final JasperReportService jasperReportService = new JasperReportService();

    @Test
    @DisplayName("Deve gerar PDF com dados completos do portfólio")
    void shouldGeneratePdfWithFullData() {
        Map<ProjectStatus, Long> countByStatus = new EnumMap<>(ProjectStatus.class);
        countByStatus.put(ProjectStatus.EM_ANALISE, 3L);
        countByStatus.put(ProjectStatus.INICIADO, 2L);
        countByStatus.put(ProjectStatus.ENCERRADO, 1L);

        Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<>(ProjectStatus.class);
        budgetByStatus.put(ProjectStatus.EM_ANALISE, new BigDecimal("300000.00"));
        budgetByStatus.put(ProjectStatus.INICIADO, new BigDecimal("150000.00"));
        budgetByStatus.put(ProjectStatus.ENCERRADO, new BigDecimal("80000.00"));

        PortfolioReportDTO dto = PortfolioReportDTO.builder()
                .projectCountByStatus(countByStatus)
                .totalBudgetByStatus(budgetByStatus)
                .averageDurationOfClosedProjects(90.5)
                .totalUniqueAllocatedMembers(15L)
                .build();

        byte[] pdf = jasperReportService.generatePortfolioPdf(dto);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertTrue(pdf[0] == 0x25 && pdf[1] == 0x50 && pdf[2] == 0x44 && pdf[3] == 0x46,
                "O arquivo deve iniciar com o magic number %PDF");
    }

    @Test
    @DisplayName("Deve gerar PDF mesmo sem projetos encerrados (média nula)")
    void shouldGeneratePdfWithNullAverage() {
        Map<ProjectStatus, Long> countByStatus = new EnumMap<>(ProjectStatus.class);
        countByStatus.put(ProjectStatus.EM_ANALISE, 1L);

        Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<>(ProjectStatus.class);
        budgetByStatus.put(ProjectStatus.EM_ANALISE, new BigDecimal("100000.00"));

        PortfolioReportDTO dto = PortfolioReportDTO.builder()
                .projectCountByStatus(countByStatus)
                .totalBudgetByStatus(budgetByStatus)
                .averageDurationOfClosedProjects(null)
                .totalUniqueAllocatedMembers(0L)
                .build();

        byte[] pdf = jasperReportService.generatePortfolioPdf(dto);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
