package com.board.service.impl;

import com.board.dto.comment.CommentCreateRequest;
import com.board.dto.comment.CommentResponse;
import com.board.dto.comment.CommentUpdateRequest;
import com.board.entity.Comment;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.CommentMapper;
import com.board.repository.CommentRepository;
import com.board.repository.EpicRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.StoryRepository;
import com.board.repository.TaskRepository;
import com.board.repository.UserRepository;
import com.board.service.CommentService;
import com.board.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of CommentService.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Pattern MENTION_PATTERN = Pattern.compile("(?i)@([a-z0-9._-]{3,30})");

    private final CommentRepository commentRepository;
    private final EpicRepository epicRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponse createCommentOnEpic(Long epicId, CommentCreateRequest request, Long userId) {
        Epic epic = findEpicById(epicId);
        validateMembership(epic.getProject(), userId);
        User author = findUserById(userId);

        Comment comment = buildComment(request, author);
        comment.setEpic(epic);

        comment = commentRepository.save(comment);
        notifyMentions(comment, epic.getProject(), author);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse createCommentOnStory(Long storyId, CommentCreateRequest request, Long userId) {
        Story story = findStoryById(storyId);
        validateMembership(story.getEpic().getProject(), userId);
        User author = findUserById(userId);

        Comment comment = buildComment(request, author);
        comment.setStory(story);

        comment = commentRepository.save(comment);
        notifyMentions(comment, story.getEpic().getProject(), author);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse createCommentOnTask(Long taskId, CommentCreateRequest request, Long userId) {
        Task task = findTaskById(taskId);
        validateMembership(task.getStory().getEpic().getProject(), userId);
        User author = findUserById(userId);

        Comment comment = buildComment(request, author);
        comment.setTask(task);

        comment = commentRepository.save(comment);
        notifyMentions(comment, task.getStory().getEpic().getProject(), author);
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId, Long userId) {
        Comment comment = findCommentById(commentId);
        Project project = getProjectFromComment(comment);
        validateMembership(project, userId);

        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForEpic(Long epicId, Long userId) {
        Epic epic = findEpicById(epicId);
        validateMembership(epic.getProject(), userId);

        List<Comment> comments = commentRepository.findByEpicOrderByCreatedAtDesc(epic);
        return commentMapper.toResponseList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForStory(Long storyId, Long userId) {
        Story story = findStoryById(storyId);
        validateMembership(story.getEpic().getProject(), userId);

        List<Comment> comments = commentRepository.findByStoryOrderByCreatedAtDesc(story);
        return commentMapper.toResponseList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForTask(Long taskId, Long userId) {
        Task task = findTaskById(taskId);
        validateMembership(task.getStory().getEpic().getProject(), userId);

        List<Comment> comments = commentRepository.findByTaskOrderByCreatedAtDesc(task);
        return commentMapper.toResponseList(comments);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request, Long userId) {
        Comment comment = findCommentById(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = findCommentById(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        comment.softDelete();
        commentRepository.save(comment);
    }

    private Comment buildComment(CommentCreateRequest request, User author) {
        Comment.CommentBuilder<?, ?> builder = Comment.builder()
                .content(request.getContent())
                .author(author);

        if (request.getParentCommentId() != null) {
            Comment parent = findCommentById(request.getParentCommentId());
            builder.parentComment(parent);
        }

        return builder.build();
    }

    private Project getProjectFromComment(Comment comment) {
        if (comment.getEpic() != null) {
            return comment.getEpic().getProject();
        } else if (comment.getStory() != null) {
            return comment.getStory().getEpic().getProject();
        } else if (comment.getTask() != null) {
            return comment.getTask().getStory().getEpic().getProject();
        }
        throw new IllegalStateException("Comment must be associated with an Epic, Story, or Task");
    }

    private Epic findEpicById(Long epicId) {
        return epicRepository.findById(epicId)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Epic not found"));
    }

    private Story findStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Story not found"));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void notifyMentions(Comment comment, Project project, User author) {
        Set<String> usernames = extractMentions(comment.getContent());
        if (usernames.isEmpty()) {
            return;
        }
        String authorUsername = author.getUsername();
        if (authorUsername != null) {
            usernames.remove(authorUsername.toLowerCase(Locale.ROOT));
        }
        if (usernames.isEmpty()) {
            return;
        }

        List<User> mentionedUsers = userRepository.findByUsernameIn(usernames);
        for (User mentioned : mentionedUsers) {
            if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, mentioned)) {
                continue;
            }
            String authorLabel = resolveDisplayName(author);
            notificationService.createNotification(
                    mentioned,
                    author,
                    "MENTION",
                    "Mentioned in a comment",
                    authorLabel + " mentioned you in a comment.",
                    null);
        }
    }

    private Set<String> extractMentions(String content) {
        if (content == null || content.isBlank()) {
            return Set.of();
        }
        Matcher matcher = MENTION_PATTERN.matcher(content);
        java.util.Set<String> usernames = new java.util.HashSet<>();
        while (matcher.find()) {
            String username = matcher.group(1);
            if (username != null) {
                usernames.add(username.toLowerCase(Locale.ROOT));
            }
        }
        return usernames;
    }

    private String resolveDisplayName(User user) {
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getEmail();
    }
}
