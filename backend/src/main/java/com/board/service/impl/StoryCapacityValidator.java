package com.board.service.impl;

import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates sprint capacity against story points.
 */
@Component
@RequiredArgsConstructor
public class StoryCapacityValidator {

    private final StoryRepository storyRepository;

    void validateSprintCapacity(Sprint sprint, Story story) {
        Integer capacityPoints = sprint.getCapacityPoints();
        if (capacityPoints == null || capacityPoints <= 0) {
            return;
        }
        int totalPoints = storyRepository.findBySprint(sprint).stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .map(Story::getStoryPoints)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();
        int storyPoints = story.getStoryPoints() != null ? story.getStoryPoints() : 0;
        if (totalPoints + storyPoints > capacityPoints) {
            throw new com.board.exception.BadRequestException("Sprint capacity exceeded");
        }
    }
}
