package com.garret.dreammoa.utils;

import com.garret.dreammoa.domain.model.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    // 현재 상수지만 나중에 변경해야함
    private static final String JWT_SECRET = "SECRET-TOO-LONG-FOR-DEMO-PLEASE-USE-REAL-KEY-----123456789";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 1주

    // JWT 서명 키
    private final Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    // Redis를 활용한 리프레시 토큰 저장 및 검증
    private final RedisTemplate<String, String> redisTemplate;


    public JwtUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public String createAccessToken(String email, String name, String nickname) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .addClaims(Map.of(
                        "name", name,
                        "nickname", nickname
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


     public String createRefreshToken(UserEntity user) {
         Date now = new Date();
         Date validity = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        String refreshToken = Jwts.builder()
                .setSubject(user.getEmail()) // 이메일을 토큰 주제로 설정
                .setIssuedAt(now) // 토큰 발행 시간
                .setExpiration(validity) // 토큰 만료 시간
                .addClaims(Map.of( // 사용자 ID를 추가로 저장
                        "userId", user.getId()
                ))
                .signWith(key, SignatureAlgorithm.HS256) // HMAC SHA-256으로 서명
                .compact();

        // Redis에 리프레시 토큰 저장 (키: 이메일, 값: 토큰)
        redisTemplate.opsForValue().set(user.getId().toString(), refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        return refreshToken;
    }


    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key) // 서명 키 설정
                    .build()
                    .parseClaimsJws(token) // 토큰 파싱
                    .getBody()
                    .getSubject(); // 주제(이메일) 반환
        } catch (JwtException e) {
            log.error("유효하지 않은 JWT 토큰", e);
            return null;
        }
    }


    // 토큰에서 이름 추출
    public String getNameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("name", String.class);
        } catch (JwtException e) {
            log.error("유효하지 않은 JWT 토큰", e);
            return null;
        }
    }

    // 토큰에서 닉네임 추출
    public String getNicknameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("nickname", String.class);
        } catch (JwtException e) {
            log.error("유효하지 않은 JWT 토큰", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key) // 서명 키 설정
                    .build()
                    .parseClaimsJws(token); // 토큰 검증
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 JWT 서명.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 클레임 문자열이 비어있습니다.");
        }
        return false;
    }

    public boolean isRefreshTokenValid(String email, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(email); // Redis에서 저장된 토큰 가져오기
        return refreshToken.equals(storedToken); // 저장된 토큰과 비교
    }

    public long getAccessTokenExpirationTime() {
        return ACCESS_TOKEN_EXPIRE_TIME / 1000; // 초 단위 반환
    }

    public long getRefreshTokenExpirationTime() {
        return REFRESH_TOKEN_EXPIRE_TIME / 1000; // 초 단위 반환
    }
}
