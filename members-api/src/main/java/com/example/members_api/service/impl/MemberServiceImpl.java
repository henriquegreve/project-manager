package com.example.members_api.service.impl;

import com.example.members_api.dto.MemberRequestDTO;
import com.example.members_api.dto.MemberResponseDTO;
import com.example.members_api.exception.BusinessException;
import com.example.members_api.mapper.MemberMapper;
import com.example.members_api.model.Member;
import com.example.members_api.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final Map<Long, Member> database = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1);

    private final MemberMapper mapper;

    @PostConstruct
    void preloadMockMembers() {
        // Garante que o "endpoint externo mockado" retorne dados mesmo sem chamadas anteriores.
        if (!database.isEmpty()) {
            return;
        }

        // 30 funcionários + 5 gerentes: total 35 membros com IDs sequenciais.
        for (int i = 1; i <= 30; i++) {
            long id = counter.getAndIncrement();
            database.put(
                    id,
                    Member.builder()
                            .id(id)
                            .name("Funcionario " + i)
                            .role("funcionario")
                            .build()
            );
        }

        for (int i = 1; i <= 5; i++) {
            long id = counter.getAndIncrement();
            database.put(
                    id,
                    Member.builder()
                            .id(id)
                            .name("Gerente " + i)
                            .role("gerente")
                            .build()
            );
        }

        log.info("members-api mock iniciado com {} membros", database.size());
    }

    @Override
    public MemberResponseDTO create(MemberRequestDTO dto) {
        simulateDelay();
        log.info("Criando membro: {}", dto.getName());

        Member member = mapper.toEntity(dto);

        Long id = counter.getAndIncrement();
        member.setId(id);

        database.put(id, member);

        return mapper.toDTO(member);
    }

    @Override
    public Page<MemberResponseDTO> findAll(int page, int size, String role) {
        simulateDelay();
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        String normalizedRole = role == null ? null : role.trim().toLowerCase();

        List<Member> sorted = database.values()
                .stream()
                .filter(member -> {
                    if (normalizedRole == null || normalizedRole.isBlank()) {
                        return true;
                    }
                    return member.getRole() != null && member.getRole().equalsIgnoreCase(normalizedRole);
                })
                .sorted(Comparator
                        .comparingInt((Member m) -> "gerente".equalsIgnoreCase(m.getRole()) ? 0 : 1)
                        .thenComparingLong(Member::getId)
                )
                .toList();

        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, sorted.size());

        List<MemberResponseDTO> content = fromIndex >= sorted.size()
                ? List.of()
                : sorted.subList(fromIndex, toIndex)
                        .stream()
                        .map(mapper::toDTO)
                        .toList();

        return new PageImpl<>(content, PageRequest.of(safePage, safeSize), sorted.size());
    }

    @Override
    public MemberResponseDTO findById(Long id) {
        simulateDelay();

        Member member = Optional.ofNullable(database.get(id))
                .orElseThrow(() -> new BusinessException("Membro não encontrado com id: " + id));

        return mapper.toDTO(member);
    }

    private void simulateDelay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
