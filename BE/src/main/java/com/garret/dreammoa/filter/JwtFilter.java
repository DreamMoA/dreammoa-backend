package com.garret.dreammoa.filter;

import com.garret.dreammoa.domain.service.CustomUserDetailsService;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);  // Logger 추가

    private final JwtUtil jwtUtil; // JWT 생성 및 검증 클래스
    private final CustomUserDetailsService userDetailsService; // 사용자 세부 정보 서비스

    private static final String HEADER_AUTHORIZATION = "Authorization"; // HTTP Authorization 헤더
    private static final String TOKEN_PREFIX = "Bearer "; // 토큰 접두사

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt)) {
            logger.info("🟢 [JWT 필터] 토큰 감지됨: {}", jwt);

            if (jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);
                logger.info("✅ [JWT 유효성 검사 통과] Email: {}", email);

                // 사용자 정보 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                logger.info("👤 [사용자 정보 로드 완료] Username: {}", userDetails.getUsername());

                // Spring Security 인증 설정
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("🔐 [Security Context 설정 완료] 사용자 인증 성공");
            } else {
                logger.warn("❌ [JWT 유효성 검사 실패] 잘못된 또는 만료된 토큰");
            }
        } else {
            logger.warn("⚠️ [JWT 미검출] 요청에 유효한 토큰이 없습니다.");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            logger.info("📥 [JWT 추출] Authorization 헤더에서 토큰 발견");
            return bearerToken.substring(TOKEN_PREFIX.length());
        }

        logger.warn("⚠️ [JWT 추출 실패] Authorization 헤더 및 쿠키에서 토큰을 찾을 수 없음");
        return null;
    }
}
