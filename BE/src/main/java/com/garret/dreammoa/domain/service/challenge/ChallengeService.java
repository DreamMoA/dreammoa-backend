package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.*;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeResponse;
import com.garret.dreammoa.domain.model.*;
import com.garret.dreammoa.domain.repository.*;
import com.garret.dreammoa.domain.service.FileService;
import com.garret.dreammoa.domain.service.tag.TagServiceImpl;
import com.garret.dreammoa.utils.SecurityUtil;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final SecurityUtil securityUtil;
    private final FileService fileService;
    private final TagServiceImpl tagService;
    private final ParticipantService participantService;
    private final OpenViduService openViduService;
    private final ParticipantHistoryService participantHistoryService;
    private final ChallengeLogService challengeLogService;

    @Transactional
    public ResponseEntity<ChallengeResponse> createChallenge(
            ChallengeCreateRequest request,
            MultipartFile thumbnail) throws Exception {
        UserEntity user = securityUtil.getCurrentUser();
        log.info("현재 유저: {}", user.getName());

        // 챌린지 엔터티 생성
        ChallengeEntity challenge = ChallengeEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .isPrivate(request.getIsPrivate())
                .startDate(request.getStartDate())
                .expireDate(request.getExpireDate())
                .standard(request.getStandard())
                .isActive(false)
                .sessionId(null)
                .build();

        // 요청데이타에 없는 태그는 등록 후 챌린지에 추가
        List<TagEntity> tags = tagService.getOrCreateTags(request.getTags());
        for (TagEntity tag : tags) {
            challenge.addTag(tag);
        }
        // 챌린지 테이블에 추가
        ChallengeEntity savedChallenge = challengeRepository.save(challenge);

        // 참여자 테이블에 추가
        participantService.addParticipant(user, savedChallenge, true);

        Long challengeId = savedChallenge.getChallengeId();

        // 사진 처리
        // 썸네일 처리: null 기본 이미지 설정
        if (thumbnail != null && !thumbnail.isEmpty()) {
            log.info("파일 업로드 중: {}", thumbnail.getOriginalFilename());
            String thumbnailURL = fileService.saveFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE).getFileUrl(); // 실제 파일 저장
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
        if(!participantService.isHost(user,challenge)){
            throw new IllegalArgumentException("챌린지를 수정할 권한이 없습니다.");
        }
        // 챌린지 정보 업데이트
        challenge.update(request.getTitle(), request.getDescription(), request.getMaxParticipants(),
                request.getIsPrivate(), request.getStartDate(), request.getExpireDate(),
                request.getStandard());

        // 태그 업데이트
        tagService.updateTags(challenge, request.getTags());
//        challengeRepository.save(challenge); 필요 없음!

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

        participantService.addParticipant(challenge, user);

        return ResponseEntity.ok(ChallengeResponse.responseMessage("챌린지에 성공적으로 참여했습니다."));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> leaveChallenge(Long challengeId) {

        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));
        UserEntity user = securityUtil.getCurrentUser();

        // 참가자 정보 조회 및 상태 업데이트
        participantService.leaveParticipant(user, challenge);
        participantHistoryService.createLeftHistory(user,challenge,"본인이 나가기 선택");

        return ResponseEntity.ok(ChallengeResponse.responseMessage("챌린지에서 정상적으로 탈퇴했습니다."));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> enterChallenge(Long challengeId, ChallengeLoadRequest loadDate) throws OpenViduJavaClientException, OpenViduHttpException {

        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        UserEntity user = securityUtil.getCurrentUser();

        // 참여자 정보 조회 및 상태 업데이트
        participantService.activateParticipant(user, challenge);

        // ✅ OpenVidu 세션 확인 및 생성
        String sessionId = openViduService.getOrCreateSession(challengeId.toString());

        // ✅ 세션 ID를 챌린지에 저장
        challenge.setSessionId(sessionId);
        challengeRepository.save(challenge);

        // ✅ 연결 토큰 생성
        String token = openViduService.createConnection(sessionId, Map.of());

        // ✅ 참가자에게 토큰 저장
        participantService.saveParticipantToken(user, challenge, token);
        // ✅ 기존 학습 로그 조회
        Optional<ChallengeLogEntity> existingLog = challengeLogService.loadStudyLog(user, challenge, loadDate.getRecordAt());
        return existingLog.map(challengeLogEntity -> ResponseEntity.ok(ChallengeResponse.responseTokenWithLog("해당 날짜의 기록과 토큰", challengeLogEntity, token)))
                .orElseGet(() -> ResponseEntity.ok(ChallengeResponse.responseToken("해당 날짜의 학습 기록이 없습니다.", token)));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> exitChallenge(Long challengeId, ChallengeExitRequest exitData) throws OpenViduJavaClientException, OpenViduHttpException {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        UserEntity user = securityUtil.getCurrentUser();

        challengeLogService.saveStudyLog(user, challenge, exitData);

        participantService.deactivateParticipant(user, challenge);

        long remainingParticipants = participantService.countActiveParticipants(challenge);
        if (remainingParticipants == 0) {
            // ✅ 참가자가 없으면 세션 종료
            openViduService.closeSession(challenge.getSessionId());
            challenge.setSessionId(null); // 세션 ID 제거
            challengeRepository.save(challenge);
        }
        return ResponseEntity.ok(ChallengeResponse.responseMessage("챌린지 세션에서 정상적으로 나갔습니다."));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> delegateRoomManager(Long challengeId, Long newHostId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));
        UserEntity user = securityUtil.getCurrentUser();

        Optional<ParticipantEntity> currentHost = participantService.getCurrentHost(user, challenge);
        Optional<ParticipantEntity> newHost = participantService.getParticipant(newHostId);

        currentHost.ifPresent(host -> {
            if (!host.getIsHost()) {
                throw new IllegalArgumentException("방장만 권한을 위임할 수 있습니다.");
            }

            newHost.ifPresent(newHostParticipant -> {
                if (!newHostParticipant.getChallenge().equals(challenge)) {
                    throw new IllegalArgumentException("새로운 방장 후보는 해당 챌린지에 참여해야 합니다.");
                }

                // 방장 권한 위임
                participantService.delegateHost(host, newHostParticipant);
            });
        });

        return ResponseEntity.ok(ChallengeResponse.responseMessage("방장 권한이 성공적으로 위임되었습니다."));
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> kickParticipate(Long challengeId, ChallengeKickRequest kickData) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));
        UserEntity user = securityUtil.getCurrentUser();

        Optional<ParticipantEntity> currentHost = participantService.getCurrentHost(user, challenge);

        currentHost.ifPresent(host -> {
            if (!host.getIsHost()) {
                throw new IllegalArgumentException("방장만 강퇴할 수 있습니다.");
            }
        });

        ParticipantEntity targetParticipant = participantService.getParticipant(kickData.getKickedUserId())
                .orElseThrow(() -> new IllegalArgumentException("강퇴할 참가자를 찾을 수 없습니다."));

        participantHistoryService.createKickHistory(user, challenge, kickData, targetParticipant);

        participantService.kickParticipant(kickData);

        return ResponseEntity.ok(ChallengeResponse.responseMessage("사용자가 챌린지에서 강퇴되었습니다."));
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

