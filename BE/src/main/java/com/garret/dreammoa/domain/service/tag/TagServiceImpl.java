package com.garret.dreammoa.domain.service.tag;

import com.garret.dreammoa.domain.dto.tag.responsedto.TagResponseDto;
import com.garret.dreammoa.domain.model.TagEntity;
import com.garret.dreammoa.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<TagResponseDto> getAllTags() {
        List<TagEntity> tags = tagRepository.findAll();
        return tags.stream()
                .map(TagResponseDto::new)  // DTO 변환
                .collect(Collectors.toList());
    }
}
