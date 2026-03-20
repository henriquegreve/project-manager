package com.example.project_manager.client;

import com.example.project_manager.dto.MemberResponseDTO;

public interface MembersApiClient {

    MemberResponseDTO findById(Long id);
}
