package com.example.project_manager.service;

import com.example.project_manager.client.MembersApiClient;
import com.example.project_manager.dto.MemberResponseDTO;
import com.example.project_manager.dto.ProjectRequestDTO;
import com.example.project_manager.dto.ProjectResponseDTO;
import com.example.project_manager.exception.BusinessRuleException;
import com.example.project_manager.exception.InvalidStatusTransitionException;
import com.example.project_manager.exception.ResourceNotFoundException;
import com.example.project_manager.mapper.ProjectMapper;
import com.example.project_manager.model.Project;
import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.repository.ProjectRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final int MAX_MEMBERS = 10;
    private static final int MAX_ACTIVE_PROJECTS_PER_MEMBER = 3;
    private static final String EMPLOYEE_ROLE = "funcionario";
    private static final String MANAGER_ROLE = "gerente";

    private final ProjectRepository projectRepository;
    private final MembersApiClient membersApiClient;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponseDTO create(ProjectRequestDTO dto) {
        MemberResponseDTO manager = membersApiClient.findById(dto.getManagerId());
        if (!MANAGER_ROLE.equalsIgnoreCase(manager.getRole())) {
            throw new BusinessRuleException("managerId deve referenciar um membro com atribuição 'gerente'");
        }
        Project project = projectMapper.toEntity(dto);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponseDTO(project, manager, Set.of());
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO findById(Long id) {
        Project project = findEntityById(id);
        return buildResponseDTO(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> findAll(String name, ProjectStatus status, Pageable pageable) {
        Specification<Project> spec = buildSpecification(name, status);
        return projectRepository.findAll(spec, pageable)
                .map(this::buildResponseDTO);
    }

    @Transactional
    public ProjectResponseDTO update(Long id, ProjectRequestDTO dto) {
        Project project = findEntityById(id);
        // Reaproveita o gerente consultado para evitar chamada duplicada na montagem da resposta
        MemberResponseDTO manager = membersApiClient.findById(dto.getManagerId());
        if (!MANAGER_ROLE.equalsIgnoreCase(manager.getRole())) {
            throw new BusinessRuleException("managerId deve referenciar um membro com atribuição 'gerente'");
        }
        projectMapper.updateEntity(project, dto);
        project = projectRepository.save(project);
        return buildResponseDTO(project, manager, Map.of());
    }

    @Transactional
    public void delete(Long id) {
        Project project = findEntityById(id);

        if (!project.getStatus().isDeletable()) {
            throw new BusinessRuleException(
                    String.format("Projeto com status '%s' não pode ser excluído", project.getStatus()));
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponseDTO updateStatus(Long id, ProjectStatus newStatus) {
        Project project = findEntityById(id);

        if (!project.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(project.getStatus(), newStatus);
        }

        project.setStatus(newStatus);
        project = projectRepository.save(project);
        return buildResponseDTO(project);
    }

    @Transactional
    public ProjectResponseDTO addMember(Long projectId, Long memberId) {
        return addMembers(projectId, List.of(memberId));
    }

    @Transactional
    public ProjectResponseDTO addMembers(Long projectId, List<Long> memberIds) {
        Project project = findEntityById(projectId);

        // Dedupe preservando ordem (comportamento previsível em caso de erro)
        Set<Long> distinctIds = memberIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (distinctIds.isEmpty()) {
            throw new BusinessRuleException("Nenhum memberId informado");
        }

        // Busca DTOs uma vez e valida role (fail-fast)
        Map<Long, MemberResponseDTO> memberDtoById = new HashMap<>();
        for (Long memberId : distinctIds) {
            MemberResponseDTO member = membersApiClient.findById(memberId);

            if (!EMPLOYEE_ROLE.equalsIgnoreCase(member.getRole())) {
                throw new BusinessRuleException("Apenas membros com atribuição 'funcionário' podem ser associados a projetos");
            }
            memberDtoById.put(memberId, member);
        }

        // Considera que o Set do projeto evita duplicatas
        Set<Long> idsToAdd = distinctIds.stream()
                .filter(id -> !project.getMemberIds().contains(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (project.getMemberIds().size() + idsToAdd.size() > MAX_MEMBERS) {
            throw new BusinessRuleException(
                    String.format("O projeto já atingiu o limite máximo de %d membros", MAX_MEMBERS));
        }

        List<ProjectStatus> excludedStatuses = List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);
        for (Long memberId : idsToAdd) {
            long activeProjects = projectRepository.countActiveProjectsByMember(memberId, excludedStatuses);

            if (activeProjects >= MAX_ACTIVE_PROJECTS_PER_MEMBER) {
                throw new BusinessRuleException(
                        String.format("O membro já está alocado em %d projetos ativos (limite: %d)",
                                activeProjects, MAX_ACTIVE_PROJECTS_PER_MEMBER));
            }
            project.getMemberIds().add(memberId);
        }

        // Salva apenas uma vez após validar e adicionar todos os membros.
        Project savedProject = projectRepository.save(project);

        // Reaproveita os DTOs já consultados para evitar chamadas duplicadas.
        return buildResponseDTO(savedProject, null, memberDtoById);
    }

    @Transactional
    public ProjectResponseDTO removeMember(Long projectId, Long memberId) {
        Project project = findEntityById(projectId);

        boolean removed = project.getMemberIds().remove(memberId);
        if (!removed) {
            throw new BusinessRuleException("Membro não está associado a este projeto");
        }

        project = projectRepository.save(project);
        return buildResponseDTO(project);
    }

    private ProjectResponseDTO buildResponseDTO(Project project) {
        return buildResponseDTO(project, null, Map.of());
    }

    private ProjectResponseDTO buildResponseDTO(Project project,
                                                   MemberResponseDTO managerOverride,
                                                   Map<Long, MemberResponseDTO> memberOverrides) {
        MemberResponseDTO manager = managerOverride != null
                ? managerOverride
                : membersApiClient.findById(project.getManagerId());

        Set<MemberResponseDTO> members = project.getMemberIds().stream()
                .map(id -> memberOverrides.getOrDefault(id, membersApiClient.findById(id)))
                .collect(Collectors.toSet());

        return projectMapper.toResponseDTO(project, manager, members);
    }

    private Project findEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Specification<Project> buildSpecification(String name, ProjectStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
