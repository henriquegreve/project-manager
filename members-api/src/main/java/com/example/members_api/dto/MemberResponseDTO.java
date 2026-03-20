package com.example.members_api.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResponseDTO {

    private Long id;

    private String name;

    private String role;

}
