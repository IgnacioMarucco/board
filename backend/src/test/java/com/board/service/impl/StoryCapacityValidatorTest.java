package com.board.service.impl;

import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.exception.BadRequestException;
import com.board.repository.StoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StoryCapacityValidator.
 */
@ExtendWith(MockitoExtension.class)
class StoryCapacityValidatorTest {

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private StoryCapacityValidator storyCapacityValidator;

    @Test
    @DisplayName("validateSprintCapacity should throw when capacity exceeded")
    void validateSprintCapacityShouldThrowWhenExceeded() {
        Sprint sprint = Sprint.builder().id(1L).capacityPoints(5).build();
        Story existing = Story.builder().id(10L).storyPoints(4).build();
        Story candidate = Story.builder().id(20L).storyPoints(2).build();

        when(storyRepository.findBySprint(sprint)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> storyCapacityValidator.validateSprintCapacity(sprint, candidate))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Sprint capacity exceeded");
    }
}
