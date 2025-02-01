package com.garret.dreammoa.domain.service.like;

import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.model.LikeEntity;
import com.garret.dreammoa.domain.model.UserEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.LikeRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    //좋아요 누르기
    @Override
    public void addLike(Long postId, Long userId) {
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. userId=" + userId));

        // ✅ 이미 좋아요를 눌렀는지 체크 (중복 방지)
        if (likeRepository.findByBoardAndUser(board, user).isPresent()) {
            throw new IllegalStateException("이미 좋아요한 게시글입니다.");
        }

        LikeEntity like = LikeEntity.builder() //LikeEntity 객체를 생성하는 빌더 패턴 사용
                .board(board) //좋아요가 눌린 게시글을 설정
                .user(user) //좋아요를 누른 사용자를 설정
                .build(); //최종적으로 LikeEntity 객체를 설정

        likeRepository.save(like); //좋아요 저장(db에 반영)
    }

    //좋아요 취소
    @Override
    public void removeLike(Long postId, Long userId) {
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. userId=" + userId));

        LikeEntity like = likeRepository.findByBoardAndUser(board, user)
                .orElseThrow(() ->new IllegalStateException("좋아요하지 않은 게시글은 취소할 수 없습니다."));

        likeRepository.delete(like);
    }

    //좋아요 개수 조회
    @Override
    @Transactional(readOnly = true)
    public int getLikeCount(Long postId) {
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        return likeRepository.countByBoard(board);
    }

    // ✅ 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    @Override
    @Transactional(readOnly = true)
    public boolean isPostLiked(Long postId, Long userId) {
        BoardEntity board = boardRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. postId=" + postId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. userId=" + userId));

        return likeRepository.findByBoardAndUser(board, user).isPresent();
    }
}
