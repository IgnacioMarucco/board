package com.board.service.impl;

import com.board.entity.BoardColumn;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.StoryStatus;
import com.board.service.ActivityLogService;
import com.board.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for StoryActivityHelper.
 */
@ExtendWith(MockitoExtension.class)
class StoryActivityHelperTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private StoryActivityHelper storyActivityHelper;

    @Test
    @DisplayName("recordChanges should log changes and notify assignee")
    void recordChangesShouldLogAndNotify() {
        Project project = Project.builder().id(1L).key("TEST").name("Test").build();
        User actor = User.builder().id(10L).email("actor@test.com").username("actor").build();
        User oldAssignee = User.builder().id(11L).email("old@test.com").username("old").build();
        User newAssignee = User.builder().id(12L).email("new@test.com").username("new").build();
        Sprint oldSprint = Sprint.builder().id(20L).name("S1").build();
        Sprint newSprint = Sprint.builder().id(21L).name("S2").build();
        BoardColumn oldColumn = BoardColumn.builder().id(30L).name("Todo").build();
        BoardColumn newColumn = BoardColumn.builder().id(31L).name("Doing").build();

        Story story = Story.builder()
                .id(100L)
                .key("STORY-1")
                .status(StoryStatus.BACKLOG)
                .assignee(oldAssignee)
                .storyPoints(3)
                .sprint(oldSprint)
                .boardColumn(oldColumn)
                .build();

        StoryActivityHelper.StoryChangeSnapshot snapshot = storyActivityHelper.snapshot(story);

        story.setStatus(StoryStatus.IN_PROGRESS);
        story.setAssignee(newAssignee);
        story.setStoryPoints(5);
        story.setSprint(newSprint);
        story.setBoardColumn(newColumn);

        storyActivityHelper.recordChanges(project, actor, story, snapshot);

        verify(activityLogService).recordActivity(project, actor, "STORY",
                100L, "STATUS_CHANGED", "from=BACKLOG,to=IN_PROGRESS");
        verify(activityLogService).recordActivity(project, actor, "STORY",
                100L, "ASSIGNEE_CHANGED", "from=old,to=new");
        verify(activityLogService).recordActivity(project, actor, "STORY",
                100L, "POINTS_CHANGED", "from=3,to=5");
        verify(activityLogService).recordActivity(project, actor, "STORY",
                100L, "SPRINT_CHANGED", "from=20,to=21");
        verify(activityLogService).recordActivity(project, actor, "STORY",
                100L, "COLUMN_CHANGED", "from=30,to=31");

        verify(notificationService, times(1)).createNotification(
                eq(newAssignee),
                eq(actor),
                eq("ASSIGNMENT"),
                eq("Assigned to Story STORY-1"),
                eq("actor assigned you to story STORY-1."),
                isNull());
    }
}
