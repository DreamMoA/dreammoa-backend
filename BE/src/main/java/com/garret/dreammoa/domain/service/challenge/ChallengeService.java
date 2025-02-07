package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeCreateRequest;
import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeUpdateRequest;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeResponse;
import com.garret.dreammoa.domain.model.*;
import com.garret.dreammoa.domain.repository.ChallengeRepository;
import com.garret.dreammoa.domain.repository.ParticipantHistoryRepository;
import com.garret.dreammoa.domain.repository.ParticipantRepository;
import com.garret.dreammoa.domain.repository.TagRepository;
import com.garret.dreammoa.domain.service.FileService;
import com.garret.dreammoa.utils.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final SecurityUtil securityUtil;
    private final FileService fileService;
    private final TagRepository tagRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantHistoryRepository participantHistoryRepository;

    @Transactional
    public ResponseEntity<ChallengeResponse> createChallenge(
            ChallengeCreateRequest request,
            MultipartFile thumbnail) throws Exception {
        UserEntity user = securityUtil.getCurrentUser();
        log.info("현재 유저: {}", user.getName());

        // 챌린지 엔터티 생성
        ChallengeEntity challenge = ChallengeEntity.builder()
                .host(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .isPrivate(request.getIsPrivate())
                .startDate(request.getStartDate())
                .expireDate(request.getExpireDate())
                .standard(request.getStandard())
                .isActive(false)
                .build();

        // 태그 저장
        for (String tagName : request.getTags()) {
            TagEntity tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(TagEntity.builder().tagName(tagName).build()));
            challenge.addTag(tag);
        }
        // 챌린지 테이블에 추가
        ChallengeEntity savedChallenge = challengeRepository.save(challenge);

        // 참여자 테이블에 추가
        ParticipantEntity participant = ParticipantEntity.builder()
                .challenge(challenge)
                .user(user)
                .isHost(true)
                .isActive(false)
                .build();
        participantRepository.save(participant);

        Long challengeId = savedChallenge.getChallengeId();

        // 사진 처리
        // 썸네일 처리: null 기본 이미지 설정
        String thumbnailURL;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            log.info("파일 업로드 중: {}", thumbnail.getOriginalFilename());
            thumbnailURL = fileService.saveFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE).getFileUrl(); // 실제 파일 저장
            return ResponseEntity.ok(ChallengeResponse.fromEntity(thumbnailURL, challenge, "챌린지가 성공적으로 생성되었습니다."));
        } else {
            log.info("썸네일이 없으므로 기본 이미지 사용");
            return  ResponseEntity.ok(ChallengeResponse.fromEntity(challenge, "챌린지가 성공적으로 생성되었습니다."));
        }

    }
    @Transactional
    public ResponseEntity<ChallengeResponse> updateChallenge(ChallengeUpdateRequest request, MultipartFile thumbnail) throws Exception {

        UserEntity user = securityUtil.getCurrentUser();
        Long challengeId = request.getChallengeId();
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        // 방장 여부 확인
        if (!challenge.getHost().getId().equals(user.getId())) {
            throw new IllegalArgumentException("챌린지를 수정할 권한이 없습니다.");
        }
        challenge.update(request.getTitle(), request.getDescription(), request.getMaxParticipants(),
                request.getIsPrivate(), request.getStartDate(), request.getExpireDate(),
                request.getStandard());

        // 태그 업데이트
        List<String> existingTags = challenge.getChallengeTags().stream()
                .map(challengeTag -> challengeTag.getTag().getTagName())
                .toList();

        // 삭제해야 할 태그
        challenge.getChallengeTags().removeIf(challengeTag ->
                !request.getTags().contains(challengeTag.getTag().getTagName())
        );
        // 추가해야 할 태그
        for (String tagName : request.getTags()) {
            if (!existingTags.contains(tagName)) {
                TagEntity tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> tagRepository.save(TagEntity.builder().tagName(tagName).build()));
                challenge.addTag(tag);
            }
        }

        challengeRepository.save(challenge);

        if (thumbnail != null) {
            FileEntity file = fileService.updateFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE);
            String newThumbnail = file.getFileUrl();
            return ResponseEntity.ok(ChallengeResponse.fromEntity(newThumbnail,challenge,"챌린지가 성공적으로 수정되었습니다."));
        }
        return ResponseEntity.ok(ChallengeResponse.fromEntity(challenge,"챌린지가 성공적으로 수정되었습니다."));
    }

    public ResponseEntity<ChallengeResponse> getChallengeInfo(Long challengeId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));
        List<FileEntity> files = fileService.getFiles(challenge.getChallengeId(), FileEntity.RelatedType.CHALLENGE);
        String thumbnail = files.get(0).getFileUrl();
        return ResponseEntity.ok(ChallengeResponse.fromEntity(thumbnail, challenge));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> joinChallenge(Long challengeId) {

        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        UserEntity user = securityUtil.getCurrentUser();
        // 강퇴 이력 조회
        Optional<ParticipantHistoryEntity> kickedHistory = participantHistoryRepository.findByChallengeIdAndUserIdAndStatus(challengeId, user.getId(), ParticipantHistoryEntity.Status.KICKED);
        if (kickedHistory.isPresent()) {
            String kickedByName = kickedHistory.get().getActionByUser().getName();  // 강퇴한 사람의 이름
            return ResponseEntity.badRequest()
                    .body(ChallengeResponse.responseMessage("강퇴된 참가자는 다시 참여할 수 없습니다. 강퇴한 사람: " + kickedByName));
        }
        // 최대참여자 수 넘었는지 조회
        long maxParticipants = challenge.getMaxParticipants();
        long currentParticipants = participantRepository.countByChallengeId(challengeId);
        if (currentParticipants >= maxParticipants) {
            throw new IllegalStateException("챌린지 참여 인원이 가득 찼습니다.");
        }
        // 방에 이미 참여하고있는지 확인
        boolean alreadyJoined = participantRepository.existsByChallengeIdAndUserId(challengeId, user.getId());
        if (alreadyJoined) {
            throw new IllegalStateException("이미 해당 챌린지에 참여 중입니다.");
        }
        ParticipantEntity participant = ParticipantEntity.builder()
                .challenge(challenge)
                .user(user)
                .isHost(false)
                .isActive(false)
                .build();
        participantRepository.save(participant);
        return ResponseEntity.ok(ChallengeResponse.responseMessage("챌린지에 성공적으로 참여했습니다."));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> enterChallenge(Long challengeId) {

        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        UserEntity user = securityUtil.getCurrentUser();

        // 3. 참여자 정보 조회 및 상태 업데이트
        ParticipantEntity participant = participantRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여자가 아닙니다."));

        // 참여자 상태를 활성화로 변경
        participant.setIsActive(true);
        participantRepository.save(participant);

//        OpenVidu openVidu = new OpenVidu(); // OpenVidu 객체 생성 (연결된 OpenVidu 서버 필요)
//        Session session = openVidu.createSession(); // 새로운 세션 생성
//
//        // 세션 ID 반환 (프론트엔드에서 이 세션을 사용하여 연결)
//        String sessionId = session.getSessionId();
//
//        // 5. 응답 객체 반환 (세션 ID 포함)
//        ChallengeResponse challengeResponse = new ChallengeResponse(challenge, sessionId);
//
//        return ResponseEntity.ok(challengeResponse);

    }

    // status 처리
//    String status;
//    LocalDate today = LocalDate.now();
//    LocalDate startDate = request.getStartDate().toLocalDate();
//    LocalDate expireDate = request.getExpireDate().toLocalDate();
//
//        if (startDate.isAfter(today)) {
//        status = "대기중";  // 시작일이 미래일 경우
//    } else if (startDate.isEqual(today) && expireDate.isAfter(today)) {
//        status = "진행중";  // 오늘이 시작일이고 종료일이 미래일 경우
//    } else {
//        status = "종료";  // 종료일이 과거일 경우
//    }
}

