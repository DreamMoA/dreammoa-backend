package com.garret.dreammoa.domain.controller;

import com.garret.dreammoa.domain.dto.board.responsedto.MainBoardResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;


    @GetMapping("/")
    public String mainP(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth= iter.next();
        String role = auth.getAuthority();

        return "main Controller : " + name + ",  " + role;
    }

    @GetMapping("/top-viewed")
    public ResponseEntity<List<MainBoardResponseDto>> getTopViewedPosts() {
        // BoardRepository를 통해 조회순이 높은 상위 20개 게시글 조회
        List<BoardEntity> topPosts = boardRepository.findTop20ByOrderByViewCountDesc();

        // BoardEntity -> BoardResponseDto 변환 (작성자 정보 포함)
        List<MainBoardResponseDto> responseList = topPosts.stream()
                .map(entity -> MainBoardResponseDto.builder()
                        .postId(entity.getPostId())
                        .title(entity.getTitle())
                        .content(entity.getContent())
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .viewCount(entity.getViewCount())
                        .likeCount(entity.getLikeCount())
                        .commentCount(entity.getCommentCount())
                        // 작성자 정보 매핑
                        .userName(entity.getUser().getName())
                        .userNickname(entity.getUser().getNickname())
                        .userProfilePicture(
                                entity.getUser().getProfileImage() != null
                                        ? entity.getUser().getProfileImage().getFileUrl()
                                        : null)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/random-determinations")
    public ResponseEntity<List<String>> getRandomDeterminations() {
        List<String> determinations = userRepository.findRandomDeterminations();
        return ResponseEntity.ok(determinations);
    }

}
