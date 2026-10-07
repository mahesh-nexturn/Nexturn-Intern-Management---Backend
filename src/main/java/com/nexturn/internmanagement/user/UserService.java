package com.nexturn.internmanagement.user;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream().map(UserResponseDto::from).toList();
    }

    public UserResponseDto findById(Long id) {
        return UserResponseDto.from(getEntity(id));
    }

    public User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserResponseDto create(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already registered: " + dto.email());
        }
        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password() == null ? "changeme123" : dto.password()))
                .role(Role.valueOf(dto.role().toUpperCase()))
                .build();
        return UserResponseDto.from(userRepository.save(user));
    }

    public UserResponseDto update(Long id, UserRequestDto dto) {
        User user = getEntity(id);
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setRole(Role.valueOf(dto.role().toUpperCase()));
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        return UserResponseDto.from(userRepository.save(user));
    }

    public UserResponseDto updateRole(Long id, String role) {
        User user = getEntity(id);
        user.setRole(Role.valueOf(role.toUpperCase()));
        return UserResponseDto.from(userRepository.save(user));
    }

    public void delete(Long id) {
        userRepository.delete(getEntity(id));
    }
}
