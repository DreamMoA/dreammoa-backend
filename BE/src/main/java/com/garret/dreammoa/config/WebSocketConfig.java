package com.garret.dreammoa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.garret.dreammoa.domain.service.stt.SpeechWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SpeechWebSocketHandler speechWebSocketHandler;

    public WebSocketConfig(SpeechWebSocketHandler speechWebSocketHandler) {
        this.speechWebSocketHandler = speechWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(speechWebSocketHandler, "/stt-websocket")
                .setAllowedOrigins("*") // ✅ 모든 도메인에서 접근 가능하도록 설정
                .addInterceptors(new HttpSessionHandshakeInterceptor()); // ✅ WebSocket 인증 우회
    }
}
