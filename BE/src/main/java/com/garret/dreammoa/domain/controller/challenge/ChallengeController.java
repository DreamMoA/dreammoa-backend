package com.garret.dreammoa.domain.controller.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeCreateRequest;
import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeUpdateRequest;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeResponse;
import com.garret.dreammoa.domain.service.challenge.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping("/create")
    public ResponseEntity<ChallengeResponse> createChallenge(
            @RequestPart(value = "challengeData") ChallengeCreateRequest requestData,
            @RequestPart(value = "thumbnail",required = false) MultipartFile thumbnail) throws Exception {
        return challengeService.createChallenge(requestData,thumbnail);
    }

    @PutMapping("/update")
    public ResponseEntity<ChallengeResponse> updateChallenge(
            @RequestPart(value = "updateData") ChallengeUpdateRequest updateData,
            @RequestPart(value = "thumbnail",required = false) MultipartFile thumbnail) throws Exception {

        return challengeService.updateChallenge(updateData, thumbnail);
    }
}
