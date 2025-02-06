package com.garret.dreammoa.domain.service.viewcount;

import com.garret.dreammoa.domain.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewCountRedisServiceImpl implements ViewCountService {

    private static final Logger logger = LoggerFactory.getLogger(ViewCountRedisServiceImpl.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final BoardRepository boardRepository; //mysql과 동기화할 때 사용

    private static final String VIEW_COUNT_KEY = "viewCount";

    //게시글 조회 시 Redis에서 조회수 증가
    @Override
    public void increaseViewCount(Long postId) {
        //Redis 키 구성
        String key = VIEW_COUNT_KEY + postId;
        //조회수 증가
        redisTemplate.opsForValue().increment(key);

        //증가된 조회수 값을 가져옴
        String updatedCount = redisTemplate.opsForValue().get(key);

        //로그출력
        System.out.println("increase함수 : Redis 조회수 증가 - postId: " + postId + ", 현재 조회수: " + updatedCount);
    }

    //Redis에서 조회수 가져오기
    @Override
    public int getViewCount(Long postId) {
        //캐시 키 생성
        String key = VIEW_COUNT_KEY + postId;
        //Redis에서 조회수 값 조회
        String count = redisTemplate.opsForValue().get(key);

        logger.info("Redis 조회수 확인 - postId: {}, 조회수: {}", postId, count);

        //조회수 값 반환
        return (count != null) ? Integer.parseInt(count) : 0;
    }

    // 1분마다 Redis 데이터를 Mysql에 동기화
    @Override
    @Scheduled(fixedRate = 60000) //1분마다 실행
    public void syncViewCountToDB() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY + "*");
        if(keys == null || keys.isEmpty()) {
            System.out.println("❌ Redis에 저장된 조회수가 없음");
            return;
        }

        for(String key : keys) {
            Long postId = Long.parseLong(key.replace(VIEW_COUNT_KEY, ""));
            String count = redisTemplate.opsForValue().get(key);

            System.out.println("동기화 대상 postId: " + postId + ", Redis 조회수: " + count);

            if(count != null) {
                Long viewCount = Long.parseLong(count);
                boardRepository.updateViewCount(postId, viewCount);

                System.out.println("MySQL 업데이트 실행 - postId: " + postId + ", 새로운 조회수: " + viewCount);
            }
        }
    }

}
