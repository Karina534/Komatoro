package org.example.komatoro.service;

import org.example.komatoro.dto.request.user.UserCreateDTORequest;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.dto.response.user.UserDTOResponse;
import org.example.komatoro.dto.response.user.UserWithSettingsDtoResponse;
import org.example.komatoro.exeption.AccountWithEmailAlreadyExist;
import org.example.komatoro.exeption.NotFoundException;
import org.example.komatoro.mapper.UserMapper;
import org.example.komatoro.model.Role;
import org.example.komatoro.model.User;
import org.example.komatoro.model.UserSettings;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.security.CustomUserDetails;
import org.example.komatoro.security.jwt.TokenUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private IUserRepository repository;
    @Mock private IUserSettingsService settingsService;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private static User testUser;
    private static UserSettings testSettings;
    private static CustomUserDetails customUserDetails;
    private static TokenUser tokenUser;
    private final static Long USER_ID = 1L;
    private final static String USER_EMAIL = "testemail@mail.ru";
    private final static String USER_USERNAME = "testuser";
    private final static String PASSWORD = "password123";
    private final static String ENCODED_PASSWORD = "encodedPassword123";

    @BeforeAll
    static void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail(USER_EMAIL);
        testUser.setUsername(USER_USERNAME);
        testUser.setPassword(PASSWORD);
        testUser.setRole(Role.USER);
        testUser.setVerified(true);

        testSettings = new UserSettings(testUser);
        testSettings.setPomodoroMinutes(25);
        testSettings.setLongBreakMinutes(15);
        testSettings.setShortBreakMinutes(5);
        testSettings.setLongBreakInterval(4);

        customUserDetails = new CustomUserDetails(testUser);
        tokenUser = new TokenUser(USER_EMAIL, USER_EMAIL,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), null);
    }

    @Test
    @DisplayName("Должен успешно зарегистрировать нового пользователя")
    void registration_NewUser_ReturnUserDTOResponse() {
        // given
        UserCreateDTORequest request = new UserCreateDTORequest(
                USER_USERNAME, PASSWORD, USER_EMAIL, Role.USER
        );

        UserDTOResponse expectedResponse = new UserDTOResponse(
                USER_ID, USER_USERNAME, USER_EMAIL, Role.USER,
                testUser.getCreatedAt(), testUser.isVerified()
        );

        doReturn(Optional.empty()).when(repository).findByEmail(USER_EMAIL);
        doReturn(testUser).when(userMapper).dtoCreateToEntity(request);
        doReturn(ENCODED_PASSWORD).when(passwordEncoder).encode(PASSWORD);
        doReturn(testUser).when(repository).save(any(User.class));
        doReturn(expectedResponse).when(userMapper).toResponse(testUser);

        // when
        UserDTOResponse response = userService.registration(request);

        // then
        assertNotNull(response);
        assertThat(response).isEqualTo(expectedResponse);

        verify(repository).findByEmail(USER_EMAIL);
        verify(userMapper).dtoCreateToEntity(request);
        verify(passwordEncoder).encode(PASSWORD);
        verify(repository).save(argThat(user -> {
            assertThat(user.getDailyStats()).isNotNull();
            assertThat(user.getSettings()).isNotNull();
            return true;
        }));
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Должен выбросить AccountWithEmailAlreadyExist при регистрации существующего пользователя")
    void registration_UserAlreadyExist_ThrowRuntimeException() {
        UserCreateDTORequest request = new UserCreateDTORequest(
                USER_USERNAME, PASSWORD, USER_EMAIL, Role.USER
        );

        doReturn(Optional.of(testUser)).when(repository).findByEmail(USER_EMAIL);

        assertThatThrownBy(() -> userService.registration(request))
                .isInstanceOf(AccountWithEmailAlreadyExist.class);

        verify(repository).findByEmail(USER_EMAIL);
        verify(userMapper, never()).dtoCreateToEntity(any());
        verify(passwordEncoder, never()).encode(any());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен успешно обновить настройки пользователя")
    void updateSettings_ValidData_ReturnUserWithSettingsDtoResponse() {
        // given
        UserSettingsDTORequest settingsRequest = new UserSettingsDTORequest(
                30, 20, 10, 5
        );

        UserWithSettingsDtoResponse expectedResponse = new UserWithSettingsDtoResponse(
                USER_ID, USER_USERNAME, USER_EMAIL, testUser.getRole(),
                testUser.getCreatedAt(), testUser.isVerified(), settingsRequest.pomodoroMinutes(),
                settingsRequest.longBreakMinutes(), settingsRequest.shortBreakMinutes(),
                settingsRequest.longBreakInterval()
        );

        doReturn(Optional.of(testUser)).when(repository).findById(USER_ID);
        doReturn(testSettings).when(settingsService).updateUserSettings(USER_ID, settingsRequest);
        doReturn(expectedResponse).when(userMapper).toResponse(testUser, testSettings);

        // when
        UserWithSettingsDtoResponse response = userService.updateSettings(
                customUserDetails, settingsRequest
        );

        // then
        assertNotNull(response);
        assertThat(response).isEqualTo(expectedResponse);
        assertThat(testUser.getSettings()).isEqualTo(testSettings);

        verify(repository).findById(USER_ID);
        verify(settingsService).updateUserSettings(USER_ID, settingsRequest);
        verify(userMapper).toResponse(testUser, testSettings);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при обновлении настроек несуществующего пользователя")
    void updateSettings_UserNotFound_ThrowNotFoundException() {

        UserSettingsDTORequest settingsRequest = new UserSettingsDTORequest(
                30, 20, 10, 5
        );

        doReturn(Optional.empty()).when(repository).findById(USER_ID);

        assertThatThrownBy(() -> userService.updateSettings(customUserDetails, settingsRequest))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findById(USER_ID);
        verify(settingsService, never()).updateUserSettings(any(), any());
        verify(userMapper, never()).toResponse(any(), any());
    }

    @Test
    @DisplayName("Должен успешно получить пользователя по ID из CustomUserDetails")
    void getById_ValidUserDetails_ReturnUserDTOResponse() {
        // given
        UserDTOResponse expectedResponse = new UserDTOResponse(
                USER_ID, USER_USERNAME, USER_EMAIL, testUser.getRole(),
                testUser.getCreatedAt(), testUser.isVerified()
        );

        doReturn(Optional.of(testUser)).when(repository).findById(USER_ID);
        doReturn(expectedResponse).when(userMapper).toResponse(testUser);

        // when
        UserDTOResponse response = userService.getById(customUserDetails);

        // then
        assertNotNull(response);
        assertThat(response).isEqualTo(expectedResponse);

        verify(repository).findById(USER_ID);
        verify(userMapper).toResponse(testUser);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при получении несуществующего пользователя")
    void getById_UserNotFound_ThrowNotFoundException() {
        doReturn(Optional.empty()).when(repository).findById(USER_ID);

        assertThatThrownBy(() -> userService.getById(customUserDetails))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findById(USER_ID);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Должен успешно получить userId из CustomUserDetails")
    void getUserIdFromUserDetails_CustomUserDetails_ReturnUserId() {
        CustomUserDetails userDetails = customUserDetails;

        Long result = userService.getUserIdFromUserDetails(userDetails);

        assertThat(result).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Должен успешно получить userId из TokenUser")
    void getUserIdFromUserDetails_TokenUser_ReturnUserId() {
        doReturn(Optional.of(testUser)).when(repository).findByEmail(USER_EMAIL);

        Long result = userService.getUserIdFromUserDetails(tokenUser);

        assertThat(result).isEqualTo(USER_ID);
        verify(repository).findByEmail(USER_EMAIL);
    }

    @Test
    @DisplayName("Должен выбросить RuntimeException для неизвестного типа UserDetails")
    void getUserIdFromUserDetails_InvalidUserDetails_ThrowRuntimeException() {
        UserDetails invalidDetails = mock(UserDetails.class);

        assertThatThrownBy(() -> userService.getUserIdFromUserDetails(invalidDetails))
                .isInstanceOf(RuntimeException.class);

        verify(repository, never()).findByEmail(any());
    }
}