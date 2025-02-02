package com.garret.dreammoa.config.oauth;

import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuth2UserCustomService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    private static final String UPLOAD_DIR = "C:/SSAFY/uploads/profile/"; // 프로필 이미지 저장 폴더

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // 로그인 제공자 (google/naver/kakao) 가져오기
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes()); // attributes 복사

        UserEntity user;
        if ("naver".equalsIgnoreCase(registrationId)) {
            user = handleNaverLogin(attributes);
        } else if ("kakao".equalsIgnoreCase(registrationId)) {
            user = handleKakaoLogin(attributes);
        } else { // Google 로그인 처리
            user = handleGoogleLogin(attributes);
        }

        // OAuth2User 객체 생성하여 반환 (Spring Security 인증용)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes, // email이 확실히 포함된 attributes 전달
                "email"
        );
    }

    /**
     * ⭐ 네이버 로그인 처리 (response 내부 데이터 파싱)
     */
    private UserEntity handleNaverLogin(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response"); // 네이버는 'response' 내부에 정보가 있음

        String id = (String) response.get("id"); // 네이버 고유 ID
        String email = response.get("email") != null ? (String) response.get("email") : id + "@naver.com"; // 이메일이 없으면 ID로 대체
        String nickname = response.get("nickname") != null ? (String) response.get("nickname") : email; // 닉네임 없으면 이메일
        String name = response.get("name") != null ? (String) response.get("name") : nickname; // 이름 없으면 닉네임
        String profileImageUrl = (String) response.get("profile_image"); // 프로필 이미지 URL

        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("nickname", nickname);

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .email(email)
                            .name(name)
                            .nickname(nickname)
                            .password("NaverPassWord123!")
                            .role(UserEntity.Role.Naver)
                            .build();
                    return userRepository.save(newUser);
                });

        saveProfileImage(user, profileImageUrl);
        return user;
    }

    /**
     * ⭐ 카카오 로그인 처리 (kakao_account 내부 데이터 파싱)
     */
    private UserEntity handleKakaoLogin(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account"); // 카카오 계정 정보
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile"); // 프로필 정보

        String id = attributes.get("id").toString(); // 카카오 고유 ID
        String email = kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : id + "@kakao.com"; // 이메일 없으면 ID로 대체
        String nickname = profile.get("nickname") != null ? profile.get("nickname").toString() : email; // 닉네임 없으면 이메일
        String profileImageUrl = profile.get("profile_image_url") != null ? profile.get("profile_image_url").toString() : null; // 프로필 이미지 URL

        attributes.put("email", email);
        attributes.put("name", nickname); // 카카오는 이름이 없으므로 닉네임과 동일하게 설정
        attributes.put("nickname", nickname);

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .email(email)
                            .name(nickname) // 이름 = 닉네임
                            .nickname(nickname)
                            .password("KakaoPassWord123!")
                            .role(UserEntity.Role.Kakao)
                            .build();
                    return userRepository.save(newUser);
                });

        saveProfileImage(user, profileImageUrl);
        return user;
    }

    /**
     * ⭐ 구글 로그인 처리 (일반 attributes에서 가져옴)
     */
    private UserEntity handleGoogleLogin(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = attributes.get("name") != null ? (String) attributes.get("name") : email;
        String nickname = attributes.get("nickname") != null ? (String) attributes.get("nickname") : name;
        String profileImageUrl = (String) attributes.get("picture");

        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("nickname", nickname);

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .email(email)
                            .name(name)
                            .nickname(nickname)
                            .password("GooglePassWord123!")
                            .role(UserEntity.Role.Google)
                            .build();
                    return userRepository.save(newUser);
                });

        saveProfileImage(user, profileImageUrl);
        return user;
    }

    /**
     * ⭐ 프로필 이미지 저장 및 업데이트 로직
     */
    private void saveProfileImage(UserEntity user, String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            try {
                Optional<FileEntity> existingProfile = fileRepository.findByRelatedIdAndRelatedType(user.getId(), FileEntity.RelatedType.PROFILE)
                        .stream()
                        .findFirst();

                String uniqueFileName = UUID.randomUUID().toString() + ".jpg";
                Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

                Files.createDirectories(filePath.getParent());

                byte[] imageBytes = new URL(profileImageUrl).openStream().readAllBytes();
                Files.write(filePath, imageBytes);

                String fileUrl = "/uploads/profile/" + uniqueFileName;

                if (existingProfile.isPresent()) {
                    FileEntity profileImage = existingProfile.get();
                    profileImage.setFileName(uniqueFileName);
                    profileImage.setFilePath(filePath.toString());
                    profileImage.setFileUrl(fileUrl);
                    fileRepository.save(profileImage);
                } else {
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
