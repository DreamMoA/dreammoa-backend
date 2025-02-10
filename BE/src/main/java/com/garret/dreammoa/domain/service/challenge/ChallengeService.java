package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.*;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeResponse;
import com.garret.dreammoa.domain.dto.challenge.responsedto.MyChallengeDetailResponseDto;
import com.garret.dreammoa.domain.dto.challenge.responsedto.MyChallengeResponseDto;
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
import java.util.stream.Collectors;

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

    @Transactional
    public List<MyChallengeResponseDto> getMyChallenges() {
        UserEntity currentUser = securityUtil.getCurrentUser();

        //사용자가 참여한 ParticipantEntity 목록 조회
        List<ParticipantEntity> participations = participantRepository.findByUser(currentUser);

        //각 참여 내역에서 챌린지 정보를 추출하여 DTO로 변환
        return participations.stream()
                .map(ParticipantEntity::getChallenge)
                .distinct()
                .map(challenge -> {
                    // 챌린지에 달린 태그명 리스트 생성
                    List<String> tagNames = challenge.getChallengeTags().stream()
                            .map(challengeTag -> challengeTag.getTag().getTagName())
                            .collect(Collectors.toList());

                    UserEntity host = challenge.getHost();
                    String profilePictureUrl = null;
                    if(host.getProfileImage() != null) {
                        profilePictureUrl = host.getProfileImage().getFileUrl();
                    }

                    MyChallengeResponseDto.HostInfo hostInfo = new MyChallengeResponseDto.HostInfo();
                    hostInfo.setHostId(host.getId());
                    hostInfo.setNickname(host.getNickname());
                    hostInfo.setProfilePictureUrl(profilePictureUrl);

                    // DTO 생성
                    MyChallengeResponseDto dto = new MyChallengeResponseDto();
                    dto.setChallengeId(challenge.getChallengeId());
                    dto.setTitle(challenge.getTitle());
                    dto.setDescription(challenge.getDescription());
                    dto.setStartDate(challenge.getStartDate());
                    dto.setExpireDate(challenge.getExpireDate());
                    dto.setIsActive(challenge.getIsActive());
                    dto.setTags(tagNames);
                    dto.setHost(hostInfo);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 현재 로그인한 사용자가 참여 중인 특정 챌린지의 상세 정보를 DTO로 반환
     * @param challengeId 조회할 챌린지 id
     * @return MyChallengeDetailResponseDto
     * @throws IllegalArgumentException 사용자가 해당 챌린지에 참여 중이 아닐 경우
     */
    @Transactional
    public MyChallengeDetailResponseDto getMyChallengeDetail(Long challengeId) {
        // 현재 로그인한 사용자 조회
        UserEntity currentUser = securityUtil.getCurrentUser();

        // 챌린지 엔티티 조회
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        // 현재 사용자가 해당 챌린지에 참여 중인지 확인
        boolean isParticipant = participantRepository.findByUser(currentUser)
                .stream()
                .anyMatch(participant -> participant.getChallenge().getChallengeId().equals(challengeId));
        if (!isParticipant) {
            throw new IllegalArgumentException("해당 챌린지에 참여 중이 아닙니다.");
        }

        // DTO로 변환
        MyChallengeDetailResponseDto dto = new MyChallengeDetailResponseDto();
        dto.setChallengeId(challenge.getChallengeId());
        dto.setTitle(challenge.getTitle());
        dto.setDescription(challenge.getDescription());
        dto.setStartDate(challenge.getStartDate());
        dto.setExpireDate(challenge.getExpireDate());
        dto.setIsActive(challenge.getIsActive());
        dto.setMaxParticipants(challenge.getMaxParticipants());
        dto.setStandard(challenge.getStandard());

        // 챌린지에 달린 태그 목록 추출
        List<String> tagNames = challenge.getChallengeTags().stream()
                .map(challengeTag -> challengeTag.getTag().getTagName())
                .collect(Collectors.toList());
        dto.setTags(tagNames);

        // 방장(호스트) 정보 설정
        UserEntity host = challenge.getHost();
        MyChallengeDetailResponseDto.HostInfo hostInfo = new MyChallengeDetailResponseDto.HostInfo();
        hostInfo.setHostId(host.getId());
        hostInfo.setNickname(host.getNickname());
        String profilePictureUrl = (host.getProfileImage() != null) ? host.getProfileImage().getFileUrl() : null;
        hostInfo.setProfilePictureUrl(profilePictureUrl);
        dto.setHost(hostInfo);

        return dto;
    }

    /**
     * 현재 로그인한 사용자가 참여 중인 챌린지에서 탈퇴합니다.
     * 호스트인 경우는 탈퇴가 불가능합니다.
     *
     * @param challengeId 탈퇴할 챌린지 id
     * @throws IllegalArgumentException 챌린지를 찾지 못하거나 참여 중이 아니거나, 호스트인 경우
     */
    @Transactional
    public void quitChallenge(Long challengeId) {
        // 현재 로그인한 사용자 조회
        UserEntity currentUser = securityUtil.getCurrentUser();

        // 챌린지 조회
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        // 현재 사용자가 해당 챌린지에 참여 중인지 조회
        ParticipantEntity participant = participantRepository.findByUserAndChallenge(currentUser, challenge)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지에 참여 중이 아닙니다."));

        // 호스트는 탈퇴할 수 없도록 처리
        if (participant.getIsHost() != null && participant.getIsHost()) {
            throw new IllegalArgumentException("호스트는 챌린지를 그만둘 수 없습니다.");
        }

        // 참여 기록 삭제 (탈퇴 처리)
        participantRepository.delete(participant);
    }
}

