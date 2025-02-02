package com.garret.dreammoa.config.oauth;

import com.garret.dreammoa.config.FileProperties;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.utils.JwtUtil;
import com.garret.dreammoa.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    private static final String UPLOAD_DIR = "C:/SSAFY/uploads/profile/"; // 프로필 이미지 저장 폴더

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // 인증된 사용자 정보

        // OAuth2UserRequest에서 registrationId 가져오기 (OAuth2 로그인 제공자 확인)
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        if (registrationId == null) {
            throw new IllegalArgumentException("Missing registrationId in OAuth2 login");
        }

        // 네이버, 구글, 카카오 로그인 정보 처리
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // 사용자 확인 또는 새 사용자 생성
        Optional<UserEntity> userOptional = userRepository.findByEmail(userInfo.getEmail());
        UserEntity user = userOptional.orElseGet(() -> {
            UserEntity newUser = UserEntity.builder()
                    .email(userInfo.getEmail())
                    .name(userInfo.getName())
                    .nickname(userInfo.getName()) // 닉네임 없을 경우 이름과 동일하게 설정
                    .password(getDefaultPassword(registrationId)) // 소셜 로그인별 기본 비밀번호 설정
                    .role(getUserRole(registrationId)) // 소셜 로그인별 역할 설정
                    .build();
            return userRepository.save(newUser);
        });

        // 사용자 정보 업데이트 (이름 변경 시 반영)
        if (!user.getName().equals(userInfo.getName())) {
            user.setName(userInfo.getName());
            userRepository.save(user);
        }

        // ⭐ 프로필 이미지 저장
        saveProfileImage(user, userInfo.getProfileImageUrl());

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getName(), user.getNickname(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user); // Redis에 저장됨

        // 쿠키에 토큰 저장
        CookieUtil.addCookie(response, "access_token", accessToken, (int) jwtUtil.getAccessTokenExpirationTime());
        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken, (int) jwtUtil.getRefreshTokenExpirationTime());

        // 프론트엔드 URL로 리다이렉트 (localhost:5173)
        response.sendRedirect("http://localhost:5173/");
    }


    /**
     * 소셜 로그인별 기본 비밀번호 설정
     */
    private String getDefaultPassword(String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "naver":
                return "NaverPassWord123!";
            case "google":
                return "GooglePassWord123!";
            case "kakao":
                return "KakaoPassWord123!";
            default:
                return "SocialLoginPassWord123!";
        }
    }

    /**
     * 소셜 로그인별 역할 설정
     */
    private UserEntity.Role getUserRole(String registrationId) {
        switch (registrationId.toLowerCase()) {
            case "naver":
                return UserEntity.Role.Naver;
            case "google":
                return UserEntity.Role.Google;
            case "kakao":
                return UserEntity.Role.Kakao;
            default:
                return UserEntity.Role.USER;
        }
    }

    /**
     * 프로필 이미지 저장 및 업데이트 로직
     */
    private void saveProfileImage(UserEntity user, String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            try {
                // 기존 프로필 이미지 확인 (이미 있으면 업데이트)
                Optional<FileEntity> existingProfile = fileRepository.findByRelatedIdAndRelatedType(user.getId(), FileEntity.RelatedType.PROFILE)
                        .stream()
                        .findFirst();

                // 새 파일명 생성
                String uniqueFileName = UUID.randomUUID().toString() + ".jpg";
                Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

                // 디렉토리 생성 (없으면 생성)
                Files.createDirectories(filePath.getParent());

                // 프로필 이미지 다운로드 및 저장
                byte[] imageBytes = new URL(profileImageUrl).openStream().readAllBytes();
                Files.write(filePath, imageBytes);

                // 파일 URL 생성 (정적 리소스 접근 가능하도록 변경)
                String fileUrl = "/uploads/profile/" + uniqueFileName;

                if (existingProfile.isPresent()) {
                    // 기존 파일 정보 업데이트
                    FileEntity profileImage = existingProfile.get();
                    profileImage.setFileName(uniqueFileName);
                    profileImage.setFilePath(filePath.toString());
                    profileImage.setFileUrl(fileUrl);
                    fileRepository.save(profileImage);
                } else {
                    // 새로운 프로필 이미지 저장
                    FileEntity newFile = FileEntity.builder()
                            .relatedId(user.getId())
                            .relatedType(FileEntity.RelatedType.PROFILE)
                            .fileName(uniqueFileName)
                            .filePath(filePath.toString())
                            .fileUrl(fileUrl)
                            .fileType("jpeg")
                            .build();
                    fileRepository.save(newFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to save profile image", e);
            }
        }
    }
}
