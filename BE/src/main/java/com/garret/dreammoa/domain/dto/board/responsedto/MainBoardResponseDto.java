package com.garret.dreammoa.domain.dto.board.responsedto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class MainBoardResponseDto {
    private Long postId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long viewCount;
    private int likeCount;
    private int commentCount;

    // 추가된 작성자 정보 필드
    private String userName;
    private String userNickname;
    private String userProfilePicture;
}
