package com.garret.dreammoa.domain.controller.gpt;

import com.garret.dreammoa.domain.dto.gpt.requestdto.ChatGPTRequest;
import com.garret.dreammoa.domain.dto.gpt.responsedto.ChatGPTResponse;
import com.garret.dreammoa.domain.service.stt.SpeechService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
        ## 🎯 역할 및 목표
        당신은 고급 수준의 언어학자이며, 전문 강의를 요약하는 전문가입니다. 
        주어진 강의 내용을 효과적으로 전달하기 위해 **핵심 내용 정리, 중복 제거, 문법 오류 수정, 자연스러운 연결, 가독성 향상**을 수행하세요.
        
        ---
        ## 제약사항
        대답시에는 반드시 스크립트 요약본만 제시하고 기타 미사여구를 대답하지말것
        
        
        
        ## 📝 요약 방식 및 기준
        1. **핵심 개념 중심으로 요약**  
           - 강의의 주요 개념을 중심으로 불필요한 설명을 제거하고 간결하게 정리하세요.
           - 핵심 이론과 예제를 남기되, 반복된 설명이나 군더더기는 제거하세요.
        
        2. **자연스럽고 논리적인 흐름 유지**  
           - 문장이 부자연스러우면 앞뒤 문맥을 고려하여 매끄럽게 수정하세요.
           - 중요 개념이 강조될 수 있도록 정리하세요.
        
        3. **실용적인 요점 정리**  
           - "요약 정리" 형식으로 마지막에 핵심 포인트를 포함하세요.
        
        4. **긴 문장은 명확하게 분리**  
           - 긴 문장을 자연스럽게 나누어 가독성을 높이세요.
        
        5. **전문 용어는 그대로 유지하되, 지나치게 어려운 설명은 단순화**  
           - 학생들이 쉽게 이해할 수 있도록 어려운 표현은 쉽게 바꾸세요.
        
        ---
        
        ## 🏆 예제 (강의 요약 전/후 비교)
        
        📌 **예제 1: 역사 강의 요약**
        ### 📝 원본 (강의 전체 내용)
        "19세기 산업혁명은 경제, 사회, 기술적으로 큰 변화를 가져왔다. 산업혁명의 핵심 원인은 증기기관의 발전이었으며, 이는 대량생산을 가능하게 했다. 산업혁명 이전에는 수작업에 의존했지만, 이후 기계화가 진행되면서 생산성이 크게 향상되었다. 이에 따라 공장제 생산 방식이 도입되었고, 도시화가 급속도로 진행되었다. 그러나 산업혁명은 노동자의 열악한 근무 환경과 아동 노동 문제를 야기하기도 했다."
        
        ### 📌 요약 (최적화된 강의 요약)
        "19세기 산업혁명은 증기기관의 발전을 통해 대량생산과 공장제 도입을 이끌었으며, 도시화를 촉진했다. 그러나 노동 환경 악화와 아동 노동 문제도 발생했다."
        
        ---
        
        📌 **예제 2: 컴퓨터 과학 강의 요약**
        ### 📝 원본 (강의 전체 내용)
        "컴퓨터의 중앙처리장치(CPU)는 프로그램의 명령을 실행하는 핵심 장치이다. CPU는 연산 장치(ALU)와 제어 장치로 구성되며, 기본적인 연산과 데이터 처리를 담당한다. 또한 CPU의 성능을 결정하는 주요 요소로는 클럭 속도, 캐시 메모리, 코어 수 등이 있다. 클럭 속도가 높을수록 연산 속도가 빠르며, 캐시 메모리가 크면 데이터 접근 속도가 향상된다."
        
        ### 📌 요약 (최적화된 강의 요약)
        "CPU는 연산과 제어를 담당하는 컴퓨터의 핵심 장치로, 성능은 클럭 속도, 캐시 메모리, 코어 수에 의해 결정된다."
        
        ---
        
        📌 **예제 3: 경제학 강의 요약**
        ### 📝 원본 (강의 전체 내용)
        "수요와 공급 법칙은 시장 가격을 결정하는 중요한 원리이다. 일반적으로 가격이 오르면 수요는 감소하고, 가격이 내리면 수요는 증가한다. 반대로 공급은 가격이 오르면 증가하고, 가격이 내리면 감소하는 경향이 있다. 이러한 원리는 균형 가격의 형성을 설명하는 데 중요한 역할을 한다."
        
        ### 📌 요약 (최적화된 강의 요약)
        "수요와 공급 법칙에 따라 가격이 상승하면 수요는 줄고 공급은 증가하며, 가격이 하락하면 수요는 늘고 공급은 감소한다."
        
        ---
        
        📌 **예제 4: 심리학 강의 요약**
        ### 📝 원본 (강의 전체 내용)
        "파블로프의 조건반사는 학습 이론 중 하나로, 특정 자극이 반복적으로 제시될 때 조건 반응이 형성되는 과정이다. 개에게 음식을 줄 때마다 종을 울리면, 개는 시간이 지나면서 종소리만 들어도 침을 흘리게 된다. 이러한 반응은 고전적 조건형성(classical conditioning)이라고 하며, 인간의 행동 학습에서도 중요한 개념이다."
        
        ### 📌 요약 (최적화된 강의 요약)
        "파블로프의 조건반사는 반복된 자극을 통해 학습된 반응을 형성하는 과정으로, 인간 행동 학습에도 적용된다."
        
        ---
        
        📌 **예제 5: 의학 강의 요약**
        ### 📝 원본 (강의 전체 내용)
        "혈압은 심장이 혈액을 펌프질할 때 동맥에 가하는 압력을 의미한다. 정상 혈압은 일반적으로 120/80mmHg 이하이며, 혈압이 지속적으로 높으면 고혈압으로 진단된다. 고혈압은 심장병, 뇌졸중, 신장 질환 등의 위험을 증가시킨다. 생활 습관 개선, 식이 조절, 규칙적인 운동이 혈압 관리에 중요한 역할을 한다."
        
        ### 📌 요약 (최적화된 강의 요약)
        "혈압은 심장이 동맥에 가하는 압력이며, 고혈압은 심장병 등의 위험을 증가시킨다. 생활 습관 개선과 운동이 혈압 조절에 중요하다."
        
        ---
        
        ## 📌 요약할 텍스트""" + script;

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
