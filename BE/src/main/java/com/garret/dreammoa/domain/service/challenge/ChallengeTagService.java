package com.garret.dreammoa.domain.service.challenge;

import com.garret.dreammoa.domain.repository.ChallengeTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeTagService {

    private final ChallengeTagRepository challengeTagRepository;

    // 2️⃣ 태그 조건에 해당하는 챌린지 ID 리스트 조회
    public List<Long> getChallengeIdsByTags(List<String> tags, Pageable pageable) {
        return challengeTagRepository.findChallengeIdsByTags(tags, pageable);
    }

    public List<Long> getChallengeIdsByTags(List<String> tags) {
        return challengeTagRepository.findChallengeIdsByTags(tags);
    }

}
