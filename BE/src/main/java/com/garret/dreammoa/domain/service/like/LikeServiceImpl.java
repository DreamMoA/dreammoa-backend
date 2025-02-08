package com.garret.dreammoa.domain.service.like;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.LikeEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.LikeRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {

    private final RedisTemplate<String, String> redisTemplate;
    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    //게시글 별로 좋아요 누른 userId를 저장할 때 사용할 키 접두사
    private static final String LIKE_KEY_PREFIX = "likes:";

    //좋아요 누르기
    @Override
    public void addLike(Long postId, Long userId) {
        //게시글 존재 여부 확인
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 게시글이 존재하지 않습니다. postId=" + postId));

        //사용자가 존재하는지 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자가 존재하지 않습니다. userId=" + userId));

        //Redis 키 생성 : likes:postId
        String key = LIKE_KEY_PREFIX + postId;

        //이미 좋아요한 경우 체크 -> 중복 방지
        //현재 사용자의 ID(문자열로 반환된)를 해당 Redis Set에 포함되어 있는지 확인
        Boolean alreadyLiked = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (alreadyLiked != null && alreadyLiked) { //이미 포함되어 있다면, 예외 발생
            throw new IllegalStateException("❌ 이미 좋아요한 게시글입니다.");
        }

        //Redis의 Set에 userId를 추가
        redisTemplate.opsForSet().add(key, userId.toString());
        // DB에 LikeEntity는 따로 동기화하는 로직이 있다 하더라도,
        // 실시간 반영을 위해 BoardEntity의 likeCount 컬럼도 업데이트합니다.
        boardRepository.incrementLikeCount(postId);

        log.info("✅ 게시글(postId={})에 사용자(userId={})가 좋아요 추가", postId, userId);
    }

    //좋아요 취소
    @Override
    @Transactional
    public void removeLike(Long postId, Long userId) {
        //게시글 존재 여부 확인
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 게시글이 존재하지 않습니다. postId=" + postId));

        //사용자가 존재하는지 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("❌ 사용자가 존재하지 않습니다. userId=" + userId));

        //Redis 키 생성 : 이 키를 사용해서 Redis의 Set에 저장된 좋아요 사용자 ID 목록에 접근
        String key = LIKE_KEY_PREFIX + postId;

        //Redis의 Set에서 좋아요 여부 확인
        //해당 Redis Set에 현재 사용자의 ID(문자열 형태)가 포함되어 있는지 체크
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (isMember == null || !isMember) { //좋아요를 누르지 않은 상태라면
            throw new IllegalStateException("❌ 좋아요를 누르지 않은 게시글입니다.");
        }

        //Redis의 Set에서 userId 제거
        redisTemplate.opsForSet().remove(key, userId.toString());

        //DB에서도 좋아요 기록 삭제
        likeRepository.deleteByUser_IdAndBoard_PostId(userId, postId);

        boardRepository.decrementLikeCount(postId);

        log.info("✅ 게시글(postId={})에 사용자(userId={})가 좋아요 취소", postId, userId);
    }

    //좋아요 개수 조회
    @Override
    public int getLikeCount(Long postId) {
        //Redis 키 생성
        String key = LIKE_KEY_PREFIX + postId;
        //Redis에서 지정된 key에 해당하는 Set의 요소 개수를 조회
        Long size = redisTemplate.opsForSet().size(key);
        //좋아요 개수 반환
        return (size != null) ? size.intValue() : 0;
    }

    //사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    @Override
    public boolean isPostLiked(Long postId, Long userId) {
        //Redis 키 생성
        String key = LIKE_KEY_PREFIX + postId;
        //Redis에서 좋아요 여부 확인
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        //boolean 값 반환
        return (isMember != null) ? isMember : false;
    }

    @Override
    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void syncLikesToDB() {
        //Redis 키 검색: likes:
        Set<String> keys = redisTemplate.keys(LIKE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.info("❌ Redis에 likes:* 키가 없습니다. 동기화 대상 없음");
            return;
        }

        //각 키에 대해 Redis에서 좋아요 사용자 ID 목록 조회
        for (String key : keys) {
            // 예: key = "likes:10"
            Long postId = Long.parseLong(key.replace(LIKE_KEY_PREFIX, ""));

            //Redis에서 userId 목록 가져오기
            Set<String> userIdSet = redisTemplate.opsForSet().members(key);
            if (userIdSet == null) continue;

            //DB에서 이미 좋아요가 등록된 userId 목록 가져오기
            BoardEntity board = boardRepository.findById(postId).orElse(null);
            if (board == null) {
                // 게시글이 DB에 없는 경우(삭제됨) -> Redis에서도 제거
                redisTemplate.delete(key);
                continue;
            }

            // DB에 등록된 좋아요(LikeEntity) 목록을 userId로 변환
            Set<Long> dbUserIdSet = likeRepository.findAll().stream()
                    .filter(like -> like.getBoard().getPostId().equals(postId))
                    .map(like -> like.getUser().getId())
                    .collect(Collectors.toSet());

            //Redis에는 있지만 DB에는 없는 userId -> 새로 등록
            for (String userIdStr : userIdSet) {
                Long userId = Long.parseLong(userIdStr);
                if (!dbUserIdSet.contains(userId)) {
                    // DB에 LikeEntity 등록
                    UserEntity user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        LikeEntity likeEntity = LikeEntity.builder()
                                .board(board)
                                .user(user)
                                .build();
                        likeRepository.save(likeEntity);
                    }
                }
            }

            //DB에는 있지만 Redis에는 없는 userId -> DB에서 삭제
            for (Long dbUserId : dbUserIdSet) {
                if (!userIdSet.contains(dbUserId.toString())) {
                    // 해당 LikeEntity 찾아서 삭제
                    UserEntity user = userRepository.findById(dbUserId).orElse(null);
                    if (user != null) {
                        LikeEntity likeEntity = likeRepository.findByBoardAndUser(board, user).orElse(null);
                        if (likeEntity != null) {
                            likeRepository.delete(likeEntity);
                        }
                    }
                }
            }

            log.info("게시글 postId={} 에 대한 좋아요 동기화 완료", postId);
        }
    }
}
