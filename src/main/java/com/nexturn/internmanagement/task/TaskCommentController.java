package com.nexturn.internmanagement.task;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.notification.NotificationRecipient;
import com.nexturn.internmanagement.notification.NotificationService;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.security.OwnershipService;
import com.nexturn.internmanagement.security.UserPrincipal;
import com.nexturn.internmanagement.task.dto.TaskResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import com.nexturn.internmanagement.common.ResourceNotFoundException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    public record CommentRequest(@NotBlank @Size(max = 2000) String content) {}

    public record CommentResponse(Long id, Long authorId, String authorName, String authorRole, String content,
            LocalDateTime createdAt) {
        static CommentResponse from(TaskComment c) {
            return new CommentResponse(c.getId(), c.getAuthor().getId(), c.getAuthor().getName(),
                    c.getAuthor().getRole().name(), c.getContent(), c.getCreatedAt());
        }
    }

    private final TaskService taskService;
    private final TaskCommentRepository commentRepository;
    private final OwnershipService ownershipService;
    private final NotificationService notificationService;

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<CommentResponse>> list(@PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        assertCanAccess(taskId, principal);
        return ApiResponse.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(CommentResponse::from).toList());
    }

    @PostMapping
    @Transactional
    public ApiResponse<CommentResponse> add(@PathVariable Long taskId, @Valid @RequestBody CommentRequest body,
            @AuthenticationPrincipal UserPrincipal principal) {
        assertCanAccess(taskId, principal);
        TaskComment comment = new TaskComment();
        comment.setTask(taskService.getEntity(taskId));
        comment.setAuthor(principal.getUser());
        comment.setContent(body.content().trim());
        TaskComment saved = commentRepository.save(comment);
        notifyParticipants(saved, principal);
        return ApiResponse.ok(CommentResponse.from(saved), "Comment added");
    }

    private void notifyParticipants(TaskComment comment, UserPrincipal principal) {
        Task task = comment.getTask();
        User author = comment.getAuthor();
        String title = "New comment on task: " + task.getTitle();
        String preview = comment.getContent().length() > 120
                ? comment.getContent().substring(0, 120) + "..."
                : comment.getContent();
        String message = author.getName() + " commented: " + preview;

        if (task.getMentor() != null && task.getMentor().getUser() != null
                && !task.getMentor().getUser().getId().equals(author.getId())) {
            notificationService.notify(author, task.getMentor().getUser(), NotificationRecipient.Mentor, title, message);
        }
        if (task.getIntern() != null && task.getIntern().getUser() != null
                && !task.getIntern().getUser().getId().equals(author.getId())) {
            notificationService.notify(author, task.getIntern().getUser(), NotificationRecipient.Intern, title, message);
        }
    }

    @PutMapping("/{commentId}")
    @Transactional
    public ApiResponse<CommentResponse> edit(@PathVariable Long taskId, @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest body, @AuthenticationPrincipal UserPrincipal principal) {
        assertCanAccess(taskId, principal);
        TaskComment comment = commentRepository.findById(commentId)
                .filter(c -> c.getTask().getId().equals(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        if (!comment.getAuthor().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("You can edit only your own comments");
        }
        comment.setContent(body.content().trim());
        return ApiResponse.ok(CommentResponse.from(commentRepository.save(comment)), "Comment updated");
    }

    private void assertCanAccess(Long taskId, UserPrincipal principal) {
        TaskResponseDto task = taskService.findById(taskId);
        ownershipService.assertOwnsMentorResource(principal, task.mentorId());
        ownershipService.assertOwnsInternResource(principal, task.internId());
    }
}
