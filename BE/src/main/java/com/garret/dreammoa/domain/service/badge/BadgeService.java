package com.garret.dreammoa.domain.service.badge;

import com.garret.dreammoa.domain.dto.badge.response.BadgeResponseDTO;
import com.garret.dreammoa.domain.model.*;
import com.garret.dreammoa.domain.repository.BadgeRepository;
import com.garret.dreammoa.domain.repository.UserBadgeRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.file.FileService;
import com.garret.dreammoa.utils.SecurityUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final FileService fileService;

    public List<BadgeEntity> getAllBadges(){
        log.info("[BadgeService] 모든 뱃지 조회 요청");
        return badgeRepository.findAll();
    }

    @Transactional
    public void assignBadgeToUser(Long badgeId) {
        UserEntity user = securityUtil.getCurrentUser();
        log.info("[BadgeService] 사용자 {} 에게 뱃지 {} 부여 시도", user.getId(), badgeId);

        BadgeEntity badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> {
                    log.error("[BadgeService] 뱃지 {} 를 찾을 수 없음", badgeId);
                    return new IllegalArgumentException("해당 뱃지를 찾을 수 없습니다.");
                });

        boolean alreadyHasBadge = userBadgeRepository.existsByUserAndBadge(user, badge);
        if (alreadyHasBadge) {
            log.warn("[BadgeService] 사용자 {} 는 이미 뱃지 {} 를 보유 중", user.getId(), badgeId);
            throw new IllegalArgumentException("사용자는 동일한 뱃지를 여러 번 받을 수 없습니다.");
        }

        UserBadgeEntity userBadge = UserBadgeEntity.builder()
                .user(user)
                .badge(badge)
                .build();
        userBadgeRepository.save(userBadge);
        log.info("[BadgeService] 사용자 {} 에게 뱃지 {} 부여 완료", user.getId(), badgeId);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponseDTO> getUserBadges(){
        UserEntity user = securityUtil.getCurrentUser();
        log.info("[BadgeService] 사용자 {} 의 보유 뱃지 조회 요청", user.getId());
        List<UserBadgeEntity> userBadges = userBadgeRepository.findByUser(user);
        if (userBadges.isEmpty()) {
            log.warn("[BadgeService] 사용자 {} 는 보유한 뱃지가 없음", user.getId());
        }
        return userBadges.stream()
                .map(userBadge -> new BadgeResponseDTO(userBadge.getBadge()))
                .collect(Collectors.toList());
    }

    public void assignBadgeForChallenge(ChallengeEntity challenge, UserEntity user){
        // 썸네일 가져오기
        List<FileEntity> files = fileService.getFiles(challenge.getChallengeId(), FileEntity.RelatedType.CHALLENGE);
        String thumbnailUrl = (files != null && !files.isEmpty()) ? files.get(0).getFileUrl() : null;

        boolean alreadyAwarded = userBadgeRepository.existsByUserAndBadge_Name(user, challenge.getTitle());
        if(!alreadyAwarded){
            BadgeEntity badge = BadgeEntity.builder()
                    .name(challenge.getTitle())
                    .description(challenge.getTitle() + " 첼린지를 성공함")
                    .iconUrl(thumbnailUrl)
                    .build();
            BadgeEntity saveBadge = badgeRepository.save(badge);

            UserBadgeEntity userBadge = UserBadgeEntity.builder()
                    .user(user)
                    .badge(saveBadge)
                    .build();

            userBadgeRepository.save(userBadge);
        }
    }
}
