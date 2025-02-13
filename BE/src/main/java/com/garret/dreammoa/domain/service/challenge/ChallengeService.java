package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.*;
import com.garret.dreammoa.domain.dto.challenge.responsedto.*;
import com.garret.dreammoa.domain.dto.dashboard.request.StudyHistoryDto;
import com.garret.dreammoa.domain.dto.dashboard.response.DashboardChallengeDto;
import com.garret.dreammoa.domain.model.*;
import com.garret.dreammoa.domain.repository.*;
import com.garret.dreammoa.domain.service.FileService;
import com.garret.dreammoa.domain.service.tag.TagServiceImpl;
import com.garret.dreammoa.utils.EncryptionUtil;
import com.garret.dreammoa.utils.JwtUtil;
import com.garret.dreammoa.utils.SecurityUtil;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import com.garret.dreammoa.domain.model.FileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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
    private final EncryptionUtil encryptionUtil;
    private final ChallengeTagService challengeTagService;
    private final JwtUtil jwtUtil;
    private final ChallengeLogRepository challengeLogRepository;
    private final FileRepository fileRepository;


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
            return ResponseEntity.ok(ChallengeResponse.fromEntity(challenge, "챌린지가 성공적으로 생성되었습니다."));
        }
    }

    @Transactional
    public ResponseEntity<ChallengeResponse> updateChallenge(ChallengeUpdateRequest request, MultipartFile thumbnail) throws Exception {
        UserEntity user = securityUtil.getCurrentUser();
        Long challengeId = request.getChallengeId();
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        // 방장 여부 확인
        if (!participantService.isHost(user, challenge)) {
            throw new IllegalArgumentException("챌린지를 수정할 권한이 없습니다.");
        }
        // 챌린지 정보 업데이트
        challenge.update(request.getTitle(), request.getDescription(), request.getMaxParticipants(),
                request.getIsPrivate(), request.getStartDate(), request.getExpireDate(),
                request.getStandard());

        // 태그 업데이트
        tagService.updateTags(challenge, request.getTags());
//        challengeRepository.save(challenge); 필요 없음!
        System.out.println("thumbnail = " + thumbnail);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            FileEntity file = fileService.updateFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE);
            String newThumbnail = file.getFileUrl();
            return ResponseEntity.ok(ChallengeResponse.fromEntity(newThumbnail, challenge, "챌린지가 성공적으로 수정되었습니다."));
        }
        return ResponseEntity.ok(ChallengeResponse.fromEntity(challenge, "챌린지가 성공적으로 수정되었습니다."));
    }

    /**
     * 현재 로그인한 사용자가 참여 중인 특정 챌린지의 상세 정보를 DTO로 반환
     *
     * @param challengeId 조회할 챌린지 id
     * @return MyChallengeDetailResponseDto
     * @throws IllegalArgumentException 사용자가 해당 챌린지에 참여 중이 아닐 경우
     */
    public ResponseEntity<ChallengeResponse> getChallengeInfo(Long challengeId) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 썸네일 확인
        List<FileEntity> files = fileService.getFiles(challenge.getChallengeId(), FileEntity.RelatedType.CHALLENGE);
        String thumbnail = (files != null && !files.isEmpty()) ? files.get(0).getFileUrl() : null;

        System.out.println("authentication = " + authentication);
        System.out.println("authentication.isAuthenticated() = " + authentication.isAuthenticated());

        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken)
            return ResponseEntity.ok(ChallengeResponse.fromEntity(thumbnail, challenge, "참여하려면 로그인이 필요합니다."));
        UserEntity user = securityUtil.getCurrentUser();
        participantHistoryService.validateNotKicked(challenge, user); //강퇴 이력 조회
        // 참가 이력 조회
        boolean isParticipant = participantService.existsByChallengeAndUser(challenge, user);
        if (isParticipant) {
            // 이미 참가 중이면 엔터 챌린지 가능 정보를 제공
            return ResponseEntity.ok(ChallengeResponse.fromEntity(thumbnail, challenge, "이미 참가 중입니다. 엔터챌린지 가능합니다."));
        } else {
            // 참가 중이 아니면 조인 챌린지 가능 정보를 제공
            return ResponseEntity.ok(ChallengeResponse.fromEntity(thumbnail, challenge, "아직 참가하지 않았습니다. 조인챌린지 가능합니다."));
        }
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
        participantHistoryService.createLeftHistory(user, challenge, "본인이 나가기 선택");

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
        if(Objects.isNull(challenge.getSessionId())){
            challenge.setSessionId(sessionId);
            challengeRepository.save(challenge);
        }

        // ✅ 연결 토큰 생성
        String token = openViduService.createConnection(sessionId, Map.of());

        // ✅ 참가자에게 토큰 저장
        participantService.saveParticipantToken(user, challenge, token);
        // ✅ 기존 학습 로그 조회
        System.out.println("🔍 recordAt 값: " + loadDate.getRecordAt());
        Optional<ChallengeLogEntity> existingLog = challengeLogService.loadStudyLog(user, challenge, loadDate.getRecordAt());

        if (existingLog.isPresent()) {
            System.out.println("✅ 학습 기록 존재: " + existingLog.get().getRecordAt());
        } else {
            System.out.println("❌ 학습 기록 없음!");
        }
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

    @Transactional
    public List<MyChallengeResponseDto> getMyChallenges() {

        UserEntity currentUser = securityUtil.getCurrentUser();

        //사용자가 참여한 ParticipantEntity 목록 조회
        List<ParticipantEntity> participations = participantService.findByUser(currentUser);

        //각 참여 내역에서 챌린지 정보를 추출하여 DTO로 변환
        return participations.stream()
                .map(ParticipantEntity::getChallenge)
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 월별 공부 히스토리 조회
     * JWT 토큰에서 사용자 ID를 추출한 후,
     * 지정한 연도와 월의 시작일~종료일 사이의 기록을 조회합니다.
     */
    public List<StudyHistoryDto> getMonthlyStudyHistory(String accessToken, int year, int month) {
        if (!jwtUtil.validateToken(accessToken)) {
            throw new RuntimeException("유효하지 않은 Access Token입니다.");
        }
        Long userId = jwtUtil.getUserIdFromToken(accessToken);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<ChallengeLogEntity> logs = challengeLogRepository.findByUser_IdAndRecordAtBetween(userId, startDate, endDate);

        return logs.stream().map(log -> {
            Long challengeId = log.getChallenge().getChallengeId();
            String challengeTitle = log.getChallenge().getTitle();

            // 챌린지 썸네일 조회
            Optional<FileEntity> thumbnailFile = fileRepository.findByRelatedIdAndRelatedType(challengeId, FileEntity.RelatedType.CHALLENGE)
                    .stream().findFirst();
            String thumbnailUrl = thumbnailFile.map(FileEntity::getFileUrl).orElse(null);

            return StudyHistoryDto.builder()
                    .challengeLogId(log.getId())
                    .challengeId(challengeId)
                    .challengeTitle(challengeTitle)
                    .recordAt(log.getRecordAt())
                    .pureStudyTime(log.getPureStudyTime())
                    .screenTime(log.getScreenTime())
                    .isSuccess(log.getIsSuccess())
                    .thumbnailUrl(thumbnailUrl) // 챌린지 썸네일 추가
                    .build();
        }).collect(Collectors.toList());
    }

    public List<EndingSoonChallengeDto> getEndingSoonChallenges() {
        LocalDateTime now = LocalDateTime.now();
        List<ChallengeEntity> challenges = challengeRepository.findTop20ByStartDateAfterOrderByStartDateAsc(now);

        return challenges.stream().map(challenge -> {
            // 남은 일수 계산 (소수점 이하는 버림)
            long remainingDays = Duration.between(now, challenge.getStartDate()).toDays();

            // 해당 챌린지의 썸네일 조회
            List<FileEntity> files = fileService.getFiles(challenge.getChallengeId(), FileEntity.RelatedType.CHALLENGE);
            String thumbnail = (files != null && !files.isEmpty()) ? files.get(0).getFileUrl() : null;

            return EndingSoonChallengeDto.builder()
                    .challengeId(challenge.getChallengeId())
                    .title(challenge.getTitle())
                    .thumbnail(thumbnail)
                    .remainingDays(remainingDays)
                    .build();
        }).collect(Collectors.toList());
    }

    public ChallengeEntity getChallengeById(Long challengeId) {
        return challengeRepository.findById(challengeId).orElse(null);
    }

    public String generateInviteUrl(Long challengeId) throws Exception {
        ChallengeEntity challenge = getChallengeById(challengeId);
        if (challenge == null) {
            throw new IllegalArgumentException("해당 첼린지를 찾을 수 없습니다.");
        }

        // 1. 첼린지 ID 암호화 및 URL-safe Base 64 인코딩
        String encryptedId = encryptionUtil.encrypt(String.valueOf(challengeId));
        String encodedId = Base64.getUrlEncoder().encodeToString(encryptedId.getBytes());

        return "http://localhost:5173/challenges/invite/accept?encryptedId=" + encodedId;

    }


    // 초대 URL 수락 -> 첼린지 상세조회
    public ResponseEntity<?> acceptInvite(String encodedId) throws Exception {

        // 1. Base64 디코딩 후 체린지 ID 문자열 추출
        String encryptedId = new String(Base64.getUrlDecoder().decode(encodedId));

        // 2. 복호화를 통해 원래 첼린지 ID 문자열 추출
        String challengeIdStr = encryptionUtil.decrypt(encryptedId);
        Long challengeId = Long.parseLong(challengeIdStr);

        // 3. 추출한 챌린지 ID로 기존 상세 조회 호출
        return getChallengeInfo(challengeId);
    }

    public ResponseEntity<List<MyChallengeResponseDto>> getTagChallenges(String tags) {

        // 페이징 설정
        Pageable pageable = PageRequest.of(0, 8);

        List<String> tagList = (tags == null || tags.isBlank()) ? Collections.emptyList() :
                Arrays.stream(tags.split("\\s*,\\s*"))
                        .collect(Collectors.toList());

        // 태그 필터링할 챌린지 ID 가져오기
        List<Long> challengeIds = tagList.isEmpty() ?
                challengeRepository.findAllChallengeIds(pageable) :  // 태그 없을 때 전체 조회
                challengeTagService.getChallengeIdsByTags(tagList, pageable);

        //
        List<ChallengeEntity> tagChallenges = challengeRepository.findTagChallenges(challengeIds);

        tagChallenges.sort(Comparator.comparing(c -> challengeIds.indexOf(c.getChallengeId())));

        return ResponseEntity.ok(tagChallenges.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList()));
    }

    public ResponseEntity<SearchChallengeResponseDto> searchChallenges(String tags, String keyword) {

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 8);

        List<String> tagList = (tags == null || tags.isBlank()) ? Collections.emptyList() :
                Arrays.stream(tags.split("\\s*,\\s*"))
                        .collect(Collectors.toList());
        // 태그 필터링할 챌린지 ID 가져오기
        List<Long> challengeIds = tagList.isEmpty() ?
                challengeRepository.findAllChallengeIds(pageable) :  // 태그 없을 때 전체 조회
                challengeTagService.getChallengeIdsByTags(tagList, pageable);

        // ⏳ 진행 중 (startDate ~ expireDate 사이 + 참가 가능)
        Page<MyChallengeResponseDto> runningChallenges = challengeRepository
                .findRunningChallenges(challengeIds, keyword, now, pageable)
                .map(this::toResponseDto);

        // 📢 모집 중 (startDate 이전 + 참가 가능)
        Page<MyChallengeResponseDto> recruitingChallenges = challengeRepository
                .findRecruitingChallenges(challengeIds, keyword, now, pageable)
                .map(this::toResponseDto);

        // 🌟 인기 챌린지 (참가자 많은 순)
        Page<MyChallengeResponseDto> popularChallenges = challengeRepository
                .findPopularChallenges(challengeIds, keyword, pageable)
                .map(this::toResponseDto);

        return ResponseEntity.ok(SearchChallengeResponseDto.builder()
                .recruitingChallenges(recruitingChallenges.getContent())
                .runningChallenges(runningChallenges.getContent())
                .popularChallenges(popularChallenges.getContent())
                .build());
    }

    public ResponseEntity<PagedChallengeResponseDto<MyChallengeResponseDto>> getAllChallenges(String tags, String keyword, int page) {

        Pageable pageable = PageRequest.of(page, 8);

        // tags가 null 또는 빈 문자열이면 빈 리스트 처리
        List<String> tagList = (tags == null || tags.isBlank()) ? Collections.emptyList() :
                Arrays.stream(tags.split("\\s*,\\s*"))
                        .collect(Collectors.toList());
        System.out.println("tags = " + tags);
        System.out.println("keyword = " + keyword);
        // 태그 필터링할 챌린지 ID 가져오기
        List<Long> challengeIds = tagList.isEmpty() ?
                challengeRepository.findAllChallengeIds() :  // 태그 없을 때 전체 조회
                challengeTagService.getChallengeIdsByTags(tagList);
        System.out.println("challengeIds = " + challengeIds);
        Page<ChallengeEntity> challengePage = challengeRepository.findPopularChallenges(challengeIds, keyword, pageable);

        List<MyChallengeResponseDto> challengeList = challengePage.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        PagedChallengeResponseDto<MyChallengeResponseDto> pagedResponse = toPagedResponse(challengePage, challengeList);
        return ResponseEntity.ok(pagedResponse);
    }

    private MyChallengeResponseDto toResponseDto(ChallengeEntity challenge) {
        List<String> tagNames = Optional.ofNullable(challenge.getChallengeTags())
                .orElse(Collections.emptyList()) // ✅ 태그 리스트가 없으면 빈 리스트 반환
                .stream()
                .map(challengeTag -> challengeTag.getTag().getTagName())
                .collect(Collectors.toList());

        List<FileEntity> files = fileService.getFiles(challenge.getChallengeId(), FileEntity.RelatedType.CHALLENGE);
        String thumbnailUrl = files.isEmpty() ? null : files.get(0).getFileUrl();

        return MyChallengeResponseDto.builder()
                .challengeId(challenge.getChallengeId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .startDate(challenge.getStartDate())
                .expireDate(challenge.getExpireDate())
                .isActive(challenge.getIsActive())
                .tags(tagNames)
                .thumbnail(thumbnailUrl)
                .build();
    }

    // ✅ Page 객체를 JSON으로 변환해주는 메서드
    private PagedChallengeResponseDto<MyChallengeResponseDto> toPagedResponse(Page<ChallengeEntity> page, List<MyChallengeResponseDto> list) {
        return PagedChallengeResponseDto.<MyChallengeResponseDto>builder()
                .content(list)
                .currentPage(page.getNumber()) // 현재 페이지 번호
                .totalPages(page.getTotalPages()) // 전체 페이지 수
                .totalElements(page.getTotalElements()) // 전체 데이터 개수
                .isLastPage(page.isLast())
                .build();
    }


    public List<DashboardChallengeDto> getMonthlyStudyRanking(int year, int month) {
        // 현재 로그인한 사용자 조회
        var currentUser = securityUtil.getCurrentUser();

        // 입력한 연/월에 해당하는 시작일과 종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        // 사용자가 참여한 챌린지 목록 조회 (참여 내역)
        List<ParticipantEntity> participations = participantService.findByUser(currentUser);

        List<DashboardChallengeDto> dashboardList = new ArrayList<>();

        // 각 참여 챌린지마다 해당 월에 기록된 학습 로그를 집계
        for (ParticipantEntity participation : participations) {
            ChallengeEntity challenge = participation.getChallenge();
            Long challengeId = challenge.getChallengeId();

            // 해당 챌린지에 대한 학습 로그 (현재 사용자, 지정 기간)
            List<ChallengeLogEntity> logs = challengeLogRepository
                    .findByUser_IdAndChallenge_ChallengeIdAndRecordAtBetween(
                            currentUser.getId(), challengeId, startDate, endDate);

            int totalScreenTime = logs.stream()
                    .mapToInt(log -> log.getScreenTime() != null ? log.getScreenTime() : 0)
                    .sum();
            int totalPureStudyTime = logs.stream()
                    .mapToInt(log -> log.getPureStudyTime() != null ? log.getPureStudyTime() : 0)
                    .sum();

            // 챌린지 썸네일 조회
            List<FileEntity> files = fileService.getFiles(challengeId, FileEntity.RelatedType.CHALLENGE);
            String thumbnailUrl = (files != null && !files.isEmpty()) ? files.get(0).getFileUrl() : null;

            DashboardChallengeDto dto = DashboardChallengeDto.builder()
                    .challengeId(challengeId)
                    .title(challenge.getTitle())
                    .thumbnailUrl(thumbnailUrl)
                    .totalScreenTime(totalScreenTime)
                    .totalPureStudyTime(totalPureStudyTime)
                    .build();

            dashboardList.add(dto);
        }

        // 총 화면 켠 시간 기준 내림차순 정렬
        dashboardList.sort(Comparator.comparing(DashboardChallengeDto::getTotalScreenTime).reversed());

        // 상위 4개만 반환 (리스트 크기가 4 이상인 경우에만)
        if (dashboardList.size() > 4) {
            dashboardList = dashboardList.subList(0, 4);
        }

        return dashboardList;
    }
}



