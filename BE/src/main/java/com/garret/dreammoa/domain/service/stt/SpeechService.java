package com.garret.dreammoa.domain.service.stt;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SpeechService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    @Value("${openai.api.key}")
    private String openaiApiKey;

    // 공통 음성 인식 로직 (음성 데이터를 받아 텍스트로 변환)
    private String convertSpeech(byte[] audioBytes, RecognitionConfig config) throws IOException {
        ByteString audioData = ByteString.copyFrom(audioBytes);
        RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder()
                .setContent(audioData)
                .build();

        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognizeResponse response = speechClient.recognize(config, recognitionAudio);
            List<SpeechRecognitionResult> results = response.getResultsList();
            if (!results.isEmpty()) {
                return results.get(0).getAlternatives(0).getTranscript();
            } else {
                System.out.println("No transcription result found");
                return "";
            }
        }
    }

    // 화상통화 중 녹음된 파일(MultipartFile)을 처리하는 메소드
    // 파일 기반 음성 인식도 동일한 포맷(LINEAR16, 16kHz)으로 처리하도록 수정
    public String speechToText(MultipartFile audioFile) throws IOException {
        if (audioFile.isEmpty()) {
            throw new IOException("전달받은 음성 데이터 audioFile이 빈 파일입니다.");
        }

        byte[] audioBytes = audioFile.getBytes();
        // 파일에 상관없이 이제 LINEAR16, 16kHz 포맷을 사용하여 인식합니다.
        RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("ko-KR")
                .build();

        return convertSpeech(audioBytes, recognitionConfig);
    }

    // WebSocket 등에서 byte[] 형태로 전달된 음성 데이터를 LINEAR16 형식으로 처리하는 메소드
    public String speechToTextFromBytes(byte[] audioBytes) throws IOException {
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("ko-KR")
                .build();

        return convertSpeech(audioBytes, config);
    }

    // 음성 인식된 텍스트를 요약하는 기능 (OpenAI API 호출)
    public String textSummary(String speechText) {
        String prompt = String.format(
                "다음 텍스트를 간결하게 요약해줘:\n\n%s\n\n요약:",
                speechText
        );
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("temperature", 0.7);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.set("messages", messages);

            String requestBodyString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestBody);
            System.out.println("Request Body: " + requestBodyString);

            RequestBody body = RequestBody.create(
                    requestBodyString,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + openaiApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    System.out.println("OpenAI API error: " + responseBody);
                    throw new IOException("Unexpected code " + response);
                }
                JsonNode responseJson = objectMapper.readTree(responseBody);
                String content = responseJson
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText()
                        .trim();
                return removeCodeBlock(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 응답 내용에서 코드 블록을 제거하는 메소드
    private String removeCodeBlock(String content) {
        Pattern pattern = Pattern.compile("```json\\n([\\s\\S]*?)\\n```");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return content;
    }
}
