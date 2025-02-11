package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.dto.challenge.requestdto.ChallengeKickRequest;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.ParticipantEntity;
import com.garret.dreammoa.domain.model.ParticipantHistoryEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.ParticipantHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantHistoryService {

    private final ParticipantHistoryRepository participantHistoryRepository;

    public void validateNotKicked(ChallengeEntity challenge, UserEntity user) {

        Optional<ParticipantHistoryEntity> kickedHistory = participantHistoryRepository.findByChallengeAndUserAndStatus(challenge, user, ParticipantHistoryEntity.Status.KICKED);
        if (kickedHistory.isPresent()) {
            String kickedByName = kickedHistory.get().getActionByUser().getName();  // 강퇴한 사람의 이름
            throw new IllegalArgumentException("강퇴된 참가자는 다시 참여할 수 없습니다. 강퇴한 사람: " + kickedByName);
        }
    }

    @Transactional
    public void createLeftHistory(UserEntity user, ChallengeEntity challenge, String reason) {

        ParticipantHistoryEntity history = ParticipantHistoryEntity.builder()
                .user(user)
                .actionByUser(user)  // 본인이 나간 경우 본인을 저장
                .challenge(challenge)
                .status(ParticipantHistoryEntity.Status.LEFT)
                .reason(reason)
                .build();
        participantHistoryRepository.save(history);
    }

    @Transactional
    public void createKickHistory(UserEntity user, ChallengeEntity challenge, ChallengeKickRequest kickData, ParticipantEntity targetParticipant) {
        ParticipantHistoryEntity history = ParticipantHistoryEntity.builder()
                .user(targetParticipant.getUser()) // 강퇴된 유저
                .actionByUser(user)  // 강퇴한 사람 (현재 방장)
                .challenge(challenge)
                .status(ParticipantHistoryEntity.Status.KICKED)
                .reason(kickData.getReason())  // 강퇴 사유
                .build();
        participantHistoryRepository.save(history);
    }
}
