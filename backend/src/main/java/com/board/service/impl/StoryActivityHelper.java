package com.board.service.impl;

import com.board.entity.BoardColumn;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.StoryStatus;
import com.board.service.ActivityLogService;
import com.board.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Handles activity logging and notifications for story changes.
 */
@Component
@RequiredArgsConstructor
public class StoryActivityHelper {

    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    StoryChangeSnapshot snapshot(Story story) {
        return new StoryChangeSnapshot(
                story.getStatus(),
                story.getAssignee(),
                story.getStoryPoints(),
                story.getSprint(),
                story.getBoardColumn()
        );
    }

    void recordChanges(Project project, User actor, Story story, StoryChangeSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        if (snapshot.status() != story.getStatus()) {
            String details = "from=" + snapshot.status() + ",to=" + story.getStatus();
            activityLogService.recordActivity(project, actor, "STORY",
                    story.getId(), "STATUS_CHANGED", details);
        }
        if (!Objects.equals(idOf(snapshot.assignee()), idOf(story.getAssignee()))) {
            recordAssigneeActivity(project, actor, story, snapshot.assignee(), story.getAssignee());
            notifyAssignmentIfChanged(actor, story.getAssignee(), snapshot.assignee(), story);
        }
        if (!Objects.equals(snapshot.storyPoints(), story.getStoryPoints())) {
            String details = "from=" + valueOrEmpty(snapshot.storyPoints())
                    + ",to=" + valueOrEmpty(story.getStoryPoints());
            activityLogService.recordActivity(project, actor, "STORY",
                    story.getId(), "POINTS_CHANGED", details);
        }
        if (!Objects.equals(idOf(snapshot.sprint()), idOf(story.getSprint()))) {
            String details = "from=" + valueOrEmpty(idOf(snapshot.sprint()))
                    + ",to=" + valueOrEmpty(idOf(story.getSprint()));
            activityLogService.recordActivity(project, actor, "STORY",
                    story.getId(), "SPRINT_CHANGED", details);
        }
        if (!Objects.equals(idOf(snapshot.boardColumn()), idOf(story.getBoardColumn()))) {
            String details = "from=" + valueOrEmpty(idOf(snapshot.boardColumn()))
                    + ",to=" + valueOrEmpty(idOf(story.getBoardColumn()));
            activityLogService.recordActivity(project, actor, "STORY",
                    story.getId(), "COLUMN_CHANGED", details);
        }
    }

    void notifyAssignmentIfChanged(User actor, User assignee, User previousAssignee, Story story) {
        if (assignee == null || Objects.equals(idOf(assignee), idOf(previousAssignee))) {
            return;
        }
        if (actor != null && Objects.equals(idOf(actor), idOf(assignee))) {
            return;
        }
        notificationService.createNotification(
                assignee,
                actor,
                "ASSIGNMENT",
                "Assigned to Story " + story.getKey(),
                resolveDisplayName(actor) + " assigned you to story " + story.getKey() + ".",
                null);
    }

    private void recordAssigneeActivity(Project project, User actor, Story story,
            User oldAssignee, User newAssignee) {
        String details = "from=" + valueOrEmpty(nameOrNull(oldAssignee))
                + ",to=" + valueOrEmpty(nameOrNull(newAssignee));
        activityLogService.recordActivity(project, actor, "STORY",
                story.getId(), "ASSIGNEE_CHANGED", details);
    }

    private Long idOf(Object entity) {
        if (entity instanceof com.board.entity.BaseEntity baseEntity) {
            return baseEntity.getId();
        }
        return null;
    }

    private String nameOrNull(User user) {
        return user == null ? null : resolveDisplayName(user);
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "";
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getEmail();
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    record StoryChangeSnapshot(
            StoryStatus status,
            User assignee,
            Integer storyPoints,
            Sprint sprint,
            BoardColumn boardColumn
    ) {
    }
}
