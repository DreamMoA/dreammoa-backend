package com.garret.dreammoa.domain.dto.comment.requestdto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//댓글 작성 및 수정을 처리하기 위한 dto
public class CommentRequestDto {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long parentCommentId;
}
