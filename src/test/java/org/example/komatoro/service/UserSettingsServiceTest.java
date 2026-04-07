package org.example.komatoro.service;

import org.assertj.core.api.Assert;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.dto.response.userSettings.UserSettingsDTOResponse;
import org.example.komatoro.exeption.NotFoundException;
import org.example.komatoro.model.User;
import org.example.komatoro.model.UserSettings;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.repository.IUserSettingsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {
    @Mock private IUserSettingsRepository repository;
    @Mock private IUserRepository userRepository;
    @InjectMocks private UserSettingsService userSettingsService;

    private static User testUser;
    private static UserSettings testSettings;
    private final static Long USER_ID = 1L;
    private final static Integer DEFAULT_POMODORO_MINUTES = 25;
    private final static Integer DEFAULT_LONG_BREAK_MINUTES = 15;
    private final static Integer DEFAULT_SHORT_BREAK_MINUTES = 5;
    private final static Integer DEFAULT_LONG_BREAK_INTERVAL = 4;

    @BeforeAll
    static void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail("testemail@mail.ru");

        testSettings = new UserSettings(testUser);
        testSettings.setPomodoroMinutes(30);
        testSettings.setLongBreakMinutes(20);
        testSettings.setShortBreakMinutes(10);
        testSettings.setLongBreakInterval(3);
    }

    @Test
    @DisplayName("Должен успешно получить настройки пользователя по ID")
    void getUserSettingsByUserId_SettingsExist_ReturnResponseDTO() {
        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);

        UserSettingsDTOResponse response = userSettingsService.getUserSettingsByUserId(USER_ID);

        assertNotNull(response);
        assertThat(response.pomodoroMinutes()).isEqualTo(testSettings.getPomodoroMinutes());
        assertThat(response.longBreakMinutes()).isEqualTo(testSettings.getLongBreakMinutes());
        assertThat(response.shortBreakMinutes()).isEqualTo(testSettings.getShortBreakMinutes());
        assertThat(response.longBreakInterval()).isEqualTo(testSettings.getLongBreakInterval());

        verify(repository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при получении настроек несуществующего пользователя")
    void getUserSettingsByUserId_SettingsNotFound_ThrowNotFoundException() {
        doReturn(Optional.empty()).when(repository).findByUserId(USER_ID);

        assertThatThrownBy(() -> userSettingsService.getUserSettingsByUserId(USER_ID))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Должен успешно создать базовые настройки для пользователя")
    void createBasicUserSettings_PayloadValid_ReturnResponseDTO() {
        doReturn(testUser).when(userRepository).getReferenceById(USER_ID);
        when(repository.save(any(UserSettings.class))).thenAnswer(inv -> {
            UserSettings toSave = inv.getArgument(0);
            toSave.setPomodoroMinutes(DEFAULT_POMODORO_MINUTES);
            toSave.setLongBreakMinutes(DEFAULT_LONG_BREAK_MINUTES);
            toSave.setShortBreakMinutes(DEFAULT_SHORT_BREAK_MINUTES);
            toSave.setLongBreakInterval(DEFAULT_LONG_BREAK_INTERVAL);
            return toSave;
        });

        UserSettingsDTOResponse response = userSettingsService.createBasicUserSettings(USER_ID);

        assertNotNull(response);
        assertThat(response.pomodoroMinutes()).isEqualTo(DEFAULT_POMODORO_MINUTES);
        assertThat(response.longBreakMinutes()).isEqualTo(DEFAULT_LONG_BREAK_MINUTES);
        assertThat(response.shortBreakMinutes()).isEqualTo(DEFAULT_SHORT_BREAK_MINUTES);
        assertThat(response.longBreakInterval()).isEqualTo(DEFAULT_LONG_BREAK_INTERVAL);

        verify(userRepository).getReferenceById(USER_ID);
        verify(repository).save(any(UserSettings.class));
    }

    @Test
    @DisplayName("Должен успешно обновить все поля настроек")
    void updateUserSettings_UpdateAllFields_ReturnUpdatedSettings() {
        UserSettingsDTORequest request = new UserSettingsDTORequest(
                35, 25, 15, 5
        );

        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);
        when(repository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings result = userSettingsService.updateUserSettings(USER_ID, request);

        assertNotNull(result);
        assertThat(result.getPomodoroMinutes()).isEqualTo(35);
        assertThat(result.getLongBreakMinutes()).isEqualTo(25);
        assertThat(result.getShortBreakMinutes()).isEqualTo(15);
        assertThat(result.getLongBreakInterval()).isEqualTo(5);

        verify(repository).findByUserId(USER_ID);
        verify(repository).save(testSettings);
    }

    @Test
    @DisplayName("Должен успешно обновить только pomodoroMinutes")
    void updateUserSettings_UpdatePomodoroOnly_ReturnUpdatedSettings() {
        // given
        UserSettingsDTORequest request = new UserSettingsDTORequest(
                40, null, null, null
        );

        Integer expectedPomodoroMinutes = 40;
        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);
        when(repository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        UserSettings result = userSettingsService.updateUserSettings(USER_ID, request);

        // then
        assertNotNull(result);
        assertThat(result.getPomodoroMinutes()).isEqualTo(expectedPomodoroMinutes);
        assertThat(result.getLongBreakMinutes()).isEqualTo(testSettings.getLongBreakMinutes());
        assertThat(result.getShortBreakMinutes()).isEqualTo(testSettings.getShortBreakMinutes());
        assertThat(result.getLongBreakInterval()).isEqualTo(testSettings.getLongBreakInterval());

        verify(repository).findByUserId(USER_ID);
        verify(repository).save(testSettings);
    }

    @Test
    @DisplayName("Должен успешно обновить только longBreakMinutes")
    void updateUserSettings_UpdateLongBreakOnly_ReturnUpdatedSettings() {
        UserSettingsDTORequest request = new UserSettingsDTORequest(
                null, 30, null, null
        );

        Integer expectedLongBreakMinutes = 30;
        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);
        when(repository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings result = userSettingsService.updateUserSettings(USER_ID, request);

        assertNotNull(result);
        assertThat(result.getPomodoroMinutes()).isEqualTo(testSettings.getPomodoroMinutes());
        assertThat(result.getLongBreakMinutes()).isEqualTo(expectedLongBreakMinutes);
        assertThat(result.getShortBreakMinutes()).isEqualTo(testSettings.getShortBreakMinutes());
        assertThat(result.getLongBreakInterval()).isEqualTo(testSettings.getLongBreakInterval());

        verify(repository).findByUserId(USER_ID);
        verify(repository).save(testSettings);
    }

    @Test
    @DisplayName("Должен успешно обновить только shortBreakMinutes")
    void updateUserSettings_UpdateShortBreakOnly_ReturnUpdatedSettings() {
        UserSettingsDTORequest request = new UserSettingsDTORequest(
                null, null, 12, null
        );

        Integer expectedShortBreakMinutes = 12;
        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);
        when(repository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings result = userSettingsService.updateUserSettings(USER_ID, request);

        assertNotNull(result);
        assertThat(result.getPomodoroMinutes()).isEqualTo(testSettings.getPomodoroMinutes());
        assertThat(result.getLongBreakMinutes()).isEqualTo(testSettings.getLongBreakMinutes());
        assertThat(result.getShortBreakMinutes()).isEqualTo(expectedShortBreakMinutes);
        assertThat(result.getLongBreakInterval()).isEqualTo(testSettings.getLongBreakInterval());

        verify(repository).findByUserId(USER_ID);
        verify(repository).save(testSettings);
    }

    @Test
    @DisplayName("Должен успешно обновить только longBreakInterval")
    void updateUserSettings_UpdateLongBreakIntervalOnly_ReturnUpdatedSettings() {
        UserSettingsDTORequest request = new UserSettingsDTORequest(
                null, null, null, 6
        );

        Integer expectedLongBreakInterval = 6;
        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);
        when(repository.save(any(UserSettings.class))).thenAnswer(inv -> inv.getArgument(0));

        UserSettings result = userSettingsService.updateUserSettings(USER_ID, request);

        assertNotNull(result);
        assertThat(result.getPomodoroMinutes()).isEqualTo(testSettings.getPomodoroMinutes());
        assertThat(result.getLongBreakMinutes()).isEqualTo(testSettings.getLongBreakMinutes());
        assertThat(result.getShortBreakMinutes()).isEqualTo(testSettings.getShortBreakMinutes());
        assertThat(result.getLongBreakInterval()).isEqualTo(expectedLongBreakInterval);

        verify(repository).findByUserId(USER_ID);
        verify(repository).save(testSettings);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при обновлении настроек несуществующего пользователя")
    void updateUserSettings_SettingsNotFound_ThrowNotFoundException() {
        UserSettingsDTORequest request = new UserSettingsDTORequest(
                35, 25, 15, 5
        );

        doReturn(Optional.empty()).when(repository).findByUserId(USER_ID);

        assertThatThrownBy(() -> userSettingsService.updateUserSettings(USER_ID, request))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findByUserId(USER_ID);
        verify(repository, never()).save(any(UserSettings.class));
    }

    @Test
    @DisplayName("Должен успешно удалить настройки пользователя")
    void deleteUserSettings_SettingsExist_DeleteSuccessfully() {
        doReturn(Optional.of(testSettings)).when(repository).findByUserId(USER_ID);
        doNothing().when(repository).delete(testSettings);

        userSettingsService.deleteUserSettings(USER_ID);

        verify(repository).findByUserId(USER_ID);
        verify(repository).delete(testSettings);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при удалении настроек несуществующего пользователя")
    void deleteUserSettings_SettingsNotFound_ThrowNotFoundException() {
        doReturn(Optional.empty()).when(repository).findByUserId(USER_ID);

        assertThatThrownBy(() -> userSettingsService.deleteUserSettings(USER_ID))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findByUserId(USER_ID);
        verify(repository, never()).delete(any(UserSettings.class));
    }
}