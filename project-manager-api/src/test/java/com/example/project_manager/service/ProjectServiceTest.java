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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MembersApiClient membersApiClient;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectRequestDTO requestDTO;
    private ProjectResponseDTO responseDTO;
    private MemberResponseDTO managerDTO;
    private MemberResponseDTO employeeDTO;
    private MemberResponseDTO invalidManagerDTO;

    @BeforeEach
    void setUp() {
        managerDTO = MemberResponseDTO.builder()
                .id(1L)
                .name("Manager")
                .role("gerente")
                .build();

        employeeDTO = MemberResponseDTO.builder()
                .id(2L)
                .name("Funcionario")
                .role("funcionario")
                .build();

        invalidManagerDTO = MemberResponseDTO.builder()
                .id(1L)
                .name("Funcionario Como Gerente")
                .role("funcionario")
                .build();

        project = Project.builder()
                .id(1L)
                .name("Projeto Teste")
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .budget(new BigDecimal("200000"))
                .description("Descrição teste")
                .status(ProjectStatus.EM_ANALISE)
                .managerId(1L)
                .memberIds(new HashSet<>())
                .build();

        requestDTO = ProjectRequestDTO.builder()
                .name("Projeto Teste")
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .budget(new BigDecimal("200000"))
                .description("Descrição teste")
                .managerId(1L)
                .build();

        responseDTO = ProjectResponseDTO.builder()
                .id(1L)
                .name("Projeto Teste")
                .status(ProjectStatus.EM_ANALISE)
                .build();
    }

    private void stubBuildResponseDTO() {
        when(membersApiClient.findById(1L)).thenReturn(managerDTO);
        when(projectMapper.toResponseDTO(any(Project.class), any(MemberResponseDTO.class), anySet()))
                .thenReturn(responseDTO);
    }

    @Test
    @DisplayName("Deve criar projeto com sucesso")
    void shouldCreateProject() {
        when(membersApiClient.findById(1L)).thenReturn(managerDTO);
        when(projectMapper.toEntity(requestDTO)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponseDTO(eq(project), eq(managerDTO), anySet())).thenReturn(responseDTO);

        ProjectResponseDTO result = projectService.create(requestDTO);

        assertNotNull(result);
        assertEquals("Projeto Teste", result.getName());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Deve rejeitar criação quando managerId não for de gerente")
    void shouldRejectCreateWhenManagerIsNotManager() {
        when(membersApiClient.findById(1L)).thenReturn(invalidManagerDTO);

        assertThrows(BusinessRuleException.class, () -> projectService.create(requestDTO));
    }

    @Test
    @DisplayName("Deve buscar projeto por ID com sucesso")
    void shouldFindProjectById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        stubBuildResponseDTO();

        ProjectResponseDTO result = projectService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando projeto não encontrado")
    void shouldThrowWhenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.findById(99L));
    }

    @Test
    @DisplayName("Deve atualizar projeto com sucesso")
    void shouldUpdateProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(membersApiClient.findById(1L)).thenReturn(managerDTO);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponseDTO(any(Project.class), any(MemberResponseDTO.class), anySet()))
                .thenReturn(responseDTO);

        ProjectResponseDTO result = projectService.update(1L, requestDTO);

        assertNotNull(result);
        verify(projectMapper).updateEntity(project, requestDTO);
    }

    @Test
    @DisplayName("Deve rejeitar update quando managerId não for de gerente")
    void shouldRejectUpdateWhenManagerIsNotManager() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(membersApiClient.findById(1L)).thenReturn(invalidManagerDTO);

        assertThrows(BusinessRuleException.class, () -> projectService.update(1L, requestDTO));
    }

    @Test
    @DisplayName("Deve excluir projeto com status EM_ANALISE")
    void shouldDeleteProjectWithDeletableStatus() {
        project.setStatus(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("Não deve excluir projeto com status INICIADO")
    void shouldNotDeleteProjectWithStatusIniciado() {
        project.setStatus(ProjectStatus.INICIADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(BusinessRuleException.class, () -> projectService.delete(1L));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("Não deve excluir projeto com status EM_ANDAMENTO")
    void shouldNotDeleteProjectWithStatusEmAndamento() {
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(BusinessRuleException.class, () -> projectService.delete(1L));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("Não deve excluir projeto com status ENCERRADO")
    void shouldNotDeleteProjectWithStatusEncerrado() {
        project.setStatus(ProjectStatus.ENCERRADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(BusinessRuleException.class, () -> projectService.delete(1L));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("Deve permitir transição de status válida")
    void shouldAllowValidStatusTransition() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        stubBuildResponseDTO();

        projectService.updateStatus(1L, ProjectStatus.ANALISE_REALIZADA);

        assertEquals(ProjectStatus.ANALISE_REALIZADA, project.getStatus());
    }

    @Test
    @DisplayName("Deve rejeitar transição de status inválida (pular etapa)")
    void shouldRejectInvalidStatusTransition() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(InvalidStatusTransitionException.class,
                () -> projectService.updateStatus(1L, ProjectStatus.INICIADO));
    }

    @Test
    @DisplayName("Deve permitir cancelamento de qualquer status ativo")
    void shouldAllowCancellation() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        stubBuildResponseDTO();

        projectService.updateStatus(1L, ProjectStatus.CANCELADO);

        assertEquals(ProjectStatus.CANCELADO, project.getStatus());
    }

    @Test
    @DisplayName("Deve associar membro funcionário ao projeto")
    void shouldAddEmployeeMember() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(membersApiClient.findById(2L)).thenReturn(employeeDTO);
        when(projectRepository.countActiveProjectsByMember(eq(2L), anyList())).thenReturn(0L);
        when(projectRepository.save(project)).thenReturn(project);
        when(membersApiClient.findById(1L)).thenReturn(managerDTO);
        when(projectMapper.toResponseDTO(any(Project.class), any(MemberResponseDTO.class), anySet()))
                .thenReturn(responseDTO);

        projectService.addMember(1L, 2L);

        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Deve rejeitar associação de membro não-funcionário")
    void shouldRejectNonEmployeeMember() {
        MemberResponseDTO consultorDTO = MemberResponseDTO.builder()
                .id(3L).name("Consultor").role("consultor").build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(membersApiClient.findById(3L)).thenReturn(consultorDTO);

        assertThrows(BusinessRuleException.class,
                () -> projectService.addMember(1L, 3L));
    }

    @Test
    @DisplayName("Deve rejeitar quando projeto já tem 10 membros")
    void shouldRejectWhenMaxMembersReached() {
        Set<Long> memberIds = new HashSet<>();
        for (long i = 10; i < 20; i++) {
            memberIds.add(i);
        }
        project.setMemberIds(memberIds);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(membersApiClient.findById(2L)).thenReturn(employeeDTO);

        assertThrows(BusinessRuleException.class,
                () -> projectService.addMember(1L, 2L));
    }

    @Test
    @DisplayName("Deve rejeitar quando membro já está em 3 projetos ativos")
    void shouldRejectWhenMemberHasMaxActiveProjects() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(membersApiClient.findById(2L)).thenReturn(employeeDTO);
        when(projectRepository.countActiveProjectsByMember(eq(2L), anyList())).thenReturn(3L);

        assertThrows(BusinessRuleException.class,
                () -> projectService.addMember(1L, 2L));
    }

    @Test
    @DisplayName("Deve remover membro do projeto")
    void shouldRemoveMember() {
        project.getMemberIds().add(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        stubBuildResponseDTO();

        projectService.removeMember(1L, 2L);

        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Deve lançar exceção ao remover membro não associado")
    void shouldThrowWhenRemovingNonAssociatedMember() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(BusinessRuleException.class,
                () -> projectService.removeMember(1L, 2L));
    }

    @Test
    @DisplayName("findAll com filtro de nome deve montar Specification com LIKE (sem status)")
    void shouldBuildSpecificationWithLikePredicateWhenNameProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        String nameFilter = "Teste";

        Page<Project> repoPage = new PageImpl<>(List.of(project), pageable, 1);

        ArgumentCaptor<Specification<Project>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(projectRepository.findAll(specCaptor.capture(), eq(pageable))).thenReturn(repoPage);
        stubBuildResponseDTO();

        Page<ProjectResponseDTO> result = projectService.findAll(nameFilter, null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        Specification<Project> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);

        CriteriaBuilder cb = org.mockito.Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = org.mockito.Mockito.mock(CriteriaQuery.class);
        Root<Project> root = org.mockito.Mockito.mock(Root.class);

        Path<Object> namePath = org.mockito.Mockito.mock(Path.class);
        Expression<String> loweredName = org.mockito.Mockito.mock(Expression.class);

        when(root.get("name")).thenReturn(namePath);
        when(cb.lower((Expression) namePath)).thenReturn(loweredName);

        Predicate likePredicate = org.mockito.Mockito.mock(Predicate.class);
        when(cb.like(loweredName, "%teste%")).thenReturn(likePredicate);

        Predicate andPredicate = org.mockito.Mockito.mock(Predicate.class);
        ArgumentCaptor<Predicate[]> andCaptor = ArgumentCaptor.forClass(Predicate[].class);
        when(cb.and(andCaptor.capture())).thenReturn(andPredicate);

        Predicate resultPredicate = capturedSpec.toPredicate(root, query, cb);

        assertNotNull(resultPredicate);
        verify(cb).like(loweredName, "%teste%");
        verify(cb, never()).equal(any(), any());

        Predicate[] predicatesPassed = andCaptor.getValue();
        assertEquals(1, predicatesPassed.length);
    }

    @Test
    @DisplayName("findAll com filtro de status deve montar Specification com EQUAL (sem nome)")
    void shouldBuildSpecificationWithEqualPredicateWhenStatusProvided() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> repoPage = new PageImpl<>(List.of(project), pageable, 1);

        ArgumentCaptor<Specification<Project>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(projectRepository.findAll(specCaptor.capture(), eq(pageable))).thenReturn(repoPage);
        stubBuildResponseDTO();

        Page<ProjectResponseDTO> result = projectService.findAll(null, ProjectStatus.INICIADO, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        Specification<Project> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);

        CriteriaBuilder cb = org.mockito.Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = org.mockito.Mockito.mock(CriteriaQuery.class);
        Root<Project> root = org.mockito.Mockito.mock(Root.class);

        Path<Object> statusPath = org.mockito.Mockito.mock(Path.class);
        Predicate equalPredicate = org.mockito.Mockito.mock(Predicate.class);
        Predicate andPredicate = org.mockito.Mockito.mock(Predicate.class);

        when(root.get("status")).thenReturn(statusPath);
        when(cb.equal(statusPath, ProjectStatus.INICIADO)).thenReturn(equalPredicate);

        ArgumentCaptor<Predicate[]> andCaptor = ArgumentCaptor.forClass(Predicate[].class);
        when(cb.and(andCaptor.capture())).thenReturn(andPredicate);

        Predicate resultPredicate = capturedSpec.toPredicate(root, query, cb);
        assertNotNull(resultPredicate);

        verify(cb, never()).like(any(Expression.class), anyString());
        verify(cb).equal(statusPath, ProjectStatus.INICIADO);

        Predicate[] predicatesPassed = andCaptor.getValue();
        assertEquals(1, predicatesPassed.length);
    }

    @Test
    @DisplayName("findAll com nome em branco deve ignorar LIKE e criar Specification sem predicados")
    void shouldIgnoreNamePredicateWhenNameIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> repoPage = new PageImpl<>(List.of(project), pageable, 1);

        ArgumentCaptor<Specification<Project>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        when(projectRepository.findAll(specCaptor.capture(), eq(pageable))).thenReturn(repoPage);
        stubBuildResponseDTO();

        Page<ProjectResponseDTO> result = projectService.findAll("   ", null, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        Specification<Project> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);

        CriteriaBuilder cb = org.mockito.Mockito.mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = org.mockito.Mockito.mock(CriteriaQuery.class);
        Root<Project> root = org.mockito.Mockito.mock(Root.class);

        ArgumentCaptor<Predicate[]> andCaptor = ArgumentCaptor.forClass(Predicate[].class);
        Predicate andPredicate = org.mockito.Mockito.mock(Predicate.class);
        when(cb.and(andCaptor.capture())).thenReturn(andPredicate);

        Predicate resultPredicate = capturedSpec.toPredicate(root, query, cb);
        assertNotNull(resultPredicate);

        Predicate[] predicatesPassed = andCaptor.getValue();
        assertEquals(0, predicatesPassed.length);
    }
}
