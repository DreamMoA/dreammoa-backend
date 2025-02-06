package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeCreateRequest;
import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeUpdateRequest;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeResponse;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.TagEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ChallengeRepository;
import com.garret.dreammoa.domain.repository.TagRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.FileService;
import com.garret.dreammoa.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final SecurityUtil securityUtil;
    private final FileService fileService;
    private final TagRepository tagRepository;

    public ResponseEntity<ChallengeResponse> createChallenge(
            ChallengeCreateRequest request,
            MultipartFile thumbnail) throws Exception {
        UserEntity user = securityUtil.getCurrentUser();
        log.info("현재 유저: {}", user.getName());

        // status 처리
        String status;
        LocalDate today = LocalDate.now();
        LocalDate startDate = request.getStartDate().toLocalDate();
        LocalDate expireDate = request.getExpireDate().toLocalDate();

        if (startDate.isAfter(today)) {
            status = "대기중";  // 시작일이 미래일 경우
        } else if (startDate.isEqual(today) && expireDate.isAfter(today)) {
            status = "진행중";  // 오늘이 시작일이고 종료일이 미래일 경우
        } else {
            status = "종료";  // 종료일이 과거일 경우
        }

        // 챌린지 엔터티 생성
        ChallengeEntity challenge = ChallengeEntity.builder()
                .host(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .isPrivate(request.getIsPrivate())
                .startDate(request.getStartDate())
                .expireDate(request.getExpireDate())
                .status(status)
                .standard(request.getStandard())
                .isActive(false)
                .build();

        // 태그 저장
        for (String tagName : request.getTags()) {
            TagEntity tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(TagEntity.builder().tagName(tagName).build()));
            challenge.addTag(tag);
        }
        ChallengeEntity savedChallenge = challengeRepository.save(challenge);
        Long challengeId = savedChallenge.getChallengeId();

        // 사진 처리
        // 썸네일 처리: null 기본 이미지 설정
        String thumbnailURL;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            log.info("파일 업로드 중: {}", thumbnail.getOriginalFilename());
            thumbnailURL = fileService.saveFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE).getFileUrl(); // 실제 파일 저장
        } else {
            log.info("썸네일이 없으므로 기본 이미지 사용");
            thumbnailURL = "기본 이미지 URL"; // 기본 이미지 URL
        }

        return ResponseEntity.ok(ChallengeResponse.fromEntity(thumbnailURL, challenge, "챌린지가 성공적으로 생성되었습니다."));
    }

    public ResponseEntity<ChallengeResponse> updateChallenge(ChallengeUpdateRequest updateData, MultipartFile thumbnail) throws Exception {

//        UserEntity user = securityUtil.getCurrentUser();
//        Long challengeId = updateData.getChallengeId();
//        ChallengeEntity challenge = challengeRepository.findById(challengeId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));
//
//        if (!challenge.getHost().getId().equals(user.getId())) {
//            throw new IllegalArgumentException("챌린지를 수정할 권한이 없습니다.");
//        }
//        challengeRepository.
//        challenge.update(updateData.getTitle(), updateData.getDescription(), updateData.getMaxParticipants(),
//                updateData.getIsPrivate(), updateData.getStartDate(), updateData.getExpireDate(),
//                updateData.getStandard());
//
//        if (thumbnail != null) {
//            FileEntity file = fileService.saveFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE);
//            file.getFileUrl();
//            fileService.
//            challenge.updateThumbnail(newThumbnailURL);
//        }
        return null;
    }
}

