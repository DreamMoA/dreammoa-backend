package com.garret.dreammoa.domain.controller.user;

import com.garret.dreammoa.domain.dto.common.ErrorResponse;
import com.garret.dreammoa.domain.dto.common.SuccessResponse;
import com.garret.dreammoa.domain.dto.user.request.*;
import com.garret.dreammoa.domain.dto.user.response.EmailCheckResponse;
import com.garret.dreammoa.domain.dto.user.response.NicknameCheckResponse;
import com.garret.dreammoa.domain.dto.user.response.ProfilePictureResponse;
import com.garret.dreammoa.domain.service.EmailService;
import com.garret.dreammoa.domain.dto.user.response.UserResponse;
import com.garret.dreammoa.domain.service.UserService;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.CookieStore;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;


    @PostMapping("/join")
    public ResponseEntity<?> joinProcess(@Valid @RequestBody JoinRequest joinRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(errorMessage));
        }

        userService.joinProcess(joinRequest);
        return ResponseEntity.ok(new SuccessResponse("회원가입이 완료되었습니다."));
    }

    /**
     * 닉네임 중복 확인 엔드포인트
     *
     * @param request 닉네임 확인 요청
     * @param bindingResult 검증 결과
     * @return 닉네임 사용 가능 여부
     */
    @PostMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@Valid @RequestBody CheckNicknameRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(errorMessage));
        }

        try {
            boolean available = userService.isNicknameAvailable(request.getNickname());
            return ResponseEntity.ok(new NicknameCheckResponse(available));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * 이메일 중복 확인 엔드포인트
     *
     * @param request 이메일 중복 확인 요청
     * @param bindingResult 검증 결과
     * @return 이메일 사용 가능 여부
     */
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@Valid @RequestBody CheckEmailRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(errorMessage));
        }

        try {
            boolean available = userService.isEmailAvailable(request.getEmail());
            return ResponseEntity.ok(new EmailCheckResponse(available));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(errorMessage));
        }

        try {
            emailService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok(new SuccessResponse("인증 코드가 이메일로 전송되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        String accessToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body("Access Token이 없습니다.");
        }

        try {
            // 2. Service를 통해 UserResponse DTO 추출
            UserResponse userInfo = userService.extractUserInfo(accessToken);

            // 3. 유저 사진 반환
            return ResponseEntity.ok(new ProfilePictureResponse(userInfo.getProfilePictureUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    } // 이거 없으면 null 처리하도록 설정해야됨

    @PostMapping("/verify-email-code")
    public ResponseEntity<?> verifyEmailCode(@Valid @RequestBody VerifyCodeRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(errorMessage));
        }

        try {
            boolean isValid = userService.verifyEmailCode(request.getEmail(), request.getCode());
            if (isValid) {
                return ResponseEntity.ok(new SuccessResponse("인증 코드가 일치합니다."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("인증 코드가 일치하지 않습니다."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }


    @PostMapping("/userInfo")
    public ResponseEntity<?> userInfo(HttpServletRequest request) {
        // 1. 쿠키에서 accessToken 가져오기
        String accessToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body("Access Token이 없습니다.");
        }

        try {
            // 2. Service를 통해 UserResponse DTO 추출
            UserResponse userInfo = userService.extractUserInfo(accessToken);

            // 3. 유저 정보 반환
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/emailFind")
    public ResponseEntity<?> emailFind(@RequestBody EmailFindRequest request) {
        try {
            String email = userService.findByEmailByNicknameAndName(request.getNickname(), request.getName());
            return ResponseEntity.ok(email);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일 인증 코드 발송
    @PostMapping("/pwFind")
    public ResponseEntity<?> pwFind(@Valid @RequestBody SendVerificationCodeRequest request) {
        String email = request.getEmail();

        try {
            emailService.sendVerificationCode(email);
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이메일 인증 전송 실패, 다시 시도해주세요.");
        }
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");


        boolean isVerified = emailService.verifyCode(email, code);
        if (email == null || code == null || email.isEmpty() || code.isEmpty()) {
            return ResponseEntity.badRequest().body("이메일과 인증 코드를 모두 입력해주세요.");
        }

        if (isVerified) {
            return ResponseEntity.ok("인증 성공! 비밀번호 재설정 페이지로 이동하세요.");
        } else {
            return ResponseEntity.badRequest().body("인증 코드가 유효하지 않습니다. 다시 시도해주세요.");
        }
    }

    @PostMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request ) {
        String accessToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body("Access Token이 없습니다.");
        }

        try {
            // Authorization 헤더에서 accessToken을 추출하고 탈퇴 처리 요청
            userService.deleteAccount(accessToken, loginRequest.getPassword());
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 또는 기타 잘못된 입력 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            // 인증 또는 권한 문제 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 유효하지 않습니다.");
        } catch (Exception e) {
            // 서버 내부 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestPart("profileData") @Valid UpdateProfileRequest updateProfileRequest,
            BindingResult bindingResult,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            HttpServletRequest request) {

        // 유효성 검증 결과 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // Access Token 확인
        String accessToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        System.out.println("accessToken = " + accessToken);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body("Access Token이 없습니다.");
        }

        try {
            userService.updateUserProfile(accessToken, updateProfileRequest, profilePicture);
            return ResponseEntity.ok(new SuccessResponse("회원 정보가 성공적으로 수정되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }

    }
        @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(@Valid @RequestBody CheckPasswordRequest request,
                                           HttpServletRequest httpRequest) {
        // Access Token 확인
        String accessToken = Arrays.stream(httpRequest.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Access Token이 없습니다."));
        }

        try {
            userService.validatePassword(request.getPassword(),jwtUtil.getEmailFromToken(accessToken));
            //@ 있는지 벨리데이션 함 해줌
            boolean isMatch = userService.checkPassword(accessToken, request.getPassword());
            if (isMatch) {
                return ResponseEntity.ok(new SuccessResponse("비밀번호가 일치합니다."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("비밀번호가 일치하지 않습니다."));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request,
                                            HttpServletRequest httpRequest) {
        String accessToken = Arrays.stream(httpRequest.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Access Token이 없습니다."));
        }

        try {
            userService.updatePassword(accessToken, request.getCurrentPassword(), request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.ok(new SuccessResponse("비밀번호가 성공적으로 변경되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }


}
