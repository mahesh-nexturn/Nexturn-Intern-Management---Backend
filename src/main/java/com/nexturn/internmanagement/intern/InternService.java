package com.nexturn.internmanagement.intern;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.meeting.MeetingRepository;
import com.nexturn.internmanagement.meeting.MeetingResponseDto;
import com.nexturn.internmanagement.task.TaskRepository;
import com.nexturn.internmanagement.task.TaskResponseDto;
import com.nexturn.internmanagement.task.TaskStatus;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternService {

    /** Default login password assigned when an intern's user account is auto-provisioned. */
    private static final String DEFAULT_INTERN_PASSWORD = "Inexturn@123";

    private final InternRepository internRepository;
    private final MentorRepository mentorRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<InternResponseDto> findAll() {
        return internRepository.findAll().stream().map(InternResponseDto::from).toList();
    }

    public List<InternResponseDto> findByMentor(Long mentorId) {
        return internRepository.findByMentorId(mentorId).stream().map(InternResponseDto::from).toList();
    }

    public InternResponseDto findById(Long id) {
        return InternResponseDto.from(getEntity(id));
    }

    public Intern getEntity(Long id) {
        return internRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + id));
    }

    public InternResponseDto create(InternRequestDto dto) {
        Intern intern = new Intern();
        applyCommonFields(intern, dto);
        intern.setUser(resolveUserForCreate(dto));
        return InternResponseDto.from(internRepository.save(intern));
    }

    public InternResponseDto update(Long id, InternRequestDto dto) {
        Intern intern = getEntity(id);
        applyCommonFields(intern, dto);
        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.userId()));
            intern.setUser(user);
        }
        return InternResponseDto.from(internRepository.save(intern));
    }

    public void delete(Long id) {
        internRepository.delete(getEntity(id));
    }

    public InternDashboardDto dashboard(Long id) {
        Intern intern = getEntity(id);
        List<TaskResponseDto> tasks = taskRepository.findByInternId(id).stream()
                .map(TaskResponseDto::from).toList();
        List<MeetingResponseDto> meetings = meetingRepository.findByInternId(id).stream()
                .map(MeetingResponseDto::from).toList();
        return new InternDashboardDto(
                InternResponseDto.from(intern),
                taskRepository.countByInternIdAndStatus(id, TaskStatus.Pending),
                taskRepository.countByInternIdAndStatus(id, TaskStatus.In_Progress),
                taskRepository.countByInternIdAndStatus(id, TaskStatus.Completed),
                tasks,
                meetings);
    }

    private void applyCommonFields(Intern intern, InternRequestDto dto) {
        intern.setName(dto.name());
        intern.setEmail(dto.email());
        intern.setDepartment(dto.department());
        intern.setStatus(dto.status() == null ? "Active" : dto.status());
        if (dto.mentorId() != null) {
            Mentor mentor = mentorRepository.findById(dto.mentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + dto.mentorId()));
            intern.setMentor(mentor);
        } else {
            intern.setMentor(null);
        }
    }

    /**
     * Resolves the login account for a newly created intern. If an explicit userId is supplied it is
     * used as-is; otherwise an existing user matching the intern's email is reused, or a new INTERN
     * user account is auto-provisioned with the default password so the intern can log in immediately.
     */
    private User resolveUserForCreate(InternRequestDto dto) {
        if (dto.userId() != null) {
            return userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.userId()));
        }
        return userRepository.findByEmail(dto.email())
                .orElseGet(() -> userRepository.save(User.builder()
                        .name(dto.name())
                        .email(dto.email())
                        .passwordHash(passwordEncoder.encode(DEFAULT_INTERN_PASSWORD))
                        .role(Role.INTERN)
                        .build()));
    }
}
