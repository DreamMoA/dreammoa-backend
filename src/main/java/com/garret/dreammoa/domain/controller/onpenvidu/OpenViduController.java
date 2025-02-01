package com.garret.dreammoa.domain.controller.onpenvidu;



import com.garret.dreammoa.domain.dto.common.ErrorResponse;
import io.openvidu.java.client.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@RequestMapping("/openvidu")
@RestController
public class OpenViduController{

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openVidu;

    @PostConstruct
    public void init(){
        //OpenVidu 서버와의 연결 객체 생성
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
        System.out.println(" openVidu init 완료");
    }

    /**
     * 새로운 세션 생성
     * 여기서 세션이 안마들어지면 JWT FILTER 통과 못한거임
     *
     * @param params 세션 속성을 포함하는 JSON 객체(필수 아님)
     * @return 생성된 세션의 ID를 포함하는 ResponseEntity
     * @throws OpenViduJavaClientException OpenVidu Java 클라이언트 고나련 예외 발생
     * @Throws OpenViduHttpException            OpenVidu 서버와 HTTP 요청 중 오류 발생 시
     */
    @PostMapping("/sessions")
    public ResponseEntity<?> initializeSession(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest httpRequest) {
        String accessToken = Arrays.stream(httpRequest.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Access Token이 없습니다."));
        }

        try {
            SessionProperties properties = SessionProperties.fromJson(params).build();
            Session session = openVidu.createSession(properties);

            if (Objects.isNull(session)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("세션 생성에 실패했습니다."));
            }

            return ResponseEntity.ok(session.getSessionId());
        } catch (OpenViduJavaClientException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("OpenVidu 클라이언트 오류: " + e.getMessage()));
        } catch (OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse("OpenVidu 서버 연결 오류: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("알 수 없는 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 지정된 세션에 대해 연결 토큰을 생성
     * 여기서 세션이 안마들어지면 JWT FILTER 통과 못한거임
     *
     * @param sessionId 연결 토큰을 생성할 세션의 ID
     * @param params 연결 속성을 포함하는 JSON 객체(선택 사항)
     * @return 생성된 연결 토큰을 포함하는 ResponseEntity
     * @Throws OpenViduJavaClientException OpenVIdu Java 클라이언트 관련 예외 발생 시
     * @Throws OpenViduHttpException OpenVIdu 서버와 HTTP 연결 중 오류 발생 시
     */
    @PostMapping("/sessions/{sessionId}/connections")
    public ResponseEntity<?> createConnection(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params, HttpServletRequest httpRequest) throws OpenViduJavaClientException, OpenViduHttpException {
        String accessToken = Arrays.stream(httpRequest.getCookies())
                .filter(cookie -> "access_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if(accessToken == null){
            return ResponseEntity.badRequest().body(new ErrorResponse("Access Token이 없습니다."));
        }


        System.out.println("createConnection 실행 완료 ");
        Session session = openVidu.getActiveSession(sessionId);
        if(session == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        System.out.println("createConnection 로직 완료 ");

        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }
}