package com.garret.dreammoa.domain.service.stt;

import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.api.gax.rpc.ClientStream;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
public class SpeechRecognitionService {
    private  final SpeechClient speechClient;
    private volatile boolean isStreaming = false;
    private ClientStream<StreamingRecognizeRequest> clientStream;

    public SpeechRecognitionService(SpeechClient speechClient) {
        this.speechClient = speechClient;
    }

    public synchronized ClientStream<StreamingRecognizeRequest> startRecognition(SseEmitter emitter) {
        if(isStreaming) {
            return null;
        }

        isStreaming = true;

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("ko-KR")
                .setEnableAutomaticPunctuation(true)
                .build();

        StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(true)
                .setSingleUtterance(false)
                .build();

        clientStream = speechClient.streamingRecognizeCallable().splitCall(new ResponseObserver<StreamingRecognizeResponse> () {
            @Override
            public void onStart(StreamController controller) {
                System.out.println("🎤 Google Speech-to-Text 스트리밍 시작됨.");
            }

            @Override
            public void onResponse(StreamingRecognizeResponse response) {
                for (StreamingRecognitionResult result : response.getResultsList()) {
                    String transcript = result.getAlternativesList().get(0).getTranscript();
                    System.out.println("📡 STT 결과: " + transcript);
                    try {
                        emitter.send(transcript);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }
            }

            @Override
            public void onComplete() {
                isStreaming = false;
                emitter.complete();
            }

            @Override
            public void onError(Throwable t) {
                isStreaming = false;
                emitter.completeWithError(t);
            }
        });

        clientStream.send(StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingConfig)
                .build());

        return clientStream;

    }

    public synchronized void sendAudioData(ClientStream<StreamingRecognizeRequest> clientStream, byte[] buffer, int bytesRead) {
        if (!isStreaming || clientStream == null) return;

        ByteString audioBytes = ByteString.copyFrom(buffer, 0, bytesRead);
        StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                .setAudioContent(audioBytes)
                .build();

        try {
            clientStream.send(request);
        } catch (Exception e) {
            System.err.println("❌ STT 전송 오류: " + e.getMessage());
        }
    }
}
