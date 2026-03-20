package com.example.project_manager.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum RiskClassification {

    BAIXO,
    MEDIO,
    ALTO;

    private static final BigDecimal LOW_THRESHOLD = new BigDecimal("100000");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("500000");
    private static final long THREE_MONTHS_DAYS = 90;
    private static final long SIX_MONTHS_DAYS = 180;

    public static RiskClassification calculate(BigDecimal budget, LocalDate startDate, LocalDate expectedEndDate) {
        long days = ChronoUnit.DAYS.between(startDate, expectedEndDate);

        boolean highBudget = budget.compareTo(HIGH_THRESHOLD) > 0;
        boolean longDeadline = days > SIX_MONTHS_DAYS;

        if (highBudget || longDeadline) {
            return ALTO;
        }

        boolean mediumBudget = budget.compareTo(LOW_THRESHOLD) > 0;
        boolean mediumDeadline = days > THREE_MONTHS_DAYS;

        if (mediumBudget || mediumDeadline) {
            return MEDIO;
        }

        return BAIXO;
    }
}
