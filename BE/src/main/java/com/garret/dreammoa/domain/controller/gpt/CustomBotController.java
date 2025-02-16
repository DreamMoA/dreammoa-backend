package com.garret.dreammoa.domain.controller.gpt;

import com.garret.dreammoa.domain.dto.gpt.requestdto.ChatGPTRequest;
import com.garret.dreammoa.domain.dto.gpt.responsedto.ChatGPTResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class CustomBotController {

    private static final Logger logger = LoggerFactory.getLogger(CustomBotController.class);

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiURL;

    @Autowired
    private RestTemplate template;

    /**
     * ✅ OpenAI API를 이용한 STT 요약 (POST 요청)
     */
    @PostMapping("/gpt-summary")
    public String chat(@RequestBody String script) {
        // ✅ 로그 출력 (API 요청 확인)
        logger.info("🚀 OpenAI API 요청 시작");
        logger.info("✅ Model: {}", model);
        logger.info("✅ API URL: {}", apiURL);
        logger.info("✅ STT 원본 데이터: {}", script);

        // ✅ 프롬프트 엔지니어링 적용
        String prompt = """
                ## 페르소나
                당신은 국어 전문가적 지식을 지니고 있는 언어학자입니다.

                ## 역할
                보내준 스크립트에 대한 중복 제거, 오타 수정, 문법 수정, 적절히 처리 등을 통해 자연스러운 요약본을 만드세요, 만약 문맥이 자연스럽지 않다면 앞뒤 단어를 기반으로 내용을 추측해서 작성하세요.
                보내준 요약본은 전체 스크립트에 대한 요약을 제공하여 한눈에 전체 내용을 이해할 수 있도록 일목요연해야합니다.
            
                ## 스크립트
                """ + script;

        // ✅ OpenAI API 요청 객체 생성
        ChatGPTRequest request = new ChatGPTRequest(model, prompt);

        // ✅ OpenAI API 요청 (RestTemplate 이용)
        ChatGPTResponse chatGPTResponse = template.postForObject(apiURL, request, ChatGPTResponse.class);

        // ✅ 결과 반환
        if (chatGPTResponse != null && chatGPTResponse.getChoices() != null && !chatGPTResponse.getChoices().isEmpty()) {
            String summary = chatGPTResponse.getChoices().get(0).getMessage().getContent();
            logger.info("✅ GPT 요약 결과: {}", summary);
            return summary;
        } else {
            logger.error("❌ GPT 요약 실패");
            return "요약 실패: OpenAI API 응답이 올바르지 않습니다.";
        }
    }
}
