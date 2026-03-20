package com.example.project_manager.mapper;

import com.example.project_manager.dto.MemberResponseDTO;
import com.example.project_manager.dto.ProjectRequestDTO;
import com.example.project_manager.dto.ProjectResponseDTO;
import com.example.project_manager.model.Project;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProjectMapper {

    public Project toEntity(ProjectRequestDTO dto) {
        return Project.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .expectedEndDate(dto.getExpectedEndDate())
                .actualEndDate(dto.getActualEndDate())
                .budget(dto.getBudget())
                .description(dto.getDescription())
                .managerId(dto.getManagerId())
                .build();
    }

    public void updateEntity(Project project, ProjectRequestDTO dto) {
        project.setName(dto.getName());
        project.setStartDate(dto.getStartDate());
        project.setExpectedEndDate(dto.getExpectedEndDate());
        project.setActualEndDate(dto.getActualEndDate());
        project.setBudget(dto.getBudget());
        project.setDescription(dto.getDescription());
        project.setManagerId(dto.getManagerId());
    }

    public ProjectResponseDTO toResponseDTO(Project project,
                                            MemberResponseDTO manager,
                                            Set<MemberResponseDTO> members) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .startDate(project.getStartDate())
                .expectedEndDate(project.getExpectedEndDate())
                .actualEndDate(project.getActualEndDate())
                .budget(project.getBudget())
                .description(project.getDescription())
                .status(project.getStatus())
                .riskClassification(project.getRiskClassification())
                .manager(manager)
                .members(members)
                .build();
    }
}
