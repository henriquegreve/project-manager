package com.example.members_api.mapper;

import com.example.members_api.dto.MemberRequestDTO;
import com.example.members_api.dto.MemberResponseDTO;
import com.example.members_api.model.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequestDTO dto) {
        return Member.builder()
                .name(dto.getName())
                .role(dto.getRole())
                .build();
    }

    public MemberResponseDTO toDTO(Member member) {
        return MemberResponseDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .role(member.getRole())
                .build();
    }

}
