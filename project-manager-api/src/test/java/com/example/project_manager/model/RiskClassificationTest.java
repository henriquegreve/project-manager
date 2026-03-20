package com.example.project_manager.model;

import com.example.project_manager.model.RiskClassification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskClassificationTest {

    @Test
    @DisplayName("Deve retornar BAIXO risco para orçamento <= 100.000 e prazo <= 3 meses")
    void shouldReturnBaixoRisk() {
        BigDecimal budget = new BigDecimal("50000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 1);

        assertEquals(RiskClassification.BAIXO, RiskClassification.calculate(budget, start, end));
    }

    @Test
    @DisplayName("Deve retornar BAIXO risco no limite exato (100.000 e 90 dias)")
    void shouldReturnBaixoRiskAtExactLimit() {
        BigDecimal budget = new BigDecimal("100000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 4, 1);

        assertEquals(RiskClassification.BAIXO, RiskClassification.calculate(budget, start, end));
    }

    @Test
    @DisplayName("Deve retornar MEDIO risco para orçamento entre 100.001 e 500.000")
    void shouldReturnMedioRiskForMediumBudget() {
        BigDecimal budget = new BigDecimal("200000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 1);

        assertEquals(RiskClassification.MEDIO, RiskClassification.calculate(budget, start, end));
    }

    @Test
    @DisplayName("Deve retornar MEDIO risco para prazo entre 3 e 6 meses")
    void shouldReturnMedioRiskForMediumDeadline() {
        BigDecimal budget = new BigDecimal("50000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 5, 15);

        assertEquals(RiskClassification.MEDIO, RiskClassification.calculate(budget, start, end));
    }

    @Test
    @DisplayName("Deve retornar ALTO risco para orçamento > 500.000")
    void shouldReturnAltoRiskForHighBudget() {
        BigDecimal budget = new BigDecimal("600000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 3, 1);

        assertEquals(RiskClassification.ALTO, RiskClassification.calculate(budget, start, end));
    }

    @Test
    @DisplayName("Deve retornar ALTO risco para prazo > 6 meses")
    void shouldReturnAltoRiskForLongDeadline() {
        BigDecimal budget = new BigDecimal("50000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 8, 1);

        assertEquals(RiskClassification.ALTO, RiskClassification.calculate(budget, start, end));
    }

    @Test
    @DisplayName("Deve retornar ALTO risco quando ambos critérios são altos")
    void shouldReturnAltoRiskWhenBothCriteriaAreHigh() {
        BigDecimal budget = new BigDecimal("700000");
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 1);

        assertEquals(RiskClassification.ALTO, RiskClassification.calculate(budget, start, end));
    }
}
