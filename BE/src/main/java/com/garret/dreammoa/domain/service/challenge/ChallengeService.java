package com.garret.dreammoa.domain.service.challenge;

import com.amazonaws.services.s3.AmazonS3Client;
import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeCreateRequest;
import com.garret.dreammoa.domain.dto.challenge.responsedto.ChallengeCreateResponse;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.FileEntity;
import com.garret.dreammoa.domain.model.TagEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ChallengeRepository;
import com.garret.dreammoa.domain.repository.TagRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.FileService;
import com.garret.dreammoa.utils.JwtUtil;
import com.garret.dreammoa.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final SecurityUtil securityUtil;
    private final FileService fileService;
    private final TagRepository tagRepository;

    // ToDo: 태그 추가해야함
    public ResponseEntity<ChallengeCreateResponse> createChallenge(
            ChallengeCreateRequest reqeustData,
            MultipartFile thumbnail) throws Exception {
        System.out.println("서비스");
        UserEntity user = securityUtil.getCurrentUser();

        // TODO: status 필드를 어떻게 처리할지 -> startDate 기준으로 판단 하고 빌더패턴에 반영
        // 챌린지 엔터티 생성
        ChallengeEntity challenge = ChallengeEntity.builder()
                .host(user)
                .title(reqeustData.getTitle())
                .description(reqeustData.getDescription())
                .maxParticipants(reqeustData.getMaxParticipants())
                .isPrivate(reqeustData.getIsPrivate())
                .startDate(reqeustData.getStartDate())
                .expireDate(reqeustData.getExpireDate())
                .standard(reqeustData.getStandard())
                .isActive(false)
                .build();

        // 태그 저장
        for (String tagName : reqeustData.getTags()) {
            TagEntity tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(TagEntity.builder().tagName(tagName).build()));
            challenge.addTag(tag);
        }

        ChallengeEntity savedChallenge = challengeRepository.save(challenge);
        Long challengeId = savedChallenge.getChallengeId();
        FileEntity fileEntity = fileService.saveFile(thumbnail, challengeId, FileEntity.RelatedType.CHALLENGE);
        String thumbnailURL = fileEntity.getFileUrl();
        // 사진 처리

        return ResponseEntity.ok(ChallengeCreateResponse.fromEntity(thumbnailURL, challenge));
    }
}

