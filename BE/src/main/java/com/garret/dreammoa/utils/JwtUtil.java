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


    public String createAccessToken(Long userId, String email, String name, String nickname) {
        if (userId == null || email == null || name == null || nickname == null) {
            log.error("❌ [AT 발급 오류] 필수 정보가 null입니다. userId: {}, email: {}, name: {}, nickname: {}",
                    userId, email, name, nickname);
            throw new IllegalArgumentException("필수 정보가 null입니다.");
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        Map<String, Object> claims = Map.of(
                "userId", String.valueOf(userId),
                "name", name,
                "nickname", nickname
        );

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("✅ [AT 발급 완료] userId: {}, email: {}, AT: {}", userId, email, token);
        return token;
    }

    public String createRefreshToken(UserEntity user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        String refreshToken = Jwts.builder()
                .setSubject(user.getEmail()) // 이메일을 주제로 설정
                .setIssuedAt(now) // 발행 시간
                .setExpiration(validity) // 만료 시간
                .addClaims(Map.of(
                        "userId", String.valueOf(user.getId()),   // ✅ 기존 RT에는 userId만 있었음
                        "name", user.getName(),                  // ✅ name 추가
                        "nickname", user.getNickname()           // ✅ nickname 추가
                ))
                .signWith(key, SignatureAlgorithm.HS256) // 서명
                .compact();

        // Redis에 RT 저장
        redisTemplate.opsForValue().set(user.getId().toString(), refreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        log.info("✅ [RT 발급 완료] UserID: {}, Name: {}, Nickname: {}", user.getId(), user.getName(), user.getNickname());
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

    public boolean isRefreshTokenValid(Long userId, String refreshToken) {
        // userId를 String으로 변환하여 Redis에서 저장된 토큰을 가져옵니다.
        String storedToken = redisTemplate.opsForValue().get(userId.toString());
        return refreshToken.equals(storedToken); // 저장된 토큰과 비교
    }

    public long getAccessTokenExpirationTime() {
        return ACCESS_TOKEN_EXPIRE_TIME / 1000; // 초 단위 반환
    }

    public long getRefreshTokenExpirationTime() {
        return REFRESH_TOKEN_EXPIRE_TIME / 1000; // 초 단위 반환
    }

    public Long getUserIdFromToken(String token) {
        try {
            // 토큰에서 클레임 추출
            String userIdStr = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", String.class); // userId를 String 타입으로 추출

            // String을 Long으로 변환
            return Long.parseLong(userIdStr);
        } catch (JwtException | NumberFormatException e) {
            log.error("유효하지 않은 JWT 토큰 또는 userId 변환 실패", e);
            return null;
        }
    }
}
