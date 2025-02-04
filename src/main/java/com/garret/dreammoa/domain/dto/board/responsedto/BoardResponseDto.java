package com.garret.dreammoa.domain.dto.board.responsedto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.garret.dreammoa.domain.model.BoardEntity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponseDto {

    private Long postId;
    private Long userId;
    private String userNickname;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BoardEntity.Category category; // enum
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private int likeCount;
    private int commentCount;

}
