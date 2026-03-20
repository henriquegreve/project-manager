package com.example.project_manager.service;

import com.example.project_manager.dto.PortfolioReportDTO;
import com.example.project_manager.model.ProjectStatus;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class JasperReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;
    private static final int COLUMN_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    private static final int COL1_WIDTH = 200;
    private static final int COL2_WIDTH = 155;
    private static final int COL3_WIDTH = COLUMN_WIDTH - COL1_WIDTH - COL2_WIDTH;

    public byte[] generatePortfolioPdf(PortfolioReportDTO reportDTO) {
        try {
            JasperDesign design = buildDesign();
            JasperReport jasperReport = JasperCompileManager.compileReport(design);

            Map<String, Object> parameters = buildParameters(reportDTO);
            List<Map<String, Object>> rows = buildDataRows(reportDTO);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    private JasperDesign buildDesign() throws JRException {
        JasperDesign design = new JasperDesign();
        design.setName("portfolio_report");
        design.setPageWidth(PAGE_WIDTH);
        design.setPageHeight(PAGE_HEIGHT);
        design.setLeftMargin(MARGIN);
        design.setRightMargin(MARGIN);
        design.setTopMargin(MARGIN);
        design.setBottomMargin(MARGIN);
        design.setColumnWidth(COLUMN_WIDTH);

        addParameters(design);
        addFields(design);

        design.setTitle(buildTitleBand());
        design.setColumnHeader(buildColumnHeaderBand());
        ((JRDesignSection) design.getDetailSection()).addBand(buildDetailBand());
        design.setSummary(buildSummaryBand());

        return design;
    }

    private void addParameters(JasperDesign design) throws JRException {
        design.addParameter(createParameter("averageDuration", String.class));
        design.addParameter(createParameter("totalUniqueMembers", Long.class));
        design.addParameter(createParameter("generatedAt", String.class));
    }

    private JRDesignParameter createParameter(String name, Class<?> type) {
        JRDesignParameter param = new JRDesignParameter();
        param.setName(name);
        param.setValueClass(type);
        return param;
    }

    private void addFields(JasperDesign design) throws JRException {
        design.addField(createField("status", String.class));
        design.addField(createField("projectCount", Long.class));
        design.addField(createField("totalBudget", String.class));
    }

    private JRDesignField createField(String name, Class<?> type) {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(type);
        return field;
    }

    private JRDesignBand buildTitleBand() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(80);

        JRDesignStaticText title = createStaticText(0, 0, COLUMN_WIDTH, 35,
                "Relatório de Portfólio", 18, true,
                HorizontalTextAlignEnum.CENTER, VerticalTextAlignEnum.MIDDLE);
        band.addElement(title);

        JRDesignTextField dateField = createTextField(0, 40, COLUMN_WIDTH, 20,
                "\"Gerado em: \" + $P{generatedAt}", 10,
                HorizontalTextAlignEnum.CENTER);
        band.addElement(dateField);

        return band;
    }

    private JRDesignBand buildColumnHeaderBand() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(25);

        int x = 0;
        band.addElement(createStaticText(x, 0, COL1_WIDTH, 25, "Status", 11, true,
                HorizontalTextAlignEnum.CENTER, VerticalTextAlignEnum.MIDDLE));
        x += COL1_WIDTH;
        band.addElement(createStaticText(x, 0, COL2_WIDTH, 25, "Qtd. Projetos", 11, true,
                HorizontalTextAlignEnum.CENTER, VerticalTextAlignEnum.MIDDLE));
        x += COL2_WIDTH;
        band.addElement(createStaticText(x, 0, COL3_WIDTH, 25, "Total Orçado (R$)", 11, true,
                HorizontalTextAlignEnum.CENTER, VerticalTextAlignEnum.MIDDLE));

        return band;
    }

    private JRDesignBand buildDetailBand() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(20);

        int x = 0;
        band.addElement(createTextField(x, 0, COL1_WIDTH, 20, "$F{status}", 9,
                HorizontalTextAlignEnum.CENTER));
        x += COL1_WIDTH;
        band.addElement(createTextField(x, 0, COL2_WIDTH, 20, "$F{projectCount}", 9,
                HorizontalTextAlignEnum.CENTER));
        x += COL2_WIDTH;
        band.addElement(createTextField(x, 0, COL3_WIDTH, 20, "$F{totalBudget}", 9,
                HorizontalTextAlignEnum.CENTER));

        return band;
    }

    private JRDesignBand buildSummaryBand() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(80);

        JRDesignStaticText header = createStaticText(0, 15, 300, 20,
                "Resumo Geral", 11, true,
                HorizontalTextAlignEnum.LEFT, VerticalTextAlignEnum.MIDDLE);
        band.addElement(header);

        band.addElement(createTextField(0, 38, COLUMN_WIDTH, 18,
                "\"Média de duração dos projetos encerrados: \" + $P{averageDuration}", 10,
                HorizontalTextAlignEnum.LEFT));

        band.addElement(createTextField(0, 58, COLUMN_WIDTH, 18,
                "\"Total de membros únicos alocados: \" + $P{totalUniqueMembers}", 10,
                HorizontalTextAlignEnum.LEFT));

        return band;
    }

    private JRDesignStaticText createStaticText(int x, int y, int width, int height,
                                                 String text, int fontSize, boolean bold,
                                                 HorizontalTextAlignEnum hAlign,
                                                 VerticalTextAlignEnum vAlign) {
        JRDesignStaticText staticText = new JRDesignStaticText();
        staticText.setX(x);
        staticText.setY(y);
        staticText.setWidth(width);
        staticText.setHeight(height);
        staticText.setText(text);
        staticText.setFontSize((float) fontSize);
        staticText.setBold(bold);
        staticText.setHorizontalTextAlign(hAlign);
        staticText.setVerticalTextAlign(vAlign);
        return staticText;
    }

    private JRDesignTextField createTextField(int x, int y, int width, int height,
                                               String expression, int fontSize,
                                               HorizontalTextAlignEnum hAlign) {
        JRDesignTextField textField = new JRDesignTextField();
        textField.setX(x);
        textField.setY(y);
        textField.setWidth(width);
        textField.setHeight(height);
        textField.setFontSize((float) fontSize);
        textField.setHorizontalTextAlign(hAlign);

        JRDesignExpression expr = new JRDesignExpression();
        expr.setText(expression);
        textField.setExpression(expr);

        return textField;
    }

    private Map<String, Object> buildParameters(PortfolioReportDTO dto) {
        Map<String, Object> params = new HashMap<>();

        String avgDuration = dto.getAverageDurationOfClosedProjects() != null
                ? String.format("%.1f dias", dto.getAverageDurationOfClosedProjects())
                : "N/A (nenhum projeto encerrado)";

        params.put("averageDuration", avgDuration);
        params.put("totalUniqueMembers", dto.getTotalUniqueAllocatedMembers());
        params.put("generatedAt", LocalDateTime.now().format(DATE_FMT));

        return params;
    }

    private List<Map<String, Object>> buildDataRows(PortfolioReportDTO dto) {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (ProjectStatus status : ProjectStatus.values()) {
            Long count = dto.getProjectCountByStatus().getOrDefault(status, 0L);
            BigDecimal budget = dto.getTotalBudgetByStatus().getOrDefault(status, BigDecimal.ZERO);

            Map<String, Object> row = new HashMap<>();
            row.put("status", formatStatus(status));
            row.put("projectCount", count);
            row.put("totalBudget", CURRENCY_FMT.format(budget));
            rows.add(row);
        }

        return rows;
    }

    private String formatStatus(ProjectStatus status) {
        return switch (status) {
            case EM_ANALISE -> "Em Análise";
            case ANALISE_REALIZADA -> "Análise Realizada";
            case ANALISE_APROVADA -> "Análise Aprovada";
            case INICIADO -> "Iniciado";
            case PLANEJADO -> "Planejado";
            case EM_ANDAMENTO -> "Em Andamento";
            case ENCERRADO -> "Encerrado";
            case CANCELADO -> "Cancelado";
        };
    }
}
