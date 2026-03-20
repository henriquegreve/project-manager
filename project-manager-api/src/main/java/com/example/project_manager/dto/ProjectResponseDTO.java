package com.example.project_manager.dto;

import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.model.RiskClassification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal budget;
    private String description;
    private ProjectStatus status;
    private RiskClassification riskClassification;
    private MemberResponseDTO manager;
    private Set<MemberResponseDTO> members;
}
