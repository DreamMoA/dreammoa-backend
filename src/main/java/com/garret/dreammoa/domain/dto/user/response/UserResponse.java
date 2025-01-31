package com.garret.dreammoa.domain.dto.user.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String email;
    private String name;
    private String nickname;
    private String profilePictureUrl;
    private String role;
}
