package com.example.project_manager.client.impl;

import com.example.project_manager.client.MembersApiClient;
import com.example.project_manager.dto.MemberResponseDTO;
import com.example.project_manager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MembersApiClientImpl implements MembersApiClient {

    private final RestTemplate restTemplate;

    @Value("${members.api.base-url:http://localhost:8081}")
    private String membersApiBaseUrl;

    @Override
    public MemberResponseDTO findById(Long id) {
        String url = membersApiBaseUrl + "/api/members/" + id;
        try {
            ResponseEntity<MemberResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    MemberResponseDTO.class
            );
            MemberResponseDTO body = response.getBody();
            if (body == null) {
                throw new IllegalStateException("members-api retornou body vazio ao buscar membro por id");
            }
            return body;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Membro", id);
        } catch (RestClientException ex) {
            throw new ResourceNotFoundException("Membro", id);
        }
    }
}
