package com.example.project_manager.controller;

import com.example.project_manager.config.SecurityConfig;
import com.example.project_manager.dto.ProjectResponseDTO;
import com.example.project_manager.exception.BusinessRuleException;
import com.example.project_manager.exception.GlobalExceptionHandler;
import com.example.project_manager.exception.InvalidStatusTransitionException;
import com.example.project_manager.exception.ResourceNotFoundException;
import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.model.RiskClassification;
import com.example.project_manager.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private ProjectResponseDTO buildResponse() {
        return ProjectResponseDTO.builder()
                .id(1L)
                .name("Projeto Teste")
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .budget(new BigDecimal("200000"))
                .status(ProjectStatus.EM_ANALISE)
                .riskClassification(RiskClassification.MEDIO)
                .members(Set.of())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /api/projects deve retornar 201")
    void shouldCreateProject() throws Exception {
        when(projectService.create(any())).thenReturn(buildResponse());

        String json = """
                {
                    "name": "Projeto Teste",
                    "startDate": "2025-01-01",
                    "expectedEndDate": "2025-06-01",
                    "budget": 200000,
                    "managerId": 1
                }
                """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Projeto Teste"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/projects/{id} deve retornar 200")
    void shouldGetProjectById() throws Exception {
        when(projectService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/projects/{id} deve retornar 404 quando não encontrado")
    void shouldReturn404WhenNotFound() throws Exception {
        when(projectService.findById(99L)).thenThrow(new ResourceNotFoundException("Projeto", 99L));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("DELETE /api/projects/{id} deve retornar 204")
    void shouldDeleteProject() throws Exception {
        doNothing().when(projectService).delete(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("DELETE /api/projects/{id} deve retornar 422 para status não-deletável")
    void shouldReturn422WhenNotDeletable() throws Exception {
        doThrow(new BusinessRuleException("Projeto com status 'INICIADO' não pode ser excluído"))
                .when(projectService).delete(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("PATCH /api/projects/{id}/status deve retornar 422 para transição inválida")
    void shouldReturn422ForInvalidTransition() throws Exception {
        when(projectService.updateStatus(eq(1L), any()))
                .thenThrow(new InvalidStatusTransitionException(ProjectStatus.EM_ANALISE, ProjectStatus.INICIADO));

        String json = """
                { "status": "INICIADO" }
                """;

        mockMvc.perform(patch("/api/projects/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /api/projects deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("GET /api/projects deve retornar 200 com paginação")
    void shouldFindAll() throws Exception {
        ProjectResponseDTO response = buildResponse();
        Page<ProjectResponseDTO> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 1),
                1
        );

        when(projectService.findAll(isNull(), isNull(), any())).thenReturn(page);

        // sort em formato JSON-like: sort=["name"]
        mockMvc.perform(get("/api/projects?page=0&size=1&sort=%5B%22name%22%5D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("PUT /api/projects/{id} deve retornar 200")
    void shouldUpdateProject() throws Exception {
        when(projectService.update(eq(1L), any())).thenReturn(buildResponse());

        String json = """
                {
                    "name": "Projeto Teste",
                    "startDate": "2025-01-01",
                    "expectedEndDate": "2025-06-01",
                    "budget": 200000,
                    "description": "Descrição teste",
                    "managerId": 1
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Projeto Teste"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("POST /api/projects/{id}/members deve retornar 200 com lista de memberIds")
    void shouldAddMember() throws Exception {
        when(projectService.addMembers(eq(1L), eq(List.of(2L)))).thenReturn(buildResponse());

        String json = """
                { "memberIds": [2] }
                """;

        mockMvc.perform(post("/api/projects/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("DELETE /api/projects/{id}/members/{memberId} deve retornar 200")
    void shouldRemoveMember() throws Exception {
        when(projectService.removeMember(eq(1L), eq(2L))).thenReturn(buildResponse());

        mockMvc.perform(delete("/api/projects/1/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
