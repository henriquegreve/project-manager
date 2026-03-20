package com.example.project_manager.controller;

import com.example.project_manager.config.SecurityConfig;
import com.example.project_manager.dto.PortfolioReportDTO;
import com.example.project_manager.exception.GlobalExceptionHandler;
import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.service.JasperReportService;
import com.example.project_manager.service.PortfolioReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortfolioReportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PortfolioReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioReportService reportService;

    @MockitoBean
    private JasperReportService jasperReportService;

    private PortfolioReportDTO buildSampleReport() {
        return PortfolioReportDTO.builder()
                .projectCountByStatus(Map.of(ProjectStatus.EM_ANALISE, 5L))
                .totalBudgetByStatus(Map.of(ProjectStatus.EM_ANALISE, new BigDecimal("500000")))
                .averageDurationOfClosedProjects(90.0)
                .totalUniqueAllocatedMembers(10L)
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/portfolio/report deve retornar 200 com relatório JSON")
    void shouldReturnReport() throws Exception {
        PortfolioReportDTO report = buildSampleReport();
        when(reportService.generateReport()).thenReturn(report);

        mockMvc.perform(get("/api/portfolio/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUniqueAllocatedMembers").value(10))
                .andExpect(jsonPath("$.averageDurationOfClosedProjects").value(90.0));
    }

    @Test
    @DisplayName("GET /api/portfolio/report deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/portfolio/report"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/portfolio/report/pdf deve retornar 200 com PDF")
    void shouldReturnPdfReport() throws Exception {
        PortfolioReportDTO report = buildSampleReport();
        byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF

        when(reportService.generateReport()).thenReturn(report);
        when(jasperReportService.generatePortfolioPdf(any(PortfolioReportDTO.class))).thenReturn(fakePdf);

        mockMvc.perform(get("/api/portfolio/report/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("GET /api/portfolio/report/pdf deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuthForPdf() throws Exception {
        mockMvc.perform(get("/api/portfolio/report/pdf"))
                .andExpect(status().isUnauthorized());
    }
}
