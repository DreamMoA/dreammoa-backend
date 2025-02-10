package com.garret.dreammoa.domain.service.tag;

import com.garret.dreammoa.domain.dto.tag.responsedto.TagResponseDto;
import com.garret.dreammoa.domain.model.ChallengeEntity;
import com.garret.dreammoa.domain.model.TagEntity;
import com.garret.dreammoa.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Override
    @Transactional
    public List<TagEntity> getOrCreateTags(List<String> tagNames) {
        List<TagEntity> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            TagEntity tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(TagEntity.builder().tagName(tagName).build()));
            tags.add(tag);
        }
        return tags;
    }

    @Override
    @Transactional
    public void updateTags(ChallengeEntity challenge, List<String> newTagNames) {
        // 기존 태그 가져오기
        List<String> existingTags = challenge.getChallengeTags().stream()
                .map(challengeTag -> challengeTag.getTag().getTagName())
                .toList();

        // 삭제해야 할 태그
        challenge.getChallengeTags().removeIf(challengeTag ->
                !newTagNames.contains(challengeTag.getTag().getTagName())
        );
        // 추가해야 할 태그
        for (String tagName : newTagNames) {
            if (!existingTags.contains(tagName)) {
                TagEntity tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> tagRepository.save(TagEntity.builder().tagName(tagName).build()));
                challenge.addTag(tag);
            }
        }
    }
}
