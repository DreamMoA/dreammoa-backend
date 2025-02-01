package com.garret.dreammoa.domain.controller.user;


import com.garret.dreammoa.domain.dto.user.CustomUserDetails;
import com.garret.dreammoa.domain.dto.user.response.TokenResponse;
import com.garret.dreammoa.domain.dto.user.request.LoginRequest;
import com.garret.dreammoa.domain.service.UserService;
import com.garret.dreammoa.utils.JwtUtil;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.FileService;
import com.garret.dreammoa.utils.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,  BindingResult  bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors); // 400 에러와 함께 에러 메시지 반환
        }

        // 이메일과 비밀번호로 인증 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 인증 수행
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 인증 성공 시 JWT 생성
        String accessToken = jwtUtil.createAccessToken(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getName(),
                userDetails.getNickname(),
                userDetails.getRole()
        );
        String refreshToken = jwtUtil.createRefreshToken(userEntity);
        Long userId = userDetails.getId();
        Optional<FileEntity> profilePicture = fileService.getProfilePicture(userId);
        String profilePictureUrl = profilePicture.map(FileEntity::getFileUrl).orElse(null);

        userService.updateLastLogin(userId);

        // 쿠키에 토큰 저장
        CookieUtil.addHttpOnlyCookie(response, "access_token", accessToken, (int) jwtUtil.getAccessTokenExpirationTime());
        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken, (int) jwtUtil.getRefreshTokenExpirationTime());

        // 응답 본문으로도 토큰 반환
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // 리프레시 토큰 쿠키에서 추출
        String refreshToken = extractTokenFromCookie(request, "refresh_token");

        // 리프레시 토큰 검증 및 액세스 토큰 갱신
        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            Long userId = Long.valueOf(jwtUtil.getUserIdFromToken(refreshToken));
            String email = jwtUtil.getEmailFromToken(refreshToken);
            String name = jwtUtil.getNameFromToken(refreshToken);
            String nickname = jwtUtil.getNicknameFromToken(refreshToken);
            String role = jwtUtil.getRoleFromToken(refreshToken);

            if (jwtUtil.isRefreshTokenValid(userId, refreshToken)) {
                // 새로운 액세스 토큰 생성
                String newAccessToken = jwtUtil.createAccessToken(userId, email, name, nickname, role);

                // 쿠키에 저장
                CookieUtil.addHttpOnlyCookie(response, "access_token", newAccessToken, (int) jwtUtil.getAccessTokenExpirationTime());
                return ResponseEntity.ok(new TokenResponse(newAccessToken, null));
            }
        }
        return ResponseEntity.status(401).body("Invalid refresh token");
    }


    @PostMapping("/user-logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 리프레시 토큰 쿠키에서 추출
        String refreshToken = extractTokenFromCookie(request, "refresh_token");

        if (refreshToken != null) {
            String email = jwtUtil.getEmailFromToken(refreshToken);
            // Redis에서 리프레시 토큰 제거
            redisTemplate.delete(email); // Redis에서 해당 토큰 제거

            // 쿠키 삭제
            CookieUtil.deleteCookie(request, response, "access_token");
            CookieUtil.deleteCookie(request, response, "refresh_token");
        }

        return ResponseEntity.ok("Successfully logged out");
    }

    // 쿠키에서 특정 이름의 토큰 값을 추출
    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
