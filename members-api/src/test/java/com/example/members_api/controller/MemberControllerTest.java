package com.example.members_api.controller;

import com.example.members_api.dto.MemberRequestDTO;
import com.example.members_api.dto.MemberResponseDTO;
import com.example.members_api.exception.BusinessException;
import com.example.members_api.exception.GlobalExceptionHandler;
import com.example.members_api.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@Import(GlobalExceptionHandler.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService service;

    @Test
    @DisplayName("POST /api/members deve retornar 201")
    void shouldCreateMember() throws Exception {
        MemberResponseDTO response = MemberResponseDTO.builder()
                .id(1L)
                .name("João")
                .role("funcionario")
                .build();

        when(service.create(any())).thenReturn(response);

        String json = """
                { "name": "João", "role": "funcionario" }
                """;

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("João"));
    }

    @Test
    @DisplayName("GET /api/members/{id} deve retornar 200")
    void shouldFindById() throws Exception {
        MemberResponseDTO response = MemberResponseDTO.builder()
                .id(1L)
                .name("João")
                .role("funcionario")
                .build();

        when(service.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/members/{id} deve retornar 404 quando não encontrado")
    void shouldReturn404WhenNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new BusinessException("Membro não encontrado com id: 99"));

        mockMvc.perform(get("/api/members/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/members com body inválido deve retornar 400")
    void shouldReturn400ForInvalidBody() throws Exception {
        MemberRequestDTO invalid = MemberRequestDTO.builder()
                .name("")
                .role("")
                .build();

        // O binding/validação é tratado pelo Spring; aqui só garantimos o status correto.
        String json = """
                { "name": "", "role": "" }
                """;

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/members deve retornar 200 com paginação")
    void shouldFindAllWithPagination() throws Exception {
        MemberResponseDTO response = MemberResponseDTO.builder()
                .id(1L)
                .name("João")
                .role("funcionario")
                .build();

        PageRequest pageable = PageRequest.of(0, 1);
        Page<MemberResponseDTO> page = new PageImpl<>(
                java.util.List.of(response),
                pageable,
                1
        );

        when(service.findAll(0, 1, null)).thenReturn(page);

        mockMvc.perform(get("/api/members?page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }
}

