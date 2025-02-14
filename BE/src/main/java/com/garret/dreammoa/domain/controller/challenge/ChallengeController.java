package com.garret.dreammoa.domain.controller.challenge;
import com.garret.dreammoa.domain.dto.challenge.requestdto.*;
import com.garret.dreammoa.domain.dto.challenge.responsedto.*;
import com.garret.dreammoa.domain.dto.dashboard.response.ChallengeMonthlyDetailDto;
import com.garret.dreammoa.domain.service.challenge.ChallengeService;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    @PostMapping("/create")
    public ResponseEntity<ChallengeResponse> createChallenge(
            @RequestPart(value = "challengeData") @Valid ChallengeCreateRequest requestData,
            @RequestPart(value = "thumbnail",required = false) MultipartFile thumbnail) throws Exception {
        return challengeService.createChallenge(requestData,thumbnail);
    }

    @PatchMapping("/update")
    public ResponseEntity<ChallengeResponse> updateChallenge(
            @RequestPart(value = "updateData") ChallengeUpdateRequest updateData,
            @RequestPart(value = "thumbnail",required = false) MultipartFile thumbnail) throws Exception {
        return challengeService.updateChallenge(updateData, thumbnail);
    }

    @GetMapping("/{challengeId}/info")
    public ResponseEntity<ChallengeInfoResponseDto> getChallengeInfo(@PathVariable Long challengeId) {
        return challengeService.getChallengeInfo(challengeId);
    }

    @PostMapping("/{challengeId}/join")
    public ResponseEntity<ChallengeResponse> joinChallenge(@PathVariable Long challengeId){
        return challengeService.joinChallenge(challengeId);
    }
    @DeleteMapping("{challengeId}/leave")
    public ResponseEntity<ChallengeResponse> leaveChallenge(@PathVariable Long challengeId){
        return challengeService.leaveChallenge(challengeId);
    }

    @PostMapping("/{challengeId}/enter")
    public ResponseEntity<ChallengeResponse> enterChallenge(@PathVariable Long challengeId, @RequestBody ChallengeLoadRequest loadDate) throws OpenViduJavaClientException, OpenViduHttpException {
        return challengeService.enterChallenge(challengeId, loadDate);
    }

    @PostMapping("/{challengeId}/exit")
    public ResponseEntity<ChallengeResponse> exitChallenge(@PathVariable Long challengeId, @RequestBody ChallengeExitRequest exitData) throws OpenViduJavaClientException, OpenViduHttpException {
        return challengeService.exitChallenge(challengeId, exitData);
    }

    @PatchMapping("/{challengeId}/delegate")
    public ResponseEntity<ChallengeResponse> delegateRoomManger(
            @PathVariable Long challengeId,
            @RequestParam Long newHostId){
        return challengeService.delegateRoomManager(challengeId, newHostId);
    }

    @PostMapping("/{challengeId}/kick")
    public ResponseEntity<ChallengeResponse> delegateRoomManger(
            @PathVariable Long challengeId,
            @RequestBody ChallengeKickRequest KickData){
        return challengeService.kickParticipate(challengeId, KickData);
    }

    @GetMapping("/my-challenges")
    public ResponseEntity<List<MyChallengeResponseDto>> getMyChallenges() {
        List<MyChallengeResponseDto> myChallenges = challengeService.getMyChallenges();
        return ResponseEntity.ok(myChallenges);
    }

    @GetMapping("/invite/{challengeId}")
    public ResponseEntity<String> generateChallengeInviteUrl(@PathVariable Long challengeId) throws Exception {
        return ResponseEntity.ok(challengeService.generateInviteUrl(challengeId));
    }

    @GetMapping("/invite/accept")
    public ResponseEntity<?> acceptInvite(@RequestParam("encryptedId") String encodedId) throws Exception {
        return challengeService.acceptInvite(encodedId);
    }

    @GetMapping("/tag-challenges")
    public ResponseEntity<List<MyChallengeResponseDto>> getTagChallenges(
            @RequestParam(required = false) String tags){
        return challengeService.getTagChallenges(tags);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchChallengeResponseDto> searchChallenges(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String keyword) {
        return challengeService.searchChallenges(tags, keyword);
    }

    @GetMapping("/all-challenges")
    public ResponseEntity<PagedChallengeResponseDto<MyChallengeResponseDto>> getPopularChallenges(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        return challengeService.getAllChallenges(tags, keyword, page);
    }



}
