package org.example.komatoro.service;

import org.aspectj.lang.annotation.Before;
import org.example.komatoro.dto.request.tomatoSession.ExtendTomatoSessionDTORequest;
import org.example.komatoro.dto.request.tomatoSession.FinishTomatoSessionDTORequest;
import org.example.komatoro.dto.request.tomatoSession.StartTomatoSessionDTORequest;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionDTOResponse;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionRecommendationDTOResponse;
import org.example.komatoro.dto.response.userSettings.UserSettingsDTOResponse;
import org.example.komatoro.exeption.*;
import org.example.komatoro.model.*;
import org.example.komatoro.repository.ITaskRepository;
import org.example.komatoro.repository.ITomatoSessionRepository;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.security.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TomatoSessionServiceTest {
    @Mock private ITomatoSessionRepository repository;
    @Mock private IUserRepository userRepository;
    @Mock private ITaskRepository taskRepository;
    @Mock private ITaskService taskService;
    @Mock private IUserSettingsService userSettingsService;
    @Mock private IUserDailyStatsService userDailyStatsService;
    @Mock private UserService userService;
    @InjectMocks private TomatoSessionService tomatoSessionService;

    private static User testUser;
    private static CustomUserDetails testUserDetails;
    private static UserSettings testSettings;
    private static UserDailyStats testStats;
    private static Task testTask;
    private TomatoSessionDTOResponse response;
    private static UserSettingsDTOResponse settingsDTOResponse;
    private TomatoSession testSession;
    private final static Long SESSION_ID = 1L;
    private final static Long TASK_ID = 1L;
    private final static Long USER_ID = 1L;
    private final static Integer baseIntendedMinuted = 25;

    @BeforeAll
    static void setUp(){
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail("testemail@mail.ru");

        testUserDetails = new CustomUserDetails(testUser);
        testSettings = new UserSettings(testUser);
        testStats = new UserDailyStats(testUser);

        testTask = new Task("Title", "Desc");
        testTask.setId(TASK_ID);

        settingsDTOResponse = new UserSettingsDTOResponse(
                testSettings.getPomodoroMinutes(),
                testSettings.getLongBreakMinutes(),
                testSettings.getShortBreakMinutes(),
                testSettings.getLongBreakInterval()
        );
    }

    @BeforeEach
    void setUpEach(){
        testSession = new TomatoSession(testUser);
        response = new TomatoSessionDTOResponse(
                SESSION_ID,
                testUser.getId(),
                testTask.getId(),
                testSession.getType(),
                testSession.getCreatedAt(),
                testSession.getStartTime(),
                testSession.getEndTime(),
                testSession.getIntendedMinutes(),
                testSession.getStatus(),
                testSession.getTotalActiveMinutes(),
                testSession.getLastResumeTime()
        );
    }


    @Test
    @DisplayName("Должен успешно создать сессию для задачи с временем из настроек")
    void startTomatoSessionWithTaskNoTime_PayloadValid_ReturnResponseDTO() {
        // given
        StartTomatoSessionDTORequest request = new StartTomatoSessionDTORequest(
                TASK_ID,
                null
        );

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
        doReturn(true).when(this.taskService).isExist(TASK_ID);
        doReturn(true).when(this.taskService).isActive(TASK_ID);
        doReturn(settingsDTOResponse).when(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        doReturn(testUser).when(this.userRepository).getReferenceById(USER_ID);
        doReturn(testTask).when(this.taskRepository).getReferenceById(TASK_ID);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            TomatoSession toSave = inv.getArgument(0);
            toSave.setId(SESSION_ID);
            return toSave;
        });

        // when
        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.startTomatoSession(
                testUserDetails,
                request
        );

        // then
        assertNotNull(dtoResponse);
        assertThat(dtoResponse.type()).isEqualTo(TomatoType.TASK);
        assertThat(dtoResponse.taskId()).isEqualTo(TASK_ID);
        assertThat(dtoResponse.intendedMinutes()).isEqualTo(testSettings.getPomodoroMinutes());
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.RUNNING);
        verify(this.repository).findTomatoSessionByUserIdAndStatus(testUser.getId(), TomatoStatus.RUNNING);
        verify(this.repository).save(any(TomatoSession.class));
        verify(this.userSettingsService).getUserSettingsByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Должен успешно создать сессию без задачи с временем из запроса")
    void startTomatoSessionWithoutTaskWithTime_PayloadValid_ReturnResponseDTO() {
        // given
        Integer intendedMinutes = 35;
        StartTomatoSessionDTORequest request = new StartTomatoSessionDTORequest(
                null, intendedMinutes);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
        doReturn(testUser).when(this.userRepository).getReferenceById(USER_ID);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            TomatoSession toSave = inv.getArgument(0);
            toSave.setId(SESSION_ID);
            return toSave;
        });

        // when
        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.startTomatoSession(
                testUserDetails,
                request
        );

        // then
        assertNotNull(dtoResponse);
        assertThat(dtoResponse.intendedMinutes()).isEqualTo(intendedMinutes);
        assertThat(dtoResponse.type()).isEqualTo(TomatoType.TIMER);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.RUNNING);
        verify(this.repository).findTomatoSessionByUserIdAndStatus(testUser.getId(), TomatoStatus.RUNNING);
        verify(this.repository).save(any(TomatoSession.class));
        verifyNoInteractions(this.taskService);
        verifyNoInteractions(this.taskRepository);
    }

    @Test
    @DisplayName("Должен выбросить RunningSessionAlreadyExistException так как у пользователя уже есть активная сессия")
    void startTomatoSession_ActiveSessionAlreadyExist_ThrowRunningSessionAlreadyExistException(){
        StartTomatoSessionDTORequest request = new StartTomatoSessionDTORequest(
                TASK_ID,
                null
        );

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);

        assertThatThrownBy(() -> this.tomatoSessionService
                .startTomatoSession(testUserDetails, request))
                .isInstanceOf(RunningSessionAlreadyExistException.class);

        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException для переданной задачи")
    void startTomatoSessionWithTask_TaskNotExist_ThrowNotFoundException(){
        StartTomatoSessionDTORequest request = new StartTomatoSessionDTORequest(
                TASK_ID,
                null
        );

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
        doReturn(false).when(this.taskService).isExist(TASK_ID);

        assertThatThrownBy(() -> this.tomatoSessionService
                .startTomatoSession(testUserDetails, request))
                .isInstanceOf(NotFoundException.class);

        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException для переданной задачи")
    void startTomatoSessionWithTask_TaskNotActive_ThrowTaskIsCompletedException(){
        StartTomatoSessionDTORequest request = new StartTomatoSessionDTORequest(
                TASK_ID,
                null
        );

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
        doReturn(true).when(this.taskService).isExist(TASK_ID);
        doReturn(false).when(this.taskService).isActive(TASK_ID);

        assertThatThrownBy(() -> this.tomatoSessionService
                .startTomatoSession(testUserDetails, request))
                .isInstanceOf(TaskIsCompletedException.class);

        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно остановить сессию")
    void pauseTomatoSession_PayloadValid_ReturnResponseDTO() {
        testSession.setLastResumeTime(Instant.now().minusSeconds(120));
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.<TomatoSession>getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.pauseTomatoSession(testUserDetails, SESSION_ID);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.PAUSED);
        assertThat(dtoResponse.totalActiveMinutes()).isEqualTo(2);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение NotFoundException для несуществующей сессии")
    void pauseTomatoSession_SessionNotExist_ThrowNotFoundException(){
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.pauseTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение InvalidSessionStatusException при попытке остановить не запущенную сессию")
    void pauseTomatoSession_InvalidStatus_ThrowInvalidSessionStatusException(){
        testSession.setStatus(TomatoStatus.INTERRUPTED);
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.pauseTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(InvalidSessionStatusException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение OwningDeniedException если сессия не принадлежит переданному пользователю")
    void pauseTomatoSession_WrongUser_ThrowOwningDeniedException(){
        doReturn(USER_ID + 4).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.pauseTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно возобновить сессию")
    void resumeTomatoSession_PayloadValid_ReturnResponseDTO() {
        testSession.setStatus(TomatoStatus.PAUSED);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        doReturn(Optional.empty()).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.<TomatoSession>getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.resumeTomatoSession(testUserDetails, SESSION_ID);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.RUNNING);
        assertNotNull(dtoResponse.lastResumeTime());

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение NotFoundException для несуществующей сессии")
    void resumeTomatoSession_SessionNotExist_ThrowNotFoundException() {
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.resumeTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение InvalidSessionStatusException при попытке возобновить не остановленную сессию")
    void resumeTomatoSession_InvalidStatus_ThrowInvalidSessionStatusException() {
        testSession.setStatus(TomatoStatus.INTERRUPTED);
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.resumeTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(InvalidSessionStatusException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение OwningDeniedException если сессия не принадлежит переданному пользователю")
    void resumeTomatoSession_WrongUser_ThrowOwningDeniedException(){
        testSession.setStatus(TomatoStatus.PAUSED);
        doReturn(USER_ID + 4).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.resumeTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение RunningSessionAlreadyExistException если у пользователя есть запущенная сессия")
    void resumeTomatoSession_ActiveSessionAlreadyExist_ThrowRunningSessionAlreadyExistException() {
        testSession.setStatus(TomatoStatus.PAUSED);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        doReturn(Optional.of(testSession)).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);

        assertThatThrownBy(() -> this.tomatoSessionService.resumeTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(RunningSessionAlreadyExistException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно увеличить длительность сессии по умолчанию на 1 для запущенной сессии")
    void extendTomatoSession_PayloadValidDefault_ReturnResponseDTO() {
        testSession.setId(SESSION_ID);
        ExtendTomatoSessionDTORequest request = new ExtendTomatoSessionDTORequest(null, null);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService
                .extendTomatoSession(testUserDetails, SESSION_ID, request);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.RUNNING);
        assertThat(dtoResponse.intendedMinutes()).isEqualTo(baseIntendedMinuted+1);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно увеличить длительность сессии на переданное значение для остановленной сессии")
    void extendTomatoSession_PayloadValid_ReturnResponseDTO() {
        testSession.setId(SESSION_ID);
        testSession.setStatus(TomatoStatus.PAUSED);
        int addMin = 4;
        ExtendTomatoSessionDTORequest request = new ExtendTomatoSessionDTORequest(null, addMin);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService
                .extendTomatoSession(testUserDetails, SESSION_ID, request);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.PAUSED);
        assertThat(dtoResponse.intendedMinutes()).isEqualTo(baseIntendedMinuted+addMin);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение NotFoundException для несуществующей сессии")
    void extendTomatoSession_SessionNotExist_ThrowNotFoundException() {
        ExtendTomatoSessionDTORequest request = new ExtendTomatoSessionDTORequest(null, null);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.extendTomatoSession(testUserDetails, SESSION_ID, request))
                .isInstanceOf(NotFoundException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение InvalidSessionStatusException при попытке увеличить не запущенную или остановленную сессию")
    void extendTomatoSession_InvalidStatus_ThrowInvalidSessionStatusException() {
        ExtendTomatoSessionDTORequest request = new ExtendTomatoSessionDTORequest(null, null);
        testSession.setStatus(TomatoStatus.INTERRUPTED);
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.extendTomatoSession(testUserDetails, SESSION_ID, request))
                .isInstanceOf(InvalidSessionStatusException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение OwningDeniedException если сессия не принадлежит переданному пользователю")
    void extendTomatoSession_WrongUser_ThrowOwningDeniedException(){
        ExtendTomatoSessionDTORequest request = new ExtendTomatoSessionDTORequest(null, null);
        testSession.setStatus(TomatoStatus.PAUSED);
        doReturn(USER_ID + 4).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.extendTomatoSession(testUserDetails, SESSION_ID, request))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение SessionParametrValidationException если сессия превысит 480 минут после увеличения")
    void extendTomatoSession_WrongUser_ThrowSessionParametrValidationException(){
        ExtendTomatoSessionDTORequest request = new ExtendTomatoSessionDTORequest(null, 480);
        testSession.setStatus(TomatoStatus.PAUSED);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.extendTomatoSession(testUserDetails, SESSION_ID, request))
                .isInstanceOf(SessionParametrValidationException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно завершить запущенную сессию для времени работы")
    void finishTomatoSessionForTimerRunning_PayloadValid_ReturnResponseDTO() {
        testSession.setLastResumeTime(Instant.now().minusSeconds(120));
        testSession.setTotalActiveMinutes(2);
        UserDailyStats testDailyStats = new UserDailyStats(testUser);
        FinishTomatoSessionDTORequest request = new FinishTomatoSessionDTORequest(SESSION_ID, TomatoStatus.COMPLETED);
        LocalDate now = LocalDate.now();

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        doReturn(testDailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, now);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.finishTomatoSession(testUserDetails, request);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.COMPLETED);
        assertNotNull(dtoResponse.endTime());
        assertThat(dtoResponse.totalActiveMinutes()).isEqualTo(4);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(testSession);
        verify(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, now);
        verify(this.userDailyStatsService).increaseStats(USER_ID, now, 1, 4);
    }

    @Test
    @DisplayName("Должен успешно завершить остановленную сессию для времени работы")
    void finishTomatoSessionForTimerPaused_PayloadValid_ReturnResponseDTO() {
        testSession.setStatus(TomatoStatus.PAUSED);
        testSession.setLastResumeTime(Instant.now().minusSeconds(120));
        testSession.setTotalActiveMinutes(2);
        UserDailyStats testDailyStats = new UserDailyStats(testUser);
        FinishTomatoSessionDTORequest request = new FinishTomatoSessionDTORequest(SESSION_ID, TomatoStatus.INTERRUPTED);
        LocalDate now = LocalDate.now();

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        doReturn(testDailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, now);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.finishTomatoSession(testUserDetails, request);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.INTERRUPTED);
        assertNotNull(dtoResponse.endTime());
        assertThat(dtoResponse.totalActiveMinutes()).isEqualTo(2);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(testSession);
        verify(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, now);
        verify(this.userDailyStatsService).increaseStats(USER_ID, now, 1, 2);
    }

    @Test
    @DisplayName("Должен успешно завершить запущенную сессию для отдыха")
    void finishTomatoSessionForRestRunning_PayloadValid_ReturnResponseDTO() {
        testSession.setType(TomatoType.SHORT_BREAK);
        testSession.setLastResumeTime(Instant.now().minusSeconds(120));
        testSession.setTotalActiveMinutes(0);
        UserDailyStats testDailyStats = new UserDailyStats(testUser);
        FinishTomatoSessionDTORequest request = new FinishTomatoSessionDTORequest(SESSION_ID, TomatoStatus.INTERRUPTED);
        LocalDate now = LocalDate.now();

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        when(this.repository.save(any(TomatoSession.class))).thenAnswer(inv -> {
            return inv.getArgument(0);
        });

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.finishTomatoSession(testUserDetails, request);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.status()).isEqualTo(TomatoStatus.INTERRUPTED);
        assertNotNull(dtoResponse.endTime());
        assertThat(dtoResponse.totalActiveMinutes()).isEqualTo(2);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).save(testSession);
        verify(this.userDailyStatsService, never()).getUserDailyStatsOrCreate(USER_ID, now);
        verify(this.userDailyStatsService, never()).increaseStats(USER_ID, now, 1, 2);
    }

    @Test
    @DisplayName("Должен выбросить исключение NotFoundException для несуществующей сессии")
    void finishTomatoSession_SessionNotExist_ThrowNotFoundException(){
        FinishTomatoSessionDTORequest request = new FinishTomatoSessionDTORequest(SESSION_ID, TomatoStatus.INTERRUPTED);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.finishTomatoSession(testUserDetails, request))
                .isInstanceOf(NotFoundException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение InvalidSessionStatusException при попытке увеличить не запущенную или остановленную сессию")
    void finishTomatoSession_InvalidStatus_ThrowInvalidSessionStatusException() {
        FinishTomatoSessionDTORequest request = new FinishTomatoSessionDTORequest(SESSION_ID, TomatoStatus.INTERRUPTED);
        testSession.setStatus(TomatoStatus.INTERRUPTED);
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.finishTomatoSession(testUserDetails, request))
                .isInstanceOf(InvalidSessionStatusException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение OwningDeniedException, если сессия не принадлежит переданному пользователю")
    void finishTomatoSession_WrongUser_ThrowOwningDeniedException(){
        FinishTomatoSessionDTORequest request = new FinishTomatoSessionDTORequest(SESSION_ID, TomatoStatus.INTERRUPTED);
        doReturn(USER_ID + 4).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.finishTomatoSession(testUserDetails, request))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).save(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно получить запущенную сессию, когда она есть")
    void getCurrentRunningSession_PayloadValidExist_ReturnResponseDTO() {
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);

        Optional<TomatoSessionDTOResponse> dtoResponse = this.tomatoSessionService.getCurrentRunningSession(testUserDetails);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.isEmpty()).isFalse();
        assertThat(dtoResponse.get().id()).isEqualTo(SESSION_ID);

        verify(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional, когда сессии нет")
    void getCurrentRunningSession_PayloadValidNotExist_ReturnResponseDTO() {
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);

        Optional<TomatoSessionDTOResponse> dtoResponse = this.tomatoSessionService.getCurrentRunningSession(testUserDetails);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.isEmpty()).isTrue();

        verify(this.repository).findTomatoSessionByUserIdAndStatus(USER_ID, TomatoStatus.RUNNING);
    }

    @Test
    @DisplayName("Должен успешно вернуть сесстю по id")
    void getSession_PayloadValid_ReturnResponseDTO() {
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        TomatoSessionDTOResponse dtoResponse = this.tomatoSessionService.getSession(testUserDetails, SESSION_ID);

        assertNotNull(dtoResponse);
        assertThat(dtoResponse.id()).isEqualTo(SESSION_ID);
        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(USER_ID);
    }

    @Test
    @DisplayName("Должен выбросить исключение NotFoundException для несуществующей сессии")
    void getSession_SessionNotExist_ThrowNotFoundException() {
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.getSession(testUserDetails, SESSION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
    }

    @Test
    @DisplayName("Должен выбросить исключение OwningDeniedException, если сессия не принадлежит переданному пользователю")
    void getSession_WrongUser_ThrowOwningDeniedException(){
        doReturn(USER_ID + 4).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.getSession(testUserDetails, SESSION_ID))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
    }

    @Test
    @DisplayName("Должен успешно удалить сессию")
    void deleteTomatoSession_PayloadValid_ReturnResponseDTO() {
        testSession.setId(SESSION_ID);
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);
        doNothing().when(this.repository).delete(any(TomatoSession.class));

        this.tomatoSessionService.deleteTomatoSession(testUserDetails, SESSION_ID);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository).delete(any(TomatoSession.class));

        verify(this.repository).delete(argThat(session ->
                session.getId().equals(SESSION_ID) &&
                        session.getUser().getId().equals(USER_ID)
        ));
    }

    @Test
    @DisplayName("Должен выбросить исключение NotFoundException для несуществующей сессии")
    void deleteTomatoSession_SessionNotExist_ThrowNotFoundException() {
        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.empty()).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.deleteTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).delete(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение OwningDeniedException, если сессия не принадлежит переданному пользователю")
    void deleteTomatoSession_WrongUser_ThrowOwningDeniedException() {
        doReturn(USER_ID + 4).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(Optional.of(testSession)).when(this.repository).findById(SESSION_ID);

        assertThatThrownBy(() -> this.tomatoSessionService.deleteTomatoSession(testUserDetails, SESSION_ID))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findById(SESSION_ID);
        verify(this.repository, never()).delete(any(TomatoSession.class));
    }

    @Test
    @DisplayName("Должен успешно вернуть список всех сессий пользователя")
    void getAllUserSessions_PayloadValidTwoSessions_ReturnResponseList() {
        testSession.setId(SESSION_ID);
        TomatoSession testSession2 = new TomatoSession(testUser);
        testSession2.setId(SESSION_ID+1);
        List<TomatoSession> sessions = List.of(testSession, testSession2);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(sessions).when(this.repository).findByUserId(USER_ID);

        List<TomatoSessionDTOResponse> result = this.tomatoSessionService.getAllUserSessions(testUserDetails);

        assertNotNull(result);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(SESSION_ID);
        assertThat(result.get(0).status()).isEqualTo(TomatoStatus.RUNNING);
        assertThat(result.get(1).id()).isEqualTo(SESSION_ID+1);
        assertThat(result.get(1).status()).isEqualTo(TomatoStatus.RUNNING);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Должен успешно вернуть пустой список всех сессий пользователя")
    void getAllUserSessions_PayloadValidNoSessions_ReturnResponseList() {
        List<TomatoSession> emptySessions = List.of();

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(emptySessions).when(this.repository).findByUserId(USER_ID);

        List<TomatoSessionDTOResponse> result = this.tomatoSessionService.getAllUserSessions(testUserDetails);

        assertNotNull(result);
        assertThat(result).isEmpty();
        assertThat(result).isInstanceOf(List.class);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.repository).findByUserId(USER_ID);
    }

    @Test
    void recommendTomatoSession() {
    }

    @Test
    @DisplayName("Должен предложить рабочую сессию, если у пользователя ещё не было сессий")
    void recommendTomatoSession_UserHasNoSessions_ReturnTimerRecommendation() {
        // given
        UserDailyStats dailyStats = new UserDailyStats(testUser);
        UserSettingsDTOResponse settingsResponse = new UserSettingsDTOResponse(25,10, 5, 4);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(settingsResponse).when(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        doReturn(dailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());

        // when
        TomatoSessionRecommendationDTOResponse result = this.tomatoSessionService.recommendTomatoSession(testUserDetails);

        // then
        assertNotNull(result);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.type()).isEqualTo(TomatoType.TIMER);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        verify(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        verify(this.repository, never()).findFirstByUserIdAndStatusInOrderByStartTimeDesc(any(), any());
    }

    @Test
    @DisplayName("Должен предложить рабочую сессию, если последняя сессия была коротким отдыхом")
    void recommendTomatoSession_LastSessionWasShortBreak_ReturnTimerRecommendation() {
        // given
        UserDailyStats dailyStats = new UserDailyStats(testUser);
        dailyStats.setPomodoroCount(3);
        UserSettingsDTOResponse settingsResponse = new UserSettingsDTOResponse(25,10, 5, 4);

        testSession.setId(SESSION_ID);
        testSession.setType(TomatoType.SHORT_BREAK);
        testSession.setStatus(TomatoStatus.COMPLETED);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(settingsResponse).when(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        doReturn(dailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        doReturn(Optional.of(testSession)).when(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(
                eq(USER_ID), anyList());

        // when
        TomatoSessionRecommendationDTOResponse result = this.tomatoSessionService.recommendTomatoSession(testUserDetails);

        // then
        assertNotNull(result);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.type()).isEqualTo(TomatoType.TIMER);

        verify(this.userService).getUserIdFromUserDetails(testUserDetails);
        verify(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        verify(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        verify(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(eq(USER_ID), anyList());
    }

    @Test
    @DisplayName("Должен предложить рабочую сессию, если последняя сессия была длинным отдыхом")
    void recommendTomatoSession_LastSessionWasLongBreak_ReturnTimerRecommendation() {
        // given
        UserDailyStats dailyStats = new UserDailyStats(testUser);
        dailyStats.setPomodoroCount(5);
        UserSettingsDTOResponse settingsResponse = new UserSettingsDTOResponse(25,10, 5, 4);

        testSession.setId(SESSION_ID);
        testSession.setType(TomatoType.LONG_BREAK);
        testSession.setStatus(TomatoStatus.COMPLETED);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(settingsResponse).when(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        doReturn(dailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        doReturn(Optional.of(testSession)).when(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(
                eq(USER_ID), anyList());

        // when
        TomatoSessionRecommendationDTOResponse result = this.tomatoSessionService.recommendTomatoSession(testUserDetails);

        // then
        assertNotNull(result);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.type()).isEqualTo(TomatoType.TIMER);

        verify(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(eq(USER_ID), anyList());
    }

    @Test
    @DisplayName("Должен предложить короткий отдых, если количество помодор не кратно интервалу длинного отдыха")
    void recommendTomatoSession_NotReachedLongBreakInterval_ReturnShortBreakRecommendation() {
        // given
        UserDailyStats dailyStats = new UserDailyStats(testUser);
        dailyStats.setPomodoroCount(2);
        UserSettingsDTOResponse settingsResponse = new UserSettingsDTOResponse(25,15, 5, 4);

        testSession.setId(SESSION_ID);
        testSession.setType(TomatoType.TIMER);
        testSession.setStatus(TomatoStatus.COMPLETED);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(settingsResponse).when(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        doReturn(dailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        doReturn(Optional.of(testSession)).when(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(
                eq(USER_ID), anyList());

        // when
        TomatoSessionRecommendationDTOResponse result = this.tomatoSessionService.recommendTomatoSession(testUserDetails);

        // then
        assertNotNull(result);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.type()).isEqualTo(TomatoType.SHORT_BREAK);

        verify(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        verify(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        verify(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(eq(USER_ID), anyList());
    }

    @Test
    @DisplayName("Должен предложить длинный отдых, если количество помодор кратно интервалу длинного отдыха")
    void recommendTomatoSession_ReachedLongBreakInterval_ReturnLongBreakRecommendation() {
        // given
        UserDailyStats dailyStats = new UserDailyStats(testUser);
        dailyStats.setPomodoroCount(4);
        UserSettingsDTOResponse settingsResponse = new UserSettingsDTOResponse(25,15, 5, 4);

        testSession.setId(SESSION_ID);
        testSession.setType(TomatoType.TIMER);
        testSession.setStatus(TomatoStatus.COMPLETED);

        doReturn(USER_ID).when(this.userService).getUserIdFromUserDetails(testUserDetails);
        doReturn(settingsResponse).when(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        doReturn(dailyStats).when(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        doReturn(Optional.of(testSession)).when(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(
                eq(USER_ID), anyList());

        // when
        TomatoSessionRecommendationDTOResponse result = this.tomatoSessionService.recommendTomatoSession(testUserDetails);

        // then
        assertNotNull(result);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.type()).isEqualTo(TomatoType.LONG_BREAK);

        verify(this.userSettingsService).getUserSettingsByUserId(USER_ID);
        verify(this.userDailyStatsService).getUserDailyStatsOrCreate(USER_ID, LocalDate.now());
        verify(this.repository).findFirstByUserIdAndStatusInOrderByStartTimeDesc(eq(USER_ID), anyList());
    }
}