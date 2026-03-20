package com.example.members_api.service;

import com.example.members_api.dto.MemberRequestDTO;
import com.example.members_api.dto.MemberResponseDTO;

import org.springframework.data.domain.Page;

public interface MemberService {
    MemberResponseDTO create(MemberRequestDTO dto);
    Page<MemberResponseDTO> findAll(int page, int size, String role);
    MemberResponseDTO findById(Long id);
}
