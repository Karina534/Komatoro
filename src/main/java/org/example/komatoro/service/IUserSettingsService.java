package org.example.komatoro.service;

import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.dto.response.userSettings.UserSettingsDTOResponse;
import org.example.komatoro.model.UserSettings;

import java.util.UUID;

public interface IUserSettingsService {
    UserSettingsDTOResponse getUserSettingsByUserId(Long userId);
    UserSettingsDTOResponse createBasicUserSettings(Long userId);
    UserSettings updateUserSettings(Long userId, UserSettingsDTORequest settings);
    void deleteUserSettings(Long userId);
}
