package com.garret.dreammoa.filter;

import com.garret.dreammoa.domain.dto.user.CustomUserDetails;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.utils.AuthorityUtils;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        System.out.println(username);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication){

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = customUserDetails.getEmail();
        String name = customUserDetails.getName();
        String nickname = customUserDetails.getNickname();
        Long userId = customUserDetails.getId();
        UserEntity.Role role = AuthorityUtils.extractRoleFromAuthorities(customUserDetails.getAuthorities());

        UserEntity user = new UserEntity(
                customUserDetails.getId(),
                customUserDetails.getName(),
                customUserDetails.getNickname(),
                customUserDetails.getEmail(),
                customUserDetails.getPassword(),
                customUserDetails.getCreatedAt(),
                customUserDetails.getLastLogin(),

                role,
                null
        );

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends  GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        
        // Access Token & Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(userId, email, name, nickname, String.valueOf(role));
        String refreshToken = jwtUtil.createRefreshToken(user);

        // Response 헤더에 토큰 추가
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Refresh-Token", refreshToken);


    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }



}
