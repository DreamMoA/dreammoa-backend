package com.garret.dreammoa.config.oauth;

import com.garret.dreammoa.config.FileProperties;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.utils.JwtUtil;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil; // JWT 생성 및 검증 클래스
    private final UserRepository userRepository; // 사용자 데이터베이스 관리
    private final FileRepository fileRepository;
    private final FileProperties fileProperties;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // 인증된 사용자 정보
        String email = (String) oAuth2User.getAttributes().get("email"); // Google 프로필에서 이메일 추출
        String name = (String) oAuth2User.getAttributes().get("name"); // Google 프로필에서 이름 추출
        String profileImageUrl = (String) oAuth2User.getAttributes().get("picture");
        // 사용자 확인 또는 새 사용자 생성
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);

        UserEntity user = userOptional.orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .name(name) // Google에서 가져온 이름 설정
                    .nickname(name) // 기본 닉네임은 이름과 동일하게 설정
                    .password("GooglePassWord123!")
                    .role(UserEntity.Role.Google) // 기본 역할 설정
                    .build();
            return userRepository.save(newUser);
        });

        // Google 프로필 이름 변경 시 데이터베이스 동기화
        if (!user.getName().equals(name)) {
            user.setName(name);
            userRepository.save(user);
        }
        // 프로필 이미지 처리
        if (profileImageUrl != null) {
            FileEntity profileImage = fileRepository.findByRelatedIdAndRelatedType(user.getId(), FileEntity.RelatedType.PROFILE)
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        // 고유 파일명 생성
                        String uniqueFileName = UUID.randomUUID().toString() + ".jpg";

                        // 파일 저장 경로
                        Path filePath = Paths.get(fileProperties.getUploadDir(), uniqueFileName);

                        try {
                            // 디렉토리 생성 (존재하지 않으면 생성)
                            Files.createDirectories(filePath.getParent());

                            // Google 프로필 이미지 다운로드 후 저장
                            byte[] imageBytes = new URL(profileImageUrl).openStream().readAllBytes();
                            Files.write(filePath, imageBytes);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to save profile image", e);
                        }

                        // 파일 URL 생성 (정적 경로 기준)
                        String fileUrl = fileProperties.getStaticLocations().replace("file:", "") + uniqueFileName;

                        // FileEntity 생성 및 저장
                        FileEntity newFile = FileEntity.builder()
                                .relatedId(user.getId())
                                .relatedType(FileEntity.RelatedType.PROFILE)
                                .fileName(uniqueFileName)
                                .filePath(filePath.toString())
                                .fileUrl(fileUrl)
                                .fileType("jpeg")
                                .build();
                        return fileRepository.save(newFile);
                    });

            // User와 프로필 이미지 연관
            user.setProfileImage(profileImage);
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getNickname());
        String refreshToken = jwtUtil.createRefreshToken(user); // Redis에 저장됨

        // 쿠키에 토큰 저장
        CookieUtil.addCookie(response, "access_token", accessToken, (int) jwtUtil.getAccessTokenExpirationTime());
        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken, (int) jwtUtil.getRefreshTokenExpirationTime());

        // 성공 메시지 전송
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.sendRedirect("http://localhost:5173/");
    }
}
