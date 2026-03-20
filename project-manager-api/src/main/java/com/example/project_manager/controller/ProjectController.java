package com.example.project_manager.controller;

import com.example.project_manager.dto.MemberIdsAssociationDTO;
import com.example.project_manager.dto.ProjectRequestDTO;
import com.example.project_manager.dto.ProjectResponseDTO;
import com.example.project_manager.dto.StatusUpdateDTO;
import com.example.project_manager.model.ProjectStatus;
import com.example.project_manager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "CRUD e gerenciamento de projetos do portfólio")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Criar projeto", description = "Cria um novo projeto no portfólio")
    @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso")
    public ResponseEntity<ProjectResponseDTO> create(@Valid @RequestBody ProjectRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(dto));
    }

    @GetMapping
    @Operation(summary = "Listar projetos", description = "Lista projetos com paginação e filtros opcionais por nome e status")
    public ResponseEntity<Page<ProjectResponseDTO>> findAll(
            @Parameter(description = "Filtrar por nome (parcial)") @RequestParam(required = false) String name,
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) ProjectStatus status,
            @Parameter(description = "Número da página (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página (1..N)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Ordenação. Suporta `sort=name` ou `sort=[\"name\"]` (ASC).") @RequestParam(required = false) String sort) {

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return ResponseEntity.ok(projectService.findAll(name, status, pageable));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }

        String s = sort.trim();

        if (s.startsWith("[") && s.endsWith("]")) {
            String inner = s.substring(1, s.length() - 1).trim();
            if (inner.isBlank()) {
                return Sort.unsorted();
            }
            inner = inner.replace("\"", "");
            String[] parts = inner.split(",");

            Sort sortResult = Sort.unsorted();
            for (String raw : parts) {
                String token = raw.trim();
                if (token.isEmpty()) continue;

                if (token.startsWith("-") && token.length() > 1) {
                    sortResult = sortResult.and(Sort.by(Sort.Direction.DESC, token.substring(1)));
                } else {
                    sortResult = sortResult.and(Sort.by(Sort.Direction.ASC, token));
                }
            }
            return sortResult;
        }

        if (s.startsWith("-") && s.length() > 1) {
            return Sort.by(Sort.Direction.DESC, s.substring(1));
        }

        String property = s;
        Sort.Direction direction = Sort.Direction.ASC;

        String[] commaParts = s.split(",", 2);
        if (commaParts.length == 2) {
            property = commaParts[0].trim();
            String dir = commaParts[1].trim();
            direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        } else {
            String[] colonParts = s.split(":", 2);
            if (colonParts.length == 2) {
                property = colonParts[0].trim();
                String dir = colonParts[1].trim();
                direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
            }
        }

        if (property.isBlank()) {
            throw new IllegalArgumentException("sort inválido");
        }
        return Sort.by(direction, property);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID")
    @ApiResponse(responseCode = "200", description = "Projeto encontrado")
    @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    public ResponseEntity<ProjectResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto", description = "Atualiza os dados de um projeto existente")
    public ResponseEntity<ProjectResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProjectRequestDTO dto) {
        return ResponseEntity.ok(projectService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir projeto", description = "Exclui um projeto (não permitido se status for INICIADO, EM_ANDAMENTO ou ENCERRADO)")
    @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso")
    @ApiResponse(responseCode = "422", description = "Projeto não pode ser excluído devido ao status atual")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status do projeto", description = "Altera o status respeitando a sequência lógica de transições")
    public ResponseEntity<ProjectResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        return ResponseEntity.ok(projectService.updateStatus(id, dto.getStatus()));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Associar membro ao projeto", description = "Apenas membros com atribuição 'funcionário' podem ser associados")
    @ApiResponse(responseCode = "200", description = "Membro associado com sucesso")
    @ApiResponse(responseCode = "422", description = "Violação de regra de negócio")
    public ResponseEntity<ProjectResponseDTO> addMembers(
            @PathVariable Long id,
            @Valid @RequestBody MemberIdsAssociationDTO dto
    ) {
        return ResponseEntity.ok(projectService.addMembers(id, dto.getMemberIds()));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Desassociar membro do projeto")
    public ResponseEntity<ProjectResponseDTO> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        return ResponseEntity.ok(projectService.removeMember(id, memberId));
    }
}
