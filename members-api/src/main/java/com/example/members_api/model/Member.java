package com.example.members_api.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {

    private Long id;

    private String name;

    private String role;

}
