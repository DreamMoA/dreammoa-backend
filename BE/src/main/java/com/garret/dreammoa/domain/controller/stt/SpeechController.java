package com.garret.dreammoa.domain.controller.stt;

import com.garret.dreammoa.domain.service.stt.AudioCaptureService;
import com.garret.dreammoa.domain.service.stt.SseEmitterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SpeechController {
    private final AudioCaptureService audioCaptureService;

    private final SseEmitterService sseEmitterService;

    public SpeechController(AudioCaptureService audioCaptureService, SseEmitterService sseEmitterService) {
        this.audioCaptureService = audioCaptureService;
        this.sseEmitterService = sseEmitterService;
    }

    // 시작
    @GetMapping(value = "/stt-start", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> startSpeechRecognition() {
        if (audioCaptureService.isStreaming()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 🚨 중복 실행 방지 (409 Conflict)
        }

        SseEmitter emitter = sseEmitterService.createEmitter();
        audioCaptureService.startStreaming(emitter);

        emitter.onCompletion(audioCaptureService::stopStreaming);
        emitter.onTimeout(audioCaptureService::stopStreaming);
        emitter.onError(e -> audioCaptureService.stopStreaming());

        return ResponseEntity.ok(emitter);
    }
    
    // 종료
    @PostMapping("/stt-stop")
    public ResponseEntity<String> stopSpeechRecognition() {
        System.out.println("🛑 음성 인식 종료 요청 수신됨.");
        audioCaptureService.stopStreaming();

        if (!audioCaptureService.isStreaming()) {
            return ResponseEntity.ok("✅ 음성 인식이 정상적으로 종료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 음성 인식 종료 실패.");
        }
    }
}
