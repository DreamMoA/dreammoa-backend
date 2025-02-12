package com.garret.dreammoa.domain.dto.board.requestdto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class BoardRequestDto {

    private String category;     // "질문" or "자유"
    private String title;
    private String content;
    private List<Long> fileIds;
    private List<String> tags;
}
