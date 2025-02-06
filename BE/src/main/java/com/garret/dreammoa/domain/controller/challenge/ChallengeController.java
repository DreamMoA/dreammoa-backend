package com.garret.dreammoa.domain.controller.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeCreateRequest;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeCreateResponse;
import com.garret.dreammoa.domain.service.challenge.ChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping("/create")
    public ResponseEntity<ChallengeCreateResponse> createChallenge(
            @RequestPart("challengeData") ChallengeCreateRequest requestData,
//            BindingResult bindingResult,
            @RequestPart(value = "thumbnail",required = false) MultipartFile thumbnail) throws Exception {

        System.out.println("controllllllllllllller");

        // 유효성 검증 결과 처리
//        if (bindingResult.hasErrors()) {
//            String errorMessage = bindingResult.getAllErrors().stream()
//                    .map(error -> error.getDefaultMessage())
//                    .collect(Collectors.joining(", "));
//            return ResponseEntity.badRequest().body(errorMessage);
//        }

        return challengeService.createChallenge(requestData,thumbnail);
    }
}
