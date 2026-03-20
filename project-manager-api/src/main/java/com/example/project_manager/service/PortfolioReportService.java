package com.example.project_manager.service;

import com.example.project_manager.dto.PortfolioReportDTO;
import com.example.project_manager.model.Project;
import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioReportService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public PortfolioReportDTO generateReport() {
        List<Project> allProjects = projectRepository.findAll();

        Map<ProjectStatus, Long> countByStatus = new EnumMap<>(ProjectStatus.class);
        Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<>(ProjectStatus.class);

        for (ProjectStatus status : ProjectStatus.values()) {
            countByStatus.put(status, 0L);
            budgetByStatus.put(status, BigDecimal.ZERO);
        }

        long totalDays = 0;
        long closedCount = 0;

        for (Project project : allProjects) {
            ProjectStatus status = project.getStatus();
            countByStatus.merge(status, 1L, Long::sum);
            budgetByStatus.merge(status, project.getBudget(), BigDecimal::add);

            if (status == ProjectStatus.ENCERRADO
                    && project.getActualEndDate() != null
                    && project.getStartDate() != null) {
                totalDays += ChronoUnit.DAYS.between(project.getStartDate(), project.getActualEndDate());
                closedCount++;
            }
        }

        Double averageDuration = closedCount > 0 ? (double) totalDays / closedCount : null;

        List<Long> allocatedMemberIds = projectRepository.findAllAllocatedMemberIds();
        long uniqueMembers = allocatedMemberIds.size();

        return PortfolioReportDTO.builder()
                .projectCountByStatus(countByStatus)
                .totalBudgetByStatus(budgetByStatus)
                .averageDurationOfClosedProjects(averageDuration)
                .totalUniqueAllocatedMembers(uniqueMembers)
                .build();
    }
}
