package com.example.project_manager.service;

import com.example.project_manager.dto.PortfolioReportDTO;
import com.example.project_manager.model.Project;
import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private PortfolioReportService reportService;

    @Test
    @DisplayName("Deve gerar relatório com projetos em diferentes status")
    void shouldGenerateReportWithMultipleStatuses() {
        Project p1 = Project.builder()
                .id(1L).name("P1").status(ProjectStatus.EM_ANALISE)
                .budget(new BigDecimal("100000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .managerId(1L).memberIds(new HashSet<>()).build();

        Project p2 = Project.builder()
                .id(2L).name("P2").status(ProjectStatus.EM_ANALISE)
                .budget(new BigDecimal("200000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .managerId(1L).memberIds(new HashSet<>()).build();

        Project p3 = Project.builder()
                .id(3L).name("P3").status(ProjectStatus.ENCERRADO)
                .budget(new BigDecimal("150000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .actualEndDate(LocalDate.of(2025, 4, 1))
                .managerId(1L).memberIds(new HashSet<>()).build();

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2, p3));
        when(projectRepository.findAllAllocatedMemberIds()).thenReturn(List.of(1L, 2L));

        PortfolioReportDTO report = reportService.generateReport();

        assertNotNull(report);
        assertEquals(2L, report.getProjectCountByStatus().get(ProjectStatus.EM_ANALISE));
        assertEquals(1L, report.getProjectCountByStatus().get(ProjectStatus.ENCERRADO));
        assertEquals(new BigDecimal("300000"), report.getTotalBudgetByStatus().get(ProjectStatus.EM_ANALISE));
        assertEquals(new BigDecimal("150000"), report.getTotalBudgetByStatus().get(ProjectStatus.ENCERRADO));
        assertEquals(2L, report.getTotalUniqueAllocatedMembers());
        assertNotNull(report.getAverageDurationOfClosedProjects());
        assertEquals(90.0, report.getAverageDurationOfClosedProjects());
    }

    @Test
    @DisplayName("Deve retornar média nula quando não há projetos encerrados")
    void shouldReturnNullAverageWhenNoClosedProjects() {
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());
        when(projectRepository.findAllAllocatedMemberIds()).thenReturn(Collections.emptyList());

        PortfolioReportDTO report = reportService.generateReport();

        assertNull(report.getAverageDurationOfClosedProjects());
        assertEquals(0L, report.getTotalUniqueAllocatedMembers());
    }

    @Test
    @DisplayName("Deve calcular média de duração com múltiplos projetos encerrados")
    void shouldCalculateAverageWithMultipleClosedProjects() {
        Project p1 = Project.builder()
                .id(1L).name("P1").status(ProjectStatus.ENCERRADO)
                .budget(new BigDecimal("100000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .actualEndDate(LocalDate.of(2025, 4, 1))
                .managerId(1L).memberIds(new HashSet<>()).build();

        Project p2 = Project.builder()
                .id(2L).name("P2").status(ProjectStatus.ENCERRADO)
                .budget(new BigDecimal("200000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .actualEndDate(LocalDate.of(2025, 7, 1))
                .managerId(1L).memberIds(new HashSet<>()).build();

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));
        when(projectRepository.findAllAllocatedMemberIds()).thenReturn(Collections.emptyList());

        PortfolioReportDTO report = reportService.generateReport();

        // p1: 90 days, p2: 181 days -> average = (90 + 181) / 2 = 135.5
        assertEquals(135.5, report.getAverageDurationOfClosedProjects());
    }
}
