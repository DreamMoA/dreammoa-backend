package com.garret.dreammoa.domain.service.stt;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpeechWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final SpeechService speechService;

    public SpeechWebSocketHandler(SpeechService speechService) {
        this.speechService = speechService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("✅ WebSocket 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("📡 클라이언트로부터 메시지 수신: " + payload);

        if ("START_STT".equals(payload)) {
            startSTT(session);
        } else if ("STOP_STT".equals(payload)) {
            stopSTT(session);
        } else {
            // Base64 인코딩된 음성 스트림 데이터 처리
            processAudioData(session, payload);
        }
    }

    private void startSTT(WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage("✅ STT 시작됨"));
    }

    private void stopSTT(WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage("🛑 STT 중단됨"));
    }

    private void processAudioData(WebSocketSession session, String audioBase64) {
        try {
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            String transcript = speechService.speechToTextFromBytes(audioBytes);
            // 변환된 텍스트를 클라이언트에 전송
            session.sendMessage(new TextMessage(transcript));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 모든 연결된 세션에 메시지를 방송 전송하는 메소드
    public void broadcastMessage(String message) {
        sessions.values().forEach(session -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
