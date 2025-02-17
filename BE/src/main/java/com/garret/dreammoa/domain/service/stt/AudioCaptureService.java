package com.garret.dreammoa.domain.service.stt;

import com.google.api.gax.rpc.ClientStream;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AudioCaptureService {
    private final SpeechRecognitionService speechRecognitionService;
    private static final int SAMPLE_RATE = 16000;
    private static final int BUFFER_SIZE = 6400;

    private volatile boolean isStreaming = false;
    private ExecutorService executor;
    private ClientStream<StreamingRecognizeRequest> clientStream;
    private SseEmitter currentEmitter;

    public AudioCaptureService(SpeechRecognitionService speechRecognitionService) {
        this.speechRecognitionService = speechRecognitionService;
    }

    public synchronized void startStreaming(SseEmitter emitter) {
        if (isStreaming) {
            return;
        }
        isStreaming = true;
        executor = Executors.newSingleThreadExecutor();
        currentEmitter = emitter;

        // ✅ SSE 연결 해제 시 자동으로 스트리밍 중단
        emitter.onCompletion(this::stopStreaming);
        emitter.onTimeout(this::stopStreaming);
        emitter.onError(e -> stopStreaming());

        executor.execute(() -> {
            TargetDataLine targetDataLine = null;
            try {
                // 🎙️ 오디오 설정
                AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
                targetDataLine.open(audioFormat);
                targetDataLine.start();

                // 🎤 Google Speech-to-Text 스트림 시작
                clientStream = speechRecognitionService.startRecognition(emitter);

                byte[] buffer = new byte[BUFFER_SIZE];
                while (isStreaming) {
                    int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        synchronized (this) {
                            if (isStreaming && clientStream != null) {
                                speechRecognitionService.sendAudioData(clientStream, buffer, bytesRead);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                stopStreaming();
                if (targetDataLine != null) {
                    targetDataLine.stop();
                    targetDataLine.close();
                }
            }
        });
    }

    public synchronized void stopStreaming() {
        if (!isStreaming) return;

        isStreaming = false;
        if (clientStream != null) {
            try {
                clientStream.closeSend();
            } catch (Exception ignored) {
            }
            clientStream = null;
        }
        if (executor != null) {
            executor.shutdown();
        }
        if (currentEmitter != null) {
            try {
                currentEmitter.complete();
            } catch (Exception ignored) {
            }
            currentEmitter = null;
        }
    }
    public boolean isStreaming() {
        return isStreaming;
    }
}
