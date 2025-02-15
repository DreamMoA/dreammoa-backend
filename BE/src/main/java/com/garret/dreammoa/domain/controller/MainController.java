package com.garret.dreammoa.domain.controller;

import com.garret.dreammoa.domain.dto.board.responsedto.MainBoardResponseDto;
import com.garret.dreammoa.domain.dto.challenge.responsedto.EndingSoonChallengeDto;
import com.garret.dreammoa.domain.dto.main.response.TotalScreenTimeResponseDto;
import com.garret.dreammoa.domain.model.BoardEntity;
import com.garret.dreammoa.domain.repository.BoardRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.user.TotalScreenTimeService;
import com.garret.dreammoa.domain.service.challenge.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final TotalScreenTimeService totalScreenTimeService;
    private final ChallengeService challengeService;


    @GetMapping("/total-screen-time")
   public ResponseEntity<TotalScreenTimeResponseDto> getTotalScreenTime(){
        return ResponseEntity.ok(totalScreenTimeService.getTotalScreenTimeDto());
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

    @GetMapping("/ending-soon")
    public ResponseEntity<List<EndingSoonChallengeDto>> getEndingSoonChallenges() {
        List<EndingSoonChallengeDto> dtos = challengeService.getEndingSoonChallenges();
        return ResponseEntity.ok(dtos);
    }

}
