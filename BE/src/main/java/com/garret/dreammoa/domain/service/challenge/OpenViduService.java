package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.common.ErrorResponse;
import io.openvidu.java.client.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenViduService {

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openVidu;

    @PostConstruct
    public void init(){
        //OpenVidu 서버와의 연결 객체 생성
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
        log.info("✅ OpenVidu 초기화 완료: {}", OPENVIDU_URL);
    }

    /**
     * 세션 ID를 받아 기존 세션이 있으면 반환, 없으면 새 세션 생성 후 반환
     */
    @Transactional
    public String getOrCreateSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);

        if (session == null) {
            log.info("🚀 새로운 OpenVidu 세션 생성: {}", sessionId);
            SessionProperties properties = new SessionProperties.Builder().build();
            session = openVidu.createSession(properties);
        } else {
            log.info("🔄 기존 OpenVidu 세션 존재: {}", sessionId);
        }

        return session.getSessionId();
    }
    /**
     * 특정 세션에 대해 연결 토큰 생성
     */
    @Transactional
    public String createConnection(String sessionId, Map<String, Object> params) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);

        if (session == null) {
            log.warn("⚠️ 세션을 찾을 수 없음: {}", sessionId);
            throw new IllegalArgumentException("세션이 존재하지 않습니다.");
        }

        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);

        log.info("🔑 연결 토큰 생성 완료: {}", connection.getToken());
        return connection.getToken();
    }

    @Transactional
    public void closeSession(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openVidu.getActiveSession(sessionId);
        if (session != null) {
            session.close();
        }
    }

    public boolean isSessionInvalid(String sessionId) {
        Session session = openVidu.getActiveSession(sessionId);
        return Objects.isNull(session); // OpenVidu에서 세션을 찾을 수 없으면 `true`
    }
}
