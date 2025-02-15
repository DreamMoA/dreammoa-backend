package com.garret.dreammoa.domain.service.user;

import com.garret.dreammoa.domain.dto.main.response.TotalScreenTimeResponseDto;
import com.garret.dreammoa.domain.repository.ChallengeLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class TotalScreenTimeService {
    private final ChallengeLogRepository challengeLogRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOTAL_SCREEN_TIME_KEY = "totalScreenTime";

    @Scheduled(fixedRate = 30000) // 30초
    public void calculateAndStoreTotalScreenTime(){
        Integer totalScreenTime = challengeLogRepository.findTotalScreenTime();
        if(totalScreenTime == null) {
            totalScreenTime = 0;
        }
        redisTemplate.opsForValue().set(TOTAL_SCREEN_TIME_KEY, totalScreenTime.toString());
        log.info("총 화면 켠 시간 계산 완료 및 Redis에 저장됨: {}", totalScreenTime);
    }

    // Redis에 저장된 총 화면 켠 시간 값을 반환
    public TotalScreenTimeResponseDto getTotalScreenTimeDto() {
        String totalScreenTime = redisTemplate.opsForValue().get(TOTAL_SCREEN_TIME_KEY);
        if(totalScreenTime == null) {
            totalScreenTime = "0";
        }
        return TotalScreenTimeResponseDto.builder().totalScreenTime(totalScreenTime).build();
    }
}
