package com.nexturn.internmanagement.hr;

import com.nexturn.internmanagement.common.ApiResponse;
import com.nexturn.internmanagement.hr.dto.HrAssignRequest;
import com.nexturn.internmanagement.hr.dto.HrRequest;
import com.nexturn.internmanagement.hr.dto.HrResponse;
import com.nexturn.internmanagement.intern.dto.InternResponseDto;
import com.nexturn.internmanagement.mentor.dto.MentorResponseDto;
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
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class HrController {

    private final HrService hrService;

    @GetMapping
    public ApiResponse<List<HrResponse>> list() {
        return ApiResponse.ok(hrService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<HrResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(hrService.findById(id));
    }

    @PostMapping
    public ApiResponse<HrResponse> create(@Valid @RequestBody HrRequest dto) {
        return ApiResponse.ok(hrService.create(dto), "HR created");
    }

    @PutMapping("/{id}")
    public ApiResponse<HrResponse> update(@PathVariable Long id, @Valid @RequestBody HrRequest dto) {
        return ApiResponse.ok(hrService.update(id, dto), "HR updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hrService.delete(id);
        return ApiResponse.ok(null, "HR deleted");
    }

    @GetMapping("/{id}/mentors")
    public ApiResponse<List<MentorResponseDto>> mentors(@PathVariable Long id) {
        return ApiResponse.ok(hrService.mentors(id));
    }

    @GetMapping("/{id}/interns")
    public ApiResponse<List<InternResponseDto>> interns(@PathVariable Long id) {
        return ApiResponse.ok(hrService.interns(id));
    }

    @PutMapping("/{id}/mentors")
    public ApiResponse<HrResponse> assignMentors(@PathVariable Long id, @Valid @RequestBody HrAssignRequest dto) {
        return ApiResponse.ok(hrService.assignMentors(id, dto.ids()), "Mentors assigned");
    }

    @PutMapping("/{id}/interns")
    public ApiResponse<HrResponse> assignInterns(@PathVariable Long id, @Valid @RequestBody HrAssignRequest dto) {
        return ApiResponse.ok(hrService.assignInterns(id, dto.ids()), "Interns assigned");
    }
}
