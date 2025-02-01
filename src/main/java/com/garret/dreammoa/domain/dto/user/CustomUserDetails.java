package com.garret.dreammoa.domain.dto.user;

import com.garret.dreammoa.domain.model.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final String nickname;
    private final String role;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLogin;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UserEntity user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.createdAt = user.getCreatedAt();
        this.lastLogin = user.getLastLogin();
        this.role = user.getRole().name();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    public static CustomUserDetails fromEntity(Optional<UserEntity> user) {
        return user.map(CustomUserDetails::new)
                .orElseThrow(() -> new IllegalArgumentException("UserEntity cannot be empty"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public String getUsername() {
        return email;
    }

    public Long getId() {
        return id;
    }

    // 계정 만료, 잠금, 자격 증명 만료, 활성화 상태에 대한 메서드
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    @Override
    public boolean isAccountNonLocked() {
        return true;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}