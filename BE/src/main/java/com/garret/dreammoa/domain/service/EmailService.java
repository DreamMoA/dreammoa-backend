package com.garret.dreammoa.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;

    private static final String AUTH_CODE_PREFIX = "AuthCode_";


    /**
     * 인증 코드를 생성하고 Redis에 저장 후 이메일로 전송하는 메서드
     *
     * @param email 사용자 이메일
     */
    public void sendVerificationCode(String email) {
        String verificationCode = generateVerificationCode();

        // Redis에 인증 코드 저장 (30분 동안 유효)
        redisTemplate.opsForValue().set(AUTH_CODE_PREFIX + email, verificationCode, Duration.ofMinutes(30));
        log.info("Redis에 인증 코드 저장: key={}, value={}, 만료시간=30분", AUTH_CODE_PREFIX + email, verificationCode);

        // 이메일 제목
        String title = "DreamMoa 이메일 인증 코드";

        // 이메일 내용 (HTML 형식)
        String htmlContent = """
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>DreamMoa 이메일 인증 코드</title>
  <style type="text/css">
    html, body {
      margin: 0;
      padding: 0;
      background-color: #F9F9F9;
      font-family: Arial, sans-serif;
    }
    table {
      border-collapse: collapse;
    }
    /* 이메일 컨테이너에 상단 여백 추가 */
    .email-container {
      max-width: 600px;
      margin: 50px auto; /* 위, 아래 50px 여백 */
    }
    .header {
      background-color: #252F3D;
      color: #ffffff;
      text-align: center;
      padding: 20px;
    }
    .header h1 {
      margin: 0;
      font-size: 24px;
    }
    .header p {
      margin: 5px 0 0 0;
      font-size: 14px;
    }
    .body-content {
      background-color: #ffffff;
      padding: 25px 35px;
      text-align: center;
    }
    .content-text {
      font-size: 14px;
      line-height: 1.4;
      color: #444444;
      margin-bottom: 15px;
    }
    .verification-code {
      font-size: 36px;
      font-weight: bold;
      color: #000000;
      margin: 20px 0;
    }
    .code-description {
      font-size: 10px;
      color: #444444;
      margin-bottom: 15px;
    }
    a.button {
      display: inline-block;
      background-color: #252F3D;
      color: #ffffff;
      padding: 10px 20px;
      text-decoration: none;
      border-radius: 4px;
      margin: 20px 0;
    }
    .footer {
      background-color: #F9F9F9;
      color: #777777;
      text-align: center;
      font-size: 12px;
      padding: 20px 30px;
    }
    .footer a {
      color: #777777;
      text-decoration: underline;
    }
  </style>
</head>
<body>
  <!-- 최상단에 추가 여백을 위한 outer table -->
  <table width="100%" bgcolor="#F9F9F9" cellpadding="0" cellspacing="0">
    <tr>
      <td style="padding-top: 20px;"> <!-- 상단에 추가 padding -->
        <table class="email-container" align="center" cellpadding="0" cellspacing="0">
          <!-- Header -->
          
          <tr>
            <td class="header" style="background-color: #252F3D; padding: 20px 0 10px 0; text-align: center;">
               <img src="https://dream-moa.s3.ap-northeast-2.amazonaws.com/dreammoalogo.png" width="75" height="45" alt="DreamMoa Logo" border="0" style="font-family: sans-serif; font-size: 15px; line-height: 140%; color: #555555;">
              <br>
              <strong>소중한 꿈을 모아</strong>
            </td>
          </tr>
          <!-- Body -->
          <tr>
            <td class="body-content">
              <p class="content-text">안녕하세요,</p>
              <p class="content-text">DreamMoa를 이용해주셔서 감사합니다.</p>
              <p class="content-text">아래 인증 코드를 입력하여 이메일 인증을 완료해주세요.</p>
              <div class="verification-code">""" + verificationCode + """
              </div>
              <p class="code-description">(이 코드는 전송 후 30분 동안 유효합니다.)</p>
              <p class="content-text">DreamMoa와 함께 소중한 꿈을 이루세요!</p>
            </td>
          </tr>
          <!-- Footer -->
          <tr>
            <td class="footer">
              <p>본 이메일은 발신 전용입니다.</p>
              <p>문의가 필요하시면
                <a href="mailto:eunspear@gmail.com">support@dreammoa.com</a>으로 연락주세요.
              </p>
              <p>DreamMoa</p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>
""";
        // 이메일 전송
        mailService.sendEmail(email, title, htmlContent);
        log.info("인증 코드 이메일 전송: to={}, subject={}", email, title);
    }


    /**
     * 인증 코드를 검증하는 메서드
     *
     * @param email 사용자 이메일
     * @param code  입력된 인증 코드
     * @return 인증 성공 여부
     */
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(AUTH_CODE_PREFIX + email);
        if (storedCode != null && storedCode.equals(code)) {
            // 인증 성공 시 Redis에서 인증 코드 삭제
            redisTemplate.delete(AUTH_CODE_PREFIX + email);
            log.info("인증 코드 검증 성공: email={}, code={}", email, code); // 추가
            return true;
        }
        log.warn("인증 코드 검증 실패: email={}, 입력된 코드={}, 저장된 코드={}", email, code, storedCode); // 추가
        return false;
    }

    /**
     * 랜덤 6자리 인증 코드를 생성하는 메서드
     *
     * @return 생성된 인증 코드
     */
    private String generateVerificationCode() {
        String code = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 6);
        log.debug("생성된 인증 코드: {}", code); // 추가
        return code;
    }
}
