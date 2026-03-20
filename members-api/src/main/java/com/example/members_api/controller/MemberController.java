package com.example.members_api.controller;

import com.example.members_api.dto.MemberRequestDTO;
import com.example.members_api.dto.MemberResponseDTO;
import com.example.members_api.service.MemberService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
@Tag(name = "Membros (API externa mockada)", description = "API REST simulando sistema externo de membros")
public class MemberController {

    private final MemberService service;

    @PostMapping
    @Operation(summary = "Criar membro", description = "Cria um novo membro enviando nome e role (cargo/atribuição)")
    public ResponseEntity<MemberResponseDTO> create(
            @Valid @RequestBody MemberRequestDTO request
    ) {
        MemberResponseDTO response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar membros", description = "Lista membros com paginação via query params `page` e `size`")
    public ResponseEntity<Page<MemberResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(service.findAll(page, size, role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar membro por ID", description = "Retorna os dados do membro pelo ID")
    public ResponseEntity<MemberResponseDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }
}
