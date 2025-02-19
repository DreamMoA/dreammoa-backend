package com.garret.dreammoa.domain.service.stt;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class SpeechWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("✅ WebSocket 연결됨: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("📡 클라이언트로부터 메시지 수신: " + payload);

        if (payload.equals("START_STT")) {
            startSTT(session);
        } else if (payload.equals("STOP_STT")) {
            stopSTT(session);
        } else {
            processAudioData(session, payload); // ✅ 오디오 스트림 데이터 처리
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
            byte[] audioBytes = java.util.Base64.getDecoder().decode(audioBase64);

            try (SpeechClient speechClient = SpeechClient.create()) {
                RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setSampleRateHertz(16000)
                        .setLanguageCode("ko-KR")
                        .build();

                RecognitionAudio audio = RecognitionAudio.newBuilder()
                        .setContent(ByteString.copyFrom(audioBytes))
                        .build();

                RecognizeResponse response = speechClient.recognize(config, audio);
                String transcript = response.getResultsList().stream()
                        .findFirst()
                        .map(result -> result.getAlternatives(0).getTranscript())
                        .orElse("⏳ 음성 인식 중...");

                // ✅ 클라이언트로 변환된 텍스트 전송
                session.sendMessage(new TextMessage(transcript));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
