package org.example.komatoro.service;

import org.example.komatoro.dto.response.userSettings.UserDailyStatsDTOResponse;
import org.example.komatoro.exeption.NotFoundException;
import org.example.komatoro.model.User;
import org.example.komatoro.model.UserDailyStats;
import org.example.komatoro.repository.IUserDailyStatsRepository;
import org.example.komatoro.repository.IUserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDailyStatsServiceTest {
    @Mock private IUserDailyStatsRepository repository;
    @Mock private IUserRepository userRepository;
    @InjectMocks private UserDailyStatsService userDailyStatsService;

    private static User testUser;
    private static UserDailyStats testStats;
    private final static Long USER_ID = 1L;
    private final static LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);
    private final static Integer BASE_POMODORO_COUNT = 0;
    private final static Integer BASE_FOCUS_MINUTES = 0;

    @BeforeAll
    static void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail("testemail@mail.ru");

        testStats = new UserDailyStats(testUser);
        testStats.setDate(TEST_DATE);
        testStats.setPomodoroCount(5);
        testStats.setFocusMinutes(120);
    }

    @Test
    @DisplayName("Должен успешно создать базовую статистику для пользователя")
    void createBasicStats_PayloadValid_ReturnResponseDTO() {
        // given
        doReturn(testUser).when(userRepository).getReferenceById(USER_ID);
        when(repository.save(any(UserDailyStats.class))).thenAnswer(inv -> {
            UserDailyStats toSave = inv.getArgument(0);
            toSave.setDate(TEST_DATE);
            return toSave;
        });

        // when
        UserDailyStatsDTOResponse response = userDailyStatsService.createBasicStats(USER_ID);

        // then
        assertNotNull(response);
        assertThat(response.date()).isEqualTo(TEST_DATE);
        assertThat(response.pomodoroCount()).isEqualTo(BASE_POMODORO_COUNT);
        assertThat(response.focusMinutes()).isEqualTo(BASE_FOCUS_MINUTES);

        verify(userRepository).getReferenceById(USER_ID);
        verify(repository).save(any(UserDailyStats.class));
    }

    @Test
    @DisplayName("Должен успешно увеличить количество помодоро")
    void increaseStats_IncreasePomodoroOnly_ReturnResponseDTO() {
        // given
        Integer addPomodoroCount = 3;
        Integer expectedPomodoroCount = testStats.getPomodoroCount() + addPomodoroCount;

        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        when(repository.save(any(UserDailyStats.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        UserDailyStatsDTOResponse response = userDailyStatsService.increaseStats(
                USER_ID, TEST_DATE, addPomodoroCount, null);

        // then
        assertNotNull(response);
        assertThat(response.pomodoroCount()).isEqualTo(expectedPomodoroCount);
        assertThat(response.focusMinutes()).isEqualTo(testStats.getFocusMinutes());

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository).save(testStats);
    }

    @Test
    @DisplayName("Должен успешно увеличить количество минут фокуса")
    void increaseStats_IncreaseFocusMinutesOnly_ReturnResponseDTO() {
        // given
        Integer addFocusMinutes = 30;
        Integer expectedFocusMinutes = testStats.getFocusMinutes() + addFocusMinutes;

        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        when(repository.save(any(UserDailyStats.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        UserDailyStatsDTOResponse response = userDailyStatsService.increaseStats(
                USER_ID, TEST_DATE, null, addFocusMinutes);

        // then
        assertNotNull(response);
        assertThat(response.pomodoroCount()).isEqualTo(testStats.getPomodoroCount());
        assertThat(response.focusMinutes()).isEqualTo(expectedFocusMinutes);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository).save(testStats);
    }

    @Test
    @DisplayName("Должен успешно увеличить количество помодоро")
    void increaseStats_IncreaseBothParameters_ReturnResponseDTO() {
        // given
        Integer addPomodoroCount = 3;
        Integer expectedPomodoroCount = testStats.getPomodoroCount() + addPomodoroCount;
        Integer addFocusMinutes = 30;
        Integer expectedFocusMinutes = testStats.getFocusMinutes() + addFocusMinutes;

        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        when(repository.save(any(UserDailyStats.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        UserDailyStatsDTOResponse response = userDailyStatsService.increaseStats(
                USER_ID, TEST_DATE, addPomodoroCount, addFocusMinutes);

        // then
        assertNotNull(response);
        assertThat(response.pomodoroCount()).isEqualTo(expectedPomodoroCount);
        assertThat(response.focusMinutes()).isEqualTo(expectedFocusMinutes);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository).save(testStats);
    }

    @Test
    @DisplayName("Должен выбросить IllegalArgumentException при отрицательном инкременте помодоро")
    void increaseStats_NegativePomodoroIncrement_ThrowIllegalArgumentException() {
        Integer addPomodoroCount = -1;
        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        assertThatThrownBy(() -> userDailyStatsService.increaseStats(
                USER_ID, TEST_DATE, addPomodoroCount, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository, never()).save(any(UserDailyStats.class));
    }

    @Test
    @DisplayName("Должен выбросить IllegalArgumentException при отрицательном инкременте минут фокуса")
    void increaseStats_NegativeFocusMinutesIncrement_ThrowIllegalArgumentException() {
        Integer addFocusMinutes = -5;
        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        assertThatThrownBy(() -> userDailyStatsService.increaseStats(
                USER_ID, TEST_DATE, null, addFocusMinutes))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository, never()).save(any(UserDailyStats.class));
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException когда статистика не найдена")
    void increaseStats_StatsNotFound_ThrowNotFoundException() {
        doReturn(Optional.empty()).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        assertThatThrownBy(() -> userDailyStatsService.increaseStats(
                USER_ID, TEST_DATE, 1, null))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository, never()).save(any(UserDailyStats.class));
    }

    @Test
    @DisplayName("Должен успешно удалить статистику")
    void deleteStats_PayloadValid_DeleteSuccessfully() {
        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        doNothing().when(repository).delete(testStats);

        userDailyStatsService.deleteStats(USER_ID, TEST_DATE);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository).delete(testStats);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при удалении несуществующей статистики")
    void deleteStats_StatsNotFound_ThrowNotFoundException() {
        doReturn(Optional.empty()).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        assertThatThrownBy(() -> userDailyStatsService.deleteStats(USER_ID, TEST_DATE))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository, never()).delete(any(UserDailyStats.class));
    }

    @Test
    @DisplayName("Должен успешно получить статистику")
    void getUserDailyStats_StatsExist_ReturnResponseDTO() {
        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        UserDailyStatsDTOResponse response = userDailyStatsService.getUserDailyStats(USER_ID, TEST_DATE);

        assertNotNull(response);
        assertThat(response.date()).isEqualTo(testStats.getDate());
        assertThat(response.pomodoroCount()).isEqualTo(testStats.getPomodoroCount());
        assertThat(response.focusMinutes()).isEqualTo(testStats.getFocusMinutes());

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException при получении несуществующей статистики")
    void getUserDailyStats_StatsNotFound_ThrowNotFoundException() {
        doReturn(Optional.empty()).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        assertThatThrownBy(() -> userDailyStatsService.getUserDailyStats(USER_ID, TEST_DATE))
                .isInstanceOf(NotFoundException.class);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
    }

    @Test
    @DisplayName("Должен вернуть существующую статистику при вызове getUserDailyStatsOrCreate")
    void getUserDailyStatsOrCreate_StatsExists_ReturnExistingStats() {
        doReturn(Optional.of(testStats)).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);

        UserDailyStats result = userDailyStatsService.getUserDailyStatsOrCreate(USER_ID, TEST_DATE);

        assertNotNull(result);
        assertThat(result).isEqualTo(testStats);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(repository, never()).save(any(UserDailyStats.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Должен создать новую статистику при вызове getUserDailyStatsOrCreate если она не существует")
    void getUserDailyStatsOrCreate_StatsNotExist_CreateAndReturnNewStats() {
        // given
        doReturn(Optional.empty()).when(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        doReturn(testUser).when(userRepository).getReferenceById(USER_ID);
        when(repository.save(any(UserDailyStats.class))).thenAnswer(inv -> {
            UserDailyStats toSave = inv.getArgument(0);
            toSave.setDate(TEST_DATE);
            return toSave;
        });

        // when
        UserDailyStats result = userDailyStatsService.getUserDailyStatsOrCreate(USER_ID, TEST_DATE);

        // then
        assertNotNull(result);
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getPomodoroCount()).isEqualTo(BASE_POMODORO_COUNT);
        assertThat(result.getFocusMinutes()).isEqualTo(BASE_FOCUS_MINUTES);

        verify(repository).findByUserIdAndDate(USER_ID, TEST_DATE);
        verify(userRepository).getReferenceById(USER_ID);
        verify(repository).save(any(UserDailyStats.class));
    }
}