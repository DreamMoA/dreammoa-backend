package com.garret.dreammoa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // ✅ 랜덤 포트로 실행
public class SpeechWebSocketHandlerTest {

    @LocalServerPort
    private int port;

    private WebSocketSession session;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    @BeforeEach
    void setUp() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        CompletableFuture<WebSocketSession> futureSession = new CompletableFuture<>();

        String wsUrl = "ws://localhost:" + port + "/stt-websocket"; // ✅ WebSocket 주소

        client.execute(new AbstractWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                System.out.println("✅ WebSocket 연결 성공: " + session.getId());
                futureSession.complete(session);
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messageQueue.offer(message.getPayload()); // ✅ 서버에서 반환된 STT 결과 수신
            }
        }, wsUrl);

        session = futureSession.get(10, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Test
    void testWebSocketConnection() throws Exception {
        assertNotNull(session);
        assertTrue(session.isOpen());
    }

    @Test
    void testStartSTT() throws Exception {
        session.sendMessage(new TextMessage("START_STT"));

        await().atMost(5, TimeUnit.SECONDS).until(() -> !messageQueue.isEmpty());
        String response = messageQueue.poll();
        assertEquals("✅ STT 시작됨", response);
    }

    @Test
    void testStopSTT() throws Exception {
        session.sendMessage(new TextMessage("STOP_STT"));

        await().atMost(5, TimeUnit.SECONDS).until(() -> !messageQueue.isEmpty());
        String response = messageQueue.poll();
        assertEquals("🛑 STT 중단됨", response);
    }

    @Test
    void testSTTResponse() throws Exception {
        // ✅ 더미 오디오(Base64) 데이터를 서버로 전송
        String dummyAudioBase64 = "UklGRi4AAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAA";
        session.sendMessage(new TextMessage(dummyAudioBase64));

        // ✅ 변환된 STT 텍스트가 서버에서 올 때까지 기다림 (최대 10초)
        await().atMost(10, TimeUnit.SECONDS).until(() -> !messageQueue.isEmpty());

        // ✅ WebSocket 응답을 가져와서 확인
        String response = messageQueue.poll();
        assertNotNull(response); // 응답이 비어있지 않아야 함
        System.out.println("🎤 WebSocket 응답 (STT 결과): " + response);
    }
}
