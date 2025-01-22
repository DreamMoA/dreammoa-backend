package com.garret.dreammoa.jwt;

import com.garret.dreammoa.dto.CustomUserDetails;
import com.garret.dreammoa.model.UserEntity;
import com.garret.dreammoa.utils.AuthorityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.User;
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
    private final TokenProvider tokenProvider;

    public LoginFilter(AuthenticationManager authenticationManager, TokenProvider tokenProvider){
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
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
                role
        );

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends  GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        
        // Access Token & Refresh Token 생성
        String accessToken = tokenProvider.createAccessToken(email, name, nickname);
        String refreshToken = tokenProvider.createRefreshToken(user);

        // Response 헤더에 토큰 추가
        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Refresh-Token", refreshToken);


    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }



}
