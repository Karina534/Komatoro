package org.example.komatoro.service;

import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.dto.TemporaryEntityDTO.UserDailyStatsDTO;
import org.example.komatoro.dto.request.tomatoSession.*;
import org.example.komatoro.dto.TemporaryEntityDTO.TomatoSessionDTO;
import org.example.komatoro.dto.response.task.TaskDTOResponse;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionDTOResponse;
import org.example.komatoro.dto.response.tomatoSession.TomatoSessionRecommendationDTOResponse;
import org.example.komatoro.dto.response.userSettings.UserDailyStatsDTOResponse;
import org.example.komatoro.dto.response.userSettings.UserSettingsDTOResponse;
import org.example.komatoro.exeption.*;
import org.example.komatoro.model.*;
import org.example.komatoro.repository.ITaskRepository;
import org.example.komatoro.repository.ITomatoSessionRepository;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.security.CustomUserDetails;
import org.example.komatoro.security.jwt.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Бизнес-логика для pomodoro/tomato сессий.
 * - Гарантирует одну RUNNING сессию на пользователя через per-user lock.
 * - При старте новой сессии прерывает (INTERRUPTED) предыдущую RUNNING.
 * - Сервер сам вычисляет actualMinutes и использует Instant.now().
 */
@Slf4j
@Transactional
@Service
public class TomatoSessionService implements ITomatoSessionService {
    private final ITomatoSessionRepository repository;
    private final IUserRepository userRepository;
    private final ITaskRepository taskRepository;
    private final ITaskService taskService;
    private final IUserSettingsService userSettingsService;
    private final IUserDailyStatsService userDailyStatsService;

    @Autowired
    public TomatoSessionService(ITomatoSessionRepository repository,
                                IUserRepository userRepository,
                                ITaskRepository taskRepository,
                                ITaskService taskService,
                                IUserSettingsService userSettingsService, IUserDailyStatsService userDailyStatsService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.userSettingsService = userSettingsService;
        this.userDailyStatsService = userDailyStatsService;
    }

    /**
     * Метод запускает сессию (для задачи и без), если у пользователя нет активных сессий
     * @param userDetails
     * @param sessionDTO
     * @return
     */
    @Override
    public TomatoSessionDTOResponse startTomatoSession(
            UserDetails userDetails,
            StartTomatoSessionDTORequest sessionDTO) {

        Long userId = this.getUserIdFromUserDetails(userDetails);

        // Проверка, что у пользователя еще нет запущенных сессий
        if (this.isAnyRunningTomatoSession(userId)){
            throw new RunningSessionAlreadyExistException(userId);
        }

        // Если томат запускается для задачи, нужно проверить, что она существует и она не завершена
        if (sessionDTO.taskId() != null) {
            if (!taskService.isExist(sessionDTO.taskId())) {
                throw new TaskNotFoundException(sessionDTO.taskId());
            }
            if (!taskService.isCompleted(sessionDTO.taskId())) {
                throw new TaskIsCompletedException(sessionDTO.taskId());
            }
        }

        // Время для таймера берется или из переданного dto сессии или из настроек пользователя pomodoroMinutes
        Integer intendedMinutes = sessionDTO.intendedMinutes() == null ?
                this.decideIntendedMinutes(userId) : sessionDTO.intendedMinutes();

        TomatoSession session = new TomatoSession();
        session.setUser(userRepository.getReferenceById(userId));
        if (sessionDTO.taskId() != null) {
            session.setTask(taskRepository.getReferenceById(sessionDTO.taskId()));
        }
        session.setIntendedMinutes(intendedMinutes);
        session.setType(sessionDTO.taskId() != null ? TomatoType.TASK : TomatoType.TIMER);

        TomatoSession saved = repository.save(session);
        return this.createResponseDTO(userId, sessionDTO.taskId(), saved);
    }

    @Override
    public TomatoSessionDTOResponse pauseTomatoSession(
            UserDetails userDetails,
            Long sessionId) {

        Long userId = this.getUserIdFromUserDetails(userDetails);

        // Получаем сессию по id
        TomatoSession session = repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Проверяем, чтобы сессия, которую мы хотим остановить, была в статусе RUNNING
        if (session.getStatus() != TomatoStatus.RUNNING){
            throw new InvalidSessionStatusException(session.getId(), session.getStatus());
        }

        // Проверяем что сессия принадлежит вызвавшему пользователю
        if (!session.getUser().getId().equals(userId)){
            throw new OwningDeniedException();
        }

        // Вычисляем активное время фокусировки в минутах
        Integer activeFocusMinutes = this.countActiveFocusTime(session.getLastResumeTime()) + session.getTotalActiveMinutes();

        session.setStatus(TomatoStatus.PAUSED);
        session.setTotalActiveMinutes(activeFocusMinutes);

        repository.save(session);

        return this.createResponseDTO(session);
    }

    @Override
    public TomatoSessionDTOResponse resumeTomatoSession(UserDetails userDetails, Long sessionId) {
        Long userId = this.getUserIdFromUserDetails(userDetails);

        // Получаем сессию по id
        TomatoSession session = repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Проверяем, чтобы сессия, которую мы хотим остановить, была в статусе PAUSED
        if (session.getStatus() != TomatoStatus.PAUSED){
            throw new InvalidSessionStatusException(session.getId(), session.getStatus());
        }

        // Проверяем что сессия принадлежит вызвавшему пользователю
        if (!session.getUser().getId().equals(userId)){
            throw new OwningDeniedException();
        }

        // Проверяем нет ли у пользователя RUNNING сессий
        if (this.isAnyRunningTomatoSession(userId)){
            throw new RunningSessionAlreadyExistException(userId);
        }

        session.setStatus(TomatoStatus.RUNNING);
        session.setLastResumeTime(Instant.now());

        TomatoSession saved = repository.save(session);
        return this.createResponseDTO(saved);
    }

    @Override
    public TomatoSessionDTOResponse extendTomatoSession(
            UserDetails userDetails,
            Long sessionId,
            ExtendTomatoSessionDTORequest tomatoSession) {

        Long userId = this.getUserIdFromUserDetails(userDetails);

        // Получаем сессию по id
        TomatoSession session = repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Проверяем, чтобы сессия, которую мы хотим увеличить, была в статусе RUNNING или PAUSED
        if (session.getStatus() != TomatoStatus.RUNNING && session.getStatus() != TomatoStatus.PAUSED){
            throw new InvalidSessionStatusException(session.getId(), session.getStatus());
        }

        // Проверяем что сессия принадлежит вызвавшему пользователю
        if (!session.getUser().getId().equals(userId)){
            throw new OwningDeniedException();
        }

        Integer addMinutes = tomatoSession.addMinutes() == null ? 1 : tomatoSession.addMinutes();
        // Проверяем, что после увеличения сессии количество минут не превысит ограничение
        Integer newIntendedMinutes = session.getIntendedMinutes() + addMinutes;
        if (newIntendedMinutes > 480){
            throw new SessionParametrValidationException(String.valueOf(newIntendedMinutes));
        }

        session.setIntendedMinutes(newIntendedMinutes);

        TomatoSession saved = repository.save(session);
        return this.createResponseDTO(saved);
    }

    @Override
    public TomatoSessionDTOResponse finishTomatoSession(
            UserDetails userDetails,
            FinishTomatoSessionDTORequest tomatoSession) {

        Long userId = this.getUserIdFromUserDetails(userDetails);

        // Получаем сессию по id
        TomatoSession session = repository.findById(tomatoSession.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(tomatoSession.sessionId()));

        // Проверяем, чтобы сессия, которую мы хотим остановить, была в статусе RUNNING или PAUSED
        if (session.getStatus() != TomatoStatus.RUNNING && session.getStatus() != TomatoStatus.PAUSED){
            throw new InvalidSessionStatusException(session.getId(), session.getStatus());
        }

        // Проверяем что сессия принадлежит вызвавшему пользователю
        if (!session.getUser().getId().equals(userId)){
            throw new OwningDeniedException();
        }

        // Если сессия была RUNNING и не была отдыхом, для нее нужно вычислить время фокусировки
        Integer activeFocusTime = session.getTotalActiveMinutes();
        if (session.getStatus() == TomatoStatus.RUNNING){
            activeFocusTime += this.countActiveFocusTime(session.getLastResumeTime());
        }

        session.setEndTime(Instant.now());
        session.setStatus(tomatoSession.status());
        session.setTotalActiveMinutes(activeFocusTime);

        TomatoSession saved = repository.save(session);

        // Если сессия была не для отдыха, нужно обновить статистику дня для пользователя
        UserDailyStats dailyStats = userDailyStatsService.getUserDailyStatsOrCreate(userId, LocalDate.now());
        if (session.getType().equals(TomatoType.TIMER) || session.getType().equals(TomatoType.TASK)){
            userDailyStatsService.increaseStats(
                    userId,
                    LocalDate.now(),
                    1,
                    activeFocusTime);
        }

        return this.createResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TomatoSessionDTOResponse> getCurrentRunningSession(UserDetails userDetails) {
        Long userId = this.getUserIdFromUserDetails(userDetails);
        return repository.findTomatoSessionByUserIdAndStatus(userId, TomatoStatus.RUNNING).map(this::createResponseDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public TomatoSessionDTOResponse getSession(UserDetails userDetails, Long sessionId) {
        Long userId = this.getUserIdFromUserDetails(userDetails);

        TomatoSession session = repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if(!session.getUser().getId().equals(userId)){
            throw new OwningDeniedException();
        }

        return this.createResponseDTO(session);
    }

    @Override
    public void deleteTomatoSession(UserDetails userDetails, Long sessionId) {
        Long userId = this.getUserIdFromUserDetails(userDetails);

        // Получаем сессию по id
        TomatoSession session = repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Проверяем что пользователь сессии совпадает с пользователем
        if(!session.getUser().getId().equals(userId)){
            log.warn("User to delete not their tomato session");
            throw new OwningDeniedException();
        }

        repository.delete(session);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isAnyRunningTomatoSession(Long userId) {
        return repository.findTomatoSessionByUserIdAndStatus(userId, TomatoStatus.RUNNING).isPresent();
    }

    @Transactional(readOnly = true)
    @Override
    public List<TomatoSessionDTOResponse> getAllUserSessions(UserDetails userDetails) {
        Long userId = this.getUserIdFromUserDetails(userDetails);
        return repository.findByUserId(userId).stream().map(this::createResponseDTO).toList();
    }

    @Override
    public TomatoSessionRecommendationDTOResponse recommendTomatoSession(UserDetails userDetails) {
        System.out.println("---------------------------------------");
        System.out.println("Start recommendation method in service");
        // Получаем пользователя и проверяем, что он существует
        Long userId = this.getUserIdFromUserDetails(userDetails);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Получаем настройки пользователя
        UserSettingsDTOResponse settings = userSettingsService.getUserSettingsByUserId(userId);

        // Получаем статистику пользователя для сегодняшнего дня
        UserDailyStats dailyStats = userDailyStatsService.getUserDailyStatsOrCreate(userId, LocalDate.now());

        // Если у пользователя еще не было рабочих сессий
        if (dailyStats.getPomodoroCount() == 0){
            System.out.println("-----------------------------------------------");
            System.out.println("User hadn't had sessions");
            return new TomatoSessionRecommendationDTOResponse(userId, TomatoType.TIMER);
        }

        // Если у пользователя последняя сессия была для отдыха, то следующая должна быть для работы
        Optional<TomatoSession> lastTomato = repository.findFirstByUserIdAndStatusInOrderByStartTimeDesc(
                userId,
                List.of(TomatoStatus.COMPLETED, TomatoStatus.INTERRUPTED));

        System.out.println("------------------------------------------------");
        System.out.println("last tomato: " + lastTomato);
        if (lastTomato.isEmpty() || lastTomato.get().getType().equals(TomatoType.SHORT_BREAK) ||
                lastTomato.get().getType().equals(TomatoType.LONG_BREAK)){
            return new TomatoSessionRecommendationDTOResponse(userId, TomatoType.TIMER);
        }

        System.out.println("--------------------------------");
        System.out.println("pomodoroCount: " + dailyStats.getPomodoroCount());
        System.out.println("long break interval: " + settings.longBreakInterval());

        // Если дошли до этого, то до этого была рабочая сессия и нужно выбрать тип перерыва
        if (dailyStats.getPomodoroCount() % settings.longBreakInterval() == 0){
            System.out.println("-----------------------------------------");
            System.out.println("Recommend long break");
            // Возвращаем длинный отдых
            return new TomatoSessionRecommendationDTOResponse(userId, TomatoType.LONG_BREAK);
        } else {
            System.out.println("-----------------------------------------");
            System.out.println("Recommend short break");
            // Возвращаем короткий отдых
            return new TomatoSessionRecommendationDTOResponse(userId, TomatoType.SHORT_BREAK);
        }
    }

    private Integer decideIntendedMinutes(Long id) {
        UserSettingsDTOResponse settingsDTOResponse = userSettingsService.getUserSettingsByUserId(id);
        return settingsDTOResponse.pomodoroMinutes();
    }

    private TomatoSessionDTOResponse createResponseDTO(Long userId, Long taskId, TomatoSession session){
        return new TomatoSessionDTOResponse(
                session.getId(),
                userId,
                taskId,
                session.getType(),
                session.getCreatedAt(),
                session.getStartTime(),
                session.getEndTime(),
                session.getIntendedMinutes(),
                session.getStatus(),
                session.getTotalActiveMinutes(),
                session.getLastResumeTime()
        );
    }

    private TomatoSessionDTOResponse createResponseDTO(TomatoSession session){
        return new TomatoSessionDTOResponse(
                session.getId(),
                session.getUser().getId(),
                session.getTask() == null ? null : session.getTask().getId(),
                session.getType(),
                session.getCreatedAt(),
                session.getStartTime(),
                session.getEndTime(),
                session.getIntendedMinutes(),
                session.getStatus(),
                session.getTotalActiveMinutes(),
                session.getLastResumeTime()
        );
    }

    private Integer countActiveFocusTime(Instant lastResumeTime){
        return Math.toIntExact(ChronoUnit.MINUTES.between(lastResumeTime, Instant.now()));
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails){
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getUserId();

        } else if (userDetails instanceof TokenUser){
            User user = this.getUserFromUserDetails(userDetails);
            return user.getId();

        } else {
            log.warn("UserDetails is not an instance of CustomUserDetails. Unable to extract user information.");
            throw new RuntimeException("Invalid user details. Not an instance of CustomUserDetails.");
        }
    }

    private User getUserFromUserDetails(UserDetails userDetails){
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));
    }
}
