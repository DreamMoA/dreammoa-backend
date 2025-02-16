package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeKickRequest;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ParticipantEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ParticipantRepository;
import com.garret.dreammoa.domain.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ParticipantHistoryService participantHistoryService;
    private final FileService fileService;

    // 방장 참가자 추가
    @Transactional
    public void addParticipant(UserEntity user, ChallengeEntity challenge, boolean isHost) {
        ParticipantEntity participant = ParticipantEntity.builder()
                .challenge(challenge)
                .user(user)
                .isHost(isHost)
                .isActive(false)
                .build();

        participantRepository.save(participant);
    }
    // 방장인지 검증
    public boolean isHost(UserEntity user, ChallengeEntity challenge) {
        return participantRepository.existsByUserAndChallengeAndIsHost(user, challenge, true);
    }
    // 일반 참가자 추가
    @Transactional
    public void addParticipant(ChallengeEntity challenge, UserEntity user) {
        // 강퇴 이력 조회
        participantHistoryService.validateNotKicked(challenge, user);

        // 최대참여자 수 넘었는지 조회
        long maxParticipants = challenge.getMaxParticipants();
        long currentParticipants = participantRepository.countByChallenge(challenge);
        if (currentParticipants >= maxParticipants) {
            throw new IllegalStateException("챌린지 참여 인원이 가득 찼습니다.");
        }
        // 방에 이미 참여하고있는지 확인
        boolean alreadyJoined = participantRepository.existsByChallengeAndUser(challenge, user);
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
    }
    // 참여자 활성화
    @Transactional
    public void activateParticipant(UserEntity user, ChallengeEntity challenge) {
        ParticipantEntity participant = participantRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여자가 아닙니다."));

        participant = ParticipantEntity.builder()
                .participantId(participant.getParticipantId())
                .challenge(participant.getChallenge())
                .user(participant.getUser())
                .isHost(participant.getIsHost())
                .isActive(true)
                .build();
        participantRepository.save(participant);
    }
    // 참여자토큰 저장
    @Transactional
    public void saveParticipantToken(UserEntity user, ChallengeEntity challenge, String token) {
        ParticipantEntity participant = participantRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여자가 아닙니다."));

        participant = ParticipantEntity.builder()
                .participantId(participant.getParticipantId())
                .challenge(participant.getChallenge())
                .user(participant.getUser())
                .isHost(participant.getIsHost())
                .isActive(participant.getIsActive())
                .sessionToken(token)
                .build();

        participantRepository.save(participant);
    }

    @Transactional
    public void leaveParticipant(UserEntity user, ChallengeEntity challenge) {
        ParticipantEntity participant = participantRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여자가 아닙니다."));

        // 참여자 수가 체크 후 삭제
        long participantCount = participantRepository.countByChallenge(challenge);
        participantRepository.delete(participant);
        if(participantCount == 1){
            fileService.deleteThumbnail(challenge.getChallengeId());
        }
        // 참가자 수가 2명 이상이고 방장이 나갔다면
        if (participantCount >= 2 && participant.getIsHost()) {
            Optional<ParticipantEntity> newHost = participantRepository.findTopByChallengeOrderByJoinedAtAsc(challenge);
            newHost.ifPresent(newHostParticipant -> {
                newHostParticipant.setIsHost(true);
                participantRepository.save(newHostParticipant);
            });
        }
    }

    // 참가자의 활성 상태 비활성화
    @Transactional
    public void deactivateParticipant(UserEntity user, ChallengeEntity challenge) {
        ParticipantEntity participant = participantRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참가자가 아닙니다."));

        participant = ParticipantEntity.builder()
                .participantId(participant.getParticipantId())
                .challenge(participant.getChallenge())
                .user(participant.getUser())
                .isHost(participant.getIsHost())
                .isActive(false)
                .sessionToken(null)
                .build();

        participantRepository.save(participant);
    }

    // 활성 참가자 수 조회
    public long countActiveParticipants(ChallengeEntity challenge) {
        return participantRepository.countByChallengeAndIsActiveTrue(challenge);
    }

    public Optional<ParticipantEntity> getCurrentHost(UserEntity user, ChallengeEntity challenge){
        return Optional.ofNullable(participantRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new IllegalArgumentException("참여자가 아닙니다.")));
    }

    public Optional<ParticipantEntity> getParticipant(Long newHostID){
        return Optional.ofNullable(participantRepository.findById(newHostID)
                .orElseThrow(() -> new IllegalArgumentException("챌린지에 참여하지 않았습니다.")));
    }

    @Transactional
    public void delegateHost(ParticipantEntity currentHost, ParticipantEntity newHost) {

        // 기존 방장 권한 해제
        currentHost.setIsHost(false);
        // 새로운 방장 권한 부여
        newHost.setIsHost(true);

        // 변경 사항 저장
        participantRepository.save(currentHost);
        participantRepository.save(newHost);
    }

    @Transactional
    public void kickParticipant(ChallengeKickRequest kickData) {
        ParticipantEntity targetParticipant = participantRepository.findById(kickData.getKickedUserId())
                .orElseThrow(() -> new IllegalArgumentException("강퇴할 유저가 챌린지에 참여하지 않았습니다."));
        participantRepository.delete(targetParticipant);
    }


    public Boolean existsByChallengeAndUser(ChallengeEntity challenge, UserEntity user) {
        return participantRepository.existsByChallengeAndUser(challenge, user);
    }

    public List<ParticipantEntity> findByUser(UserEntity currentUser) {
        return participantRepository.findByUser(currentUser);
    }

}
