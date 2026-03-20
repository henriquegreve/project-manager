package com.example.project_manager.dto;

import com.example.project_manager.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioReportDTO {

    private Map<ProjectStatus, Long> projectCountByStatus;
    private Map<ProjectStatus, BigDecimal> totalBudgetByStatus;
    private Double averageDurationOfClosedProjects;
    private Long totalUniqueAllocatedMembers;
}
