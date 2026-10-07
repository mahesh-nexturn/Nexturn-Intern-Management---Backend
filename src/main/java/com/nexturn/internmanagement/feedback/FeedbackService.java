package com.nexturn.internmanagement.feedback;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public List<FeedbackResponseDto> findAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FeedbackResponseDto::from).toList();
    }

    public List<FeedbackResponseDto> findByUser(Long userId) {
        return feedbackRepository.findByFromUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FeedbackResponseDto::from).toList();
    }

    public FeedbackResponseDto create(Long userId, FeedbackRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Feedback feedback = new Feedback();
        feedback.setFromUser(user);
        feedback.setSubject(dto.subject());
        feedback.setMessage(dto.message());
        return FeedbackResponseDto.from(feedbackRepository.save(feedback));
    }

    public void delete(Long id) {
        if (!feedbackRepository.existsById(id)) {
            throw new ResourceNotFoundException("Feedback not found: " + id);
        }
        feedbackRepository.deleteById(id);
    }
}
