package com.garret.dreammoa.domain.service.user;

import com.garret.dreammoa.domain.dto.dashboard.request.UpdateDeterminationRequest;
import com.garret.dreammoa.domain.dto.dashboard.response.DeterminationResponse;
import com.garret.dreammoa.domain.dto.user.request.JoinRequest;
import com.garret.dreammoa.domain.dto.user.request.UpdateProfileRequest;
import com.garret.dreammoa.domain.dto.user.response.UserResponse;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.file.FileService;
import com.garret.dreammoa.domain.service.mail.EmailService;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
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
        boolean verifyEmail = joinRequest.isVerifyEmail();

        if(!verifyEmail){
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

        // 사용자 엔티티 조회 (role 값을 가져오기 위함)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 사용자 ID로 프로필 URL 가져오기
//        fileRepository.findByRelatedIdAndRelatedType(userId, FileEntity.RelatedType.PROFILE)
        Optional<FileEntity> profilePicture = fileService.getByRelatedIdAndRelatedType(userId, FileEntity.RelatedType.PROFILE)
                .stream().findFirst();
        String profileUrl = profilePicture.map(FileEntity::getFileUrl).orElse(null);

        // id, email, name, nickname, profilePictureUrl, role 모두 포함하여 반환
        return new UserResponse(userId, email, name, nickname, profileUrl, user.getRole().name());
    }

    public String findByEmailByNicknameAndName(String nickname, String name) {
        UserEntity user = userRepository.findByNicknameAndName(nickname, name)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        return user.getEmail();
    }

    @Transactional
    public void deleteAccount(String accessToken, String inputPassword) {
        // Access Token 유효성 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new SecurityException("유효하지 않은 Access Token입니다.");
        }

        // 토큰에서 사용자 ID 추출
        Long userId = jwtUtil.getUserIdFromToken(accessToken);

        // 사용자 정보 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(inputPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 프로필 이미지 삭제
        Optional<FileEntity> profileImage = fileService.getProfilePicture(user.getId());
        profileImage.ifPresent(file -> fileService.deleteFile(file.getFileId()));

        // 사용자 데이터 삭제
        userRepository.delete(user);
    }


    @Transactional
    public void updateUserProfile(String accessToken, UpdateProfileRequest updateProfileRequest, MultipartFile profilePicture) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }

        String email = jwtUtil.getEmailFromToken(accessToken);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 체크
        String newNickname = updateProfileRequest.getNickname();
        if (!user.getNickname().equals(newNickname) && userRepository.existsByNickname(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 사용자 정보 업데이트
        user.setName(updateProfileRequest.getName());
        user.setNickname(newNickname);

        // 프로필 사진 업데이트 (모든 파일이 S3에 저장됨)
        if(Objects.nonNull(profilePicture)){
            try{
                //기본 PROFILE 타입 이 있으면 업데이트
                FileEntity newProfileImage = fileService.saveFile(profilePicture, user.getId(), FileEntity.RelatedType.PROFILE);
                user.setProfileImage(newProfileImage);
            }catch (Exception e){
                throw new RuntimeException("프로필 사진 업로드 중 오류가 발생했습니다 : " + e.getMessage());
            }
        }
        userRepository.save(user);
    }


    /**
     * 비밀번호 유효성 검사 메서드
     * @param password 새 비밀번호
     * @param email 사용자 이메일
     */
    public void validatePassword(String password, String email) {
        String emailLocalPart = email.split("@")[0].toLowerCase();
        String passwordLower = password.toLowerCase();

        // 비밀번호에 이메일 로컬 파트 포함 여부 검증
        if (passwordLower.contains(emailLocalPart)) {
            throw new RuntimeException("비밀번호에 이메일 이름이 포함될 수 없습니다.");
        }

        // 비밀번호 유효성 검사
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,16}$")) {
            throw new RuntimeException("비밀번호는 영어, 숫자, 특수문자를 모두 포함하여 8~16자여야 합니다.");
        }
    }

    public boolean checkPassword(String accessToken, String inputPassword) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }

        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return bCryptPasswordEncoder.matches(inputPassword, user.getPassword());
    }


    @Transactional
    public void updatePassword(String accessToken, String currentPassword, String newPassword, String confirmPassword) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Access Token입니다.");
        }

        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 비밀번호 검증
        if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 현재 비밀번호가 동일한지 검증
        if (bCryptPasswordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 다르게 설정해야 합니다.");
        }

        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검증
        validatePassword(newPassword, user.getEmail());

        // 비밀번호 변경
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserResponse getUserInfoFromDb(String accessToken) {
        // JWT 토큰 유효성 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }

        // 토큰에서 사용자 ID 추출
        Long userId = jwtUtil.getUserIdFromToken(accessToken);

        // DB에서 사용자 정보 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        // 프로필 이미지 URL 조회 (UserEntity 내의 profileImage 또는 별도 파일 테이블 조회)
        String profileUrl = null;
        if (user.getProfileImage() != null) {
            profileUrl = user.getProfileImage().getFileUrl();
        } else {
            Optional<FileEntity> profilePicture = fileRepository.findByRelatedIdAndRelatedType(userId, FileEntity.RelatedType.PROFILE)
                    .stream().findFirst();
            profileUrl = profilePicture.map(FileEntity::getFileUrl).orElse(null);
        }

        // DB의 사용자 정보를 기반으로 UserResponse DTO 생성 및 반환 (id와 role 추가)
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getNickname(), profileUrl, user.getRole().name());
    }

    // 사용자 각오 수정
    @Transactional
    public void updateDetermination(String accessToken, UpdateDeterminationRequest request) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setDetermination(request.getDetermination());
        userRepository.save(user);
    }

    // 사용자 각오 조회
    public DeterminationResponse getDetermination(String accessToken) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new DeterminationResponse(user.getDetermination());
    }
}
