package com.garret.dreammoa.domain.controller.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeCreateRequest;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeCreateResponse;
import com.garret.dreammoa.domain.service.challenge.ChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping("/create")
    public ResponseEntity<ChallengeCreateResponse> createChallenge(
            HttpServletRequest request,
            @RequestPart("challengeData") ChallengeCreateRequest requestData,
            @RequestPart(value = "Thumbnail",required = false) MultipartFile thumbnail){
        return challengeService.createChallenge(request,requestData,thumbnail);
    }
}
