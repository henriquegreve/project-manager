package com.example.project_manager.controller;

import com.example.project_manager.dto.PortfolioReportDTO;
import com.example.project_manager.service.JasperReportService;
import com.example.project_manager.service.PortfolioReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Tag(name = "Relatório de Portfólio", description = "Endpoint para geração de relatório resumido do portfólio")
public class PortfolioReportController {

    private final PortfolioReportService reportService;
    private final JasperReportService jasperReportService;

    @GetMapping("/report")
    @Operation(summary = "Gerar relatório do portfólio em JSON",
            description = "Retorna quantidade de projetos por status, total orçado por status, "
                    + "média de duração dos projetos encerrados e total de membros únicos alocados")
    public ResponseEntity<PortfolioReportDTO> generateReport() {
        return ResponseEntity.ok(reportService.generateReport());
    }

    @GetMapping(value = "/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Gerar relatório do portfólio em PDF",
            description = "Retorna o relatório de portfólio em formato PDF utilizando JasperReports")
    public ResponseEntity<byte[]> generatePdfReport() {
        PortfolioReportDTO reportDTO = reportService.generateReport();
        byte[] pdfBytes = jasperReportService.generatePortfolioPdf(reportDTO);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "portfolio-report.pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
