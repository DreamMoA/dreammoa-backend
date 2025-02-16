package com.garret.dreammoa.domain.service.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;

    /**
     * 이메일을 전송하는 메서드
     *
     * @param toEmail 수신자 이메일 주소
     * @param title   이메일 제목
     * @param text    이메일 내용
     */
    public void sendEmail(String toEmail, String title, String text) {
        MimeMessage message = emailSender.createMimeMessage();
        try {

            // MimeMessageHelper를 사용하여 HTML 콘텐츠 설정
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(text, true); // 두 번째 파라미터 `true`로 HTML 지원

            emailSender.send(message);
            log.info("이메일 전송 성공: to={}, subject={}", toEmail, title); // 추가
        } catch (Exception e) {
            log.error("이메일 전송 실패: to={}, subject={}, error={}", toEmail, title, e.getMessage()); // 추가
            throw new RuntimeException("이메일 전송에 실패했습니다.");
        }
    }

    /**
     * 이메일 메시지를 생성하는 메서드
     *
     * @param toEmail 수신자 이메일 주소
     * @param title   이메일 제목
     * @param text    이메일 내용
     * @return SimpleMailMessage 객체
     */
    private SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);
        return message;
    }
}