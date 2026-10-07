package com.nexturn.internmanagement.settings;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    public SettingsResponseDto findByUserId(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        return SettingsResponseDto.from(settings);
    }

    public SettingsResponseDto update(Long userId, SettingsRequestDto dto) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefault(userId));
        settings.setEmailNotifications(dto.emailNotifications());
        settings.setPushNotifications(dto.pushNotifications());
        settings.setTheme(dto.theme());
        settings.setPhone(dto.phone());
        return SettingsResponseDto.from(userSettingsRepository.save(settings));
    }

    private UserSettings createDefault(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        UserSettings settings = UserSettings.builder()
                .user(user)
                .emailNotifications(true)
                .pushNotifications(true)
                .theme("light")
                .build();
        return userSettingsRepository.save(settings);
    }
}
