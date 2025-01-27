package com.garret.dreammoa.domain.service;

import com.garret.dreammoa.domain.dto.user.request.JoinRequest;
import com.garret.dreammoa.domain.dto.user.response.UserResponse;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final FileService fileService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final FileRepository fileRepository;


    // 여기서 초기화

    @Transactional
    public void updateLastLogin(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void joinProcess(JoinRequest joinRequest){
        String email = joinRequest.getEmail();
        String password = joinRequest.getPassword();
        String name = joinRequest.getName();
        String nickname = joinRequest.getNickname();
        boolean verityEmail = joinRequest.isVerifyEmail();

        if(!verityEmail){
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        // 이메일 중복 체크
        if(userRepository.existsByEmail(email)){
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 닉네임 중복 체크
        if(userRepository.existsByNickname(nickname)){
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        // 비밀번호에 이메일 로컬 파트 포함 여부 검증
        String emailLocalPart = email.split("@")[0].toLowerCase();
        String passwordLower = password.toLowerCase();
        if(passwordLower.contains(emailLocalPart)){
            throw new RuntimeException("비밀번호에 이메일 이름이 포함될 수 없습니다.");
        }

        // 사용자 엔티티 생성
        UserEntity user = UserEntity.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .name(name)
                .nickname(nickname)
                .role(UserEntity.Role.USER) // 기본 역할 USER
                .build();

        userRepository.save(user);
    }

    /**
     * 이메일 중복 여부를 확인하는 메서드
     *
     * @param email 사용자 이메일
     * @return 이메일이 사용 가능하면 true, 아니면 false
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 이메일 인증 코드 검증 메서드
     * @param email 사용자 이메일
     * @param inputCode 사용자가 입력한 인증 코드
     * @return 인증 코드가 일치하면 true, 아니면 false
     */
    public boolean verifyEmailCode(String email, String inputCode) {
        return emailService.verifyCode(email, inputCode);
    }

    /**
     * 닉네임 중복 여부를 확인하는 메서드
     *
     * @param nickname 사용자 닉네임
     * @return 닉네임이 사용 가능하면 true, 아니면 false
     */
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }



    public UserResponse extractUserInfo(String accessToken) {
        // JWT 토큰 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }

        // 토큰에서 유저 정보 추출
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        String email = jwtUtil.getEmailFromToken(accessToken);
        String name = jwtUtil.getNameFromToken(accessToken);
        String nickname = jwtUtil.getNicknameFromToken(accessToken);

        if (email == null || name == null || nickname == null || userId == null) {
            throw new RuntimeException("토큰에서 유저 정보를 가져올 수 없습니다.");
        }

        // 사용자 ID로 프로필 URL 가져오기
        Optional<FileEntity> profilePicture = fileRepository.findByRelatedIdAndRelatedType(userId, FileEntity.RelatedType.PROFILE)
                .stream().findFirst();
        String profileUrl = profilePicture.map(FileEntity::getFileUrl).orElse(null);

        // 유저 정보 반환
        return new UserResponse(email, name, nickname, profileUrl);
    }

    public String findByEmailByNicknameAndName(String nickname, String name) {
        UserEntity user = userRepository.findByNicknameAndName(nickname, name)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        return user.getEmail();
    }
}
