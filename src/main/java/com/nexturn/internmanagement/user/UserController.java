package com.nexturn.internmanagement.user;

import com.nexturn.internmanagement.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponseDto>> list() {
        return ApiResponse.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponseDto> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.findById(id));
    }

    @PostMapping
    public ApiResponse<UserResponseDto> create(@Valid @RequestBody UserRequestDto dto) {
        return ApiResponse.ok(userService.create(dto), "User created");
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponseDto> update(@PathVariable Long id, @Valid @RequestBody UserRequestDto dto) {
        return ApiResponse.ok(userService.update(id, dto), "User updated");
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserResponseDto> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(userService.updateRole(id, body.get("role")), "Role updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok(null, "User deleted");
    }
}
