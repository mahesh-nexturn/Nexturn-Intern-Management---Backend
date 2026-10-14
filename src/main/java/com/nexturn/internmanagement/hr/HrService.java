package com.nexturn.internmanagement.hr;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.hr.dto.HrRequest;
import com.nexturn.internmanagement.hr.dto.HrResponse;
import com.nexturn.internmanagement.intern.Intern;
import com.nexturn.internmanagement.intern.InternRepository;
import com.nexturn.internmanagement.intern.dto.InternResponseDto;
import com.nexturn.internmanagement.mentor.Mentor;
import com.nexturn.internmanagement.mentor.MentorRepository;
import com.nexturn.internmanagement.mentor.dto.MentorResponseDto;
import com.nexturn.internmanagement.user.Role;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HrService {

    /** Default login password for HR accounts created without an explicit password. */
    private static final String DEFAULT_HR_PASSWORD = "Hr@nexturn123";

    private final UserRepository userRepository;
    private final MentorRepository mentorRepository;
    private final InternRepository internRepository;
    private final PasswordEncoder passwordEncoder;

    public List<HrResponse> findAll() {
        return userRepository.findByRole(Role.HR).stream().map(this::toResponse).toList();
    }

    public HrResponse findById(Long id) {
        return toResponse(getHr(id));
    }

    @Transactional
    public HrResponse create(HrRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already registered: " + dto.email());
        }
        String password = dto.password() == null || dto.password().isBlank() ? DEFAULT_HR_PASSWORD : dto.password();
        User hr = userRepository.save(User.builder()
                .name(dto.name())
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.HR)
                .build());
        return toResponse(hr);
    }

    @Transactional
    public HrResponse update(Long id, HrRequest dto) {
        User hr = getHr(id);
        if (!hr.getEmail().equalsIgnoreCase(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already registered: " + dto.email());
        }
        hr.setName(dto.name());
        hr.setEmail(dto.email());
        if (dto.password() != null && !dto.password().isBlank()) {
            hr.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        return toResponse(userRepository.save(hr));
    }

    @Transactional
    public void delete(Long id) {
        User hr = getHr(id);
        mentorRepository.findByHrId(id).forEach(m -> m.setHr(null));
        internRepository.findByHrId(id).forEach(i -> i.setHr(null));
        userRepository.delete(hr);
    }

    @Transactional(readOnly = true)
    public List<MentorResponseDto> mentors(Long id) {
        getHr(id);
        return mentorRepository.findByHrId(id).stream().map(MentorResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InternResponseDto> interns(Long id) {
        getHr(id);
        return internRepository.findByHrId(id).stream().map(InternResponseDto::from).toList();
    }

    /** Replaces the HR's mentor set; interns of added/removed mentors follow their mentor's HR. */
    @Transactional
    public HrResponse assignMentors(Long id, List<Long> mentorIds) {
        User hr = getHr(id);
        Set<Long> wanted = new HashSet<>(mentorIds);
        for (Mentor m : mentorRepository.findByHrId(id)) {
            if (!wanted.contains(m.getId())) {
                m.setHr(null);
                for (Intern i : internRepository.findByMentorId(m.getId())) {
                    if (i.getHr() != null && id.equals(i.getHr().getId())) {
                        i.setHr(null);
                    }
                }
            }
        }
        for (Long mentorId : wanted) {
            Mentor m = mentorRepository.findById(mentorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + mentorId));
            m.setHr(hr);
            for (Intern i : internRepository.findByMentorId(mentorId)) {
                i.setHr(hr);
            }
        }
        return toResponse(hr);
    }

    /** Replaces the HR's intern set. */
    @Transactional
    public HrResponse assignInterns(Long id, List<Long> internIds) {
        User hr = getHr(id);
        Set<Long> wanted = new HashSet<>(internIds);
        for (Intern i : internRepository.findByHrId(id)) {
            if (!wanted.contains(i.getId())) {
                i.setHr(null);
            }
        }
        for (Long internId : wanted) {
            Intern i = internRepository.findById(internId)
                    .orElseThrow(() -> new ResourceNotFoundException("Intern not found: " + internId));
            i.setHr(hr);
        }
        return toResponse(hr);
    }

    private User getHr(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR not found: " + id));
        if (user.getRole() != Role.HR) {
            throw new ResourceNotFoundException("HR not found: " + id);
        }
        return user;
    }

    private HrResponse toResponse(User hr) {
        return new HrResponse(hr.getId(), hr.getName(), hr.getEmail(),
                mentorRepository.findByHrId(hr.getId()).size(),
                internRepository.findByHrId(hr.getId()).size());
    }
}
