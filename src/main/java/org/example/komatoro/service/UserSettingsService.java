package org.example.komatoro.service;

import jakarta.validation.Valid;
import org.example.komatoro.dto.TemporaryEntityDTO.UserSettingsDTO;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.dto.response.userSettings.UserSettingsDTOResponse;
import org.example.komatoro.exeption.OwningDeniedException;
import org.example.komatoro.exeption.UserSettingsNotFoundException;
import org.example.komatoro.model.UserSettings;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.repository.IUserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
public class UserSettingsService implements IUserSettingsService{
    private final IUserSettingsRepository repository;
    private final IUserRepository userRepository;

    public UserSettingsService(IUserSettingsRepository repository, IUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public UserSettingsDTOResponse getUserSettingsByUserId(Long userId) {
        UserSettings fromRepo = repository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException(userId));

        return this.convertToResponseDTO(fromRepo);
    }

    @Override
    public UserSettingsDTOResponse createBasicUserSettings(Long userId) {
        UserSettings settings = new UserSettings(userRepository.getReferenceById(userId));

        UserSettings saved = repository.save(settings);
        return this.convertToResponseDTO(saved);
    }

    @Override
    public UserSettings updateUserSettings(Long userId, @Valid UserSettingsDTORequest settings) {
        UserSettings fromRepo = repository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException(userId));

        if (settings.pomodoroMinutes() != null){
            fromRepo.setPomodoroMinutes(settings.pomodoroMinutes());
        }
        if (settings.longBreakMinutes() != null){
            fromRepo.setLongBreakMinutes(settings.longBreakMinutes());
        }
        if (settings.shortBreakMinutes() != null){
            fromRepo.setShortBreakMinutes(settings.shortBreakMinutes());
        }
        if (settings.longBreakInterval() != null){
            fromRepo.setLongBreakInterval(settings.longBreakInterval());
        }

        UserSettings saved = repository.save(fromRepo);
        return saved;
    }

    @Override
    public void deleteUserSettings(Long userId) {
        UserSettings fromRepo = repository.findByUserId(userId)
                .orElseThrow(() -> new UserSettingsNotFoundException(userId));

        repository.delete(fromRepo);
    }

    private UserSettingsDTOResponse convertToResponseDTO(UserSettings settings){
        return new UserSettingsDTOResponse(
                settings.getPomodoroMinutes(),
                settings.getLongBreakMinutes(),
                settings.getShortBreakMinutes(),
                settings.getLongBreakInterval()
        );
    }
}
