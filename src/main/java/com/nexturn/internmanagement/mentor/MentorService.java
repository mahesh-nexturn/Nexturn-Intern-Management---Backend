package com.nexturn.internmanagement.mentor;
import com.nexturn.internmanagement.mentor.dto.MentorRequestDto;
import com.nexturn.internmanagement.mentor.dto.MentorResponseDto;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.intern.dto.InternResponseDto;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentorService {

    /** Default login password assigned when a mentor's user account is auto-provisioned. */
    private static final String DEFAULT_MENTOR_PASSWORD = "nexturn@123";

    private final MentorRepository mentorRepository;
    private final InternRepository internRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<MentorResponseDto> findAll() {
        return mentorRepository.findAll().stream().map(MentorResponseDto::from).toList();
    }

    public MentorResponseDto findById(Long id) {
        return MentorResponseDto.from(getEntity(id));
    }

    public Mentor getEntity(Long id) {
        return mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + id));
    }

    public MentorResponseDto create(MentorRequestDto dto, Long hrId) {
        Mentor mentor = new Mentor();
        applyCommonFields(mentor, dto);
        mentor.setUser(resolveUserForCreate(dto));
        if (hrId != null) {
            mentor.setHr(resolveHr(hrId));
        }
        return MentorResponseDto.from(mentorRepository.save(mentor));
    }

    /** @param hrId new owning HR account, or null to leave the current HR unchanged. */
    public MentorResponseDto update(Long id, MentorRequestDto dto, Long hrId) {
        Mentor mentor = getEntity(id);
        applyCommonFields(mentor, dto);
        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.userId()));
            mentor.setUser(user);
        }
        if (hrId != null) {
            mentor.setHr(resolveHr(hrId));
        }
        return MentorResponseDto.from(mentorRepository.save(mentor));
    }

    private User resolveHr(Long hrId) {
        User hr = userRepository.findById(hrId)
                .orElseThrow(() -> new ResourceNotFoundException("HR user not found: " + hrId));
        if (hr.getRole() != Role.HR) {
            throw new IllegalArgumentException("User " + hrId + " is not an HR account");
        }
        return hr;
    }

    public List<MentorResponseDto> findByHr(Long hrId) {
        return mentorRepository.findByHrId(hrId).stream().map(MentorResponseDto::from).toList();
    }

    public void delete(Long id) {
        mentorRepository.delete(getEntity(id));
    }

    public List<InternResponseDto> interns(Long mentorId) {
        return internRepository.findByMentorId(mentorId).stream().map(InternResponseDto::from).toList();
    }

    private void applyCommonFields(Mentor mentor, MentorRequestDto dto) {
        mentor.setName(dto.name());
        mentor.setEmail(dto.email());
        mentor.setDepartment(dto.department());
        mentor.setDesignation(dto.designation());
        mentor.setStatus(MentorStatus.valueOf(dto.status() == null ? "Active" : dto.status()));
    }

    /**
     * Resolves the login account for a newly created mentor. If an explicit userId is supplied it is
     * used as-is; otherwise an existing user matching the mentor's email is reused, or a new MENTOR
     * user account is auto-provisioned with the default password so the mentor can log in immediately.
     */
    private User resolveUserForCreate(MentorRequestDto dto) {
        if (dto.userId() != null) {
            return userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.userId()));
        }
        return userRepository.findByEmail(dto.email())
                .orElseGet(() -> userRepository.save(User.builder()
                        .name(dto.name())
                        .email(dto.email())
                        .passwordHash(passwordEncoder.encode(DEFAULT_MENTOR_PASSWORD))
                        .role(Role.MENTOR)
                        .build()));
    }
}
