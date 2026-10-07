package com.nexturn.internmanagement.mentor;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.intern.InternResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public ApiResponse<List<MentorResponseDto>> list() {
        return ApiResponse.ok(mentorService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<MentorResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(mentorService.findById(id));
    }

    @GetMapping("/{id}/interns")
    public ApiResponse<List<InternResponseDto>> interns(@PathVariable Long id) {
        return ApiResponse.ok(mentorService.interns(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MentorResponseDto> create(@Valid @RequestBody MentorRequestDto dto) {
        return ApiResponse.ok(mentorService.create(dto), "Mentor created");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<MentorResponseDto> update(@PathVariable Long id, @Valid @RequestBody MentorRequestDto dto) {
        return ApiResponse.ok(mentorService.update(id, dto), "Mentor updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mentorService.delete(id);
        return ApiResponse.ok(null, "Mentor deleted");
    }
}
