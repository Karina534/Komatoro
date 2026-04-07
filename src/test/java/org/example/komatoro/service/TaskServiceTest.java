package org.example.komatoro.service;

import org.example.komatoro.dto.request.task.CreateTaskDTORequest;
import org.example.komatoro.dto.request.task.UpdateTaskDTORequest;
import org.example.komatoro.dto.response.task.TaskDTOResponse;
import org.example.komatoro.exeption.NotFoundException;
import org.example.komatoro.exeption.OwningDeniedException;
import org.example.komatoro.mapper.TaskMapper;
import org.example.komatoro.model.Task;
import org.example.komatoro.model.User;
import org.example.komatoro.repository.ITaskRepository;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//TODO: Добавить тесты если не CustomUser, а TokenUser
//TODO: Заменить в названии Entity на DTO
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private ITaskRepository taskRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private TaskMapper mapper;
    @InjectMocks
    private TaskService taskService;

    private static User testUser;
    private static CustomUserDetails testUserDetails;
    private Task testTask;
    private TaskDTOResponse testTaskResponse;
    private Long taskId = 1L;

    @BeforeAll
    static void setUp(){
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("testemail@mail.ru");

        testUserDetails = new CustomUserDetails(testUser);
    }

    @BeforeEach
    void setUpEach(){
        testTask = new Task("Title", "desc");
        testTask.setId(taskId);
        testTask.setActive(true);
        testTask.setUser(testUser);
        testTask.setCreatedAt(Instant.now());

        testTaskResponse = new TaskDTOResponse(testTask.getId(),
                testTask.getTitle(),
                testTask.getDescription(),
                testTask.isActive(),
                testTask.getCreatedAt());
    }

    @Test
    void createTask_PayloadIsValid_ReturnValidResponseEntity() {
        // given
        CreateTaskDTORequest taskDTORequest = new CreateTaskDTORequest("Title", "desc");
        Task mapped = new Task();
        mapped.setTitle("Title");
        mapped.setDescription("desc");

        when(this.userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(this.mapper.dtoToEntity(taskDTORequest)).thenReturn(mapped);
        when(this.taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task toSave = inv.getArgument(0);
            toSave.setId(taskId);
            return toSave;
        });
        when(this.mapper.toResponse(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            return new TaskDTOResponse(t.getId(),t.getTitle(), t.getDescription(), t.isActive(), t.getCreatedAt());
        });

        // when
        TaskDTOResponse response = this.taskService.createTask(testUserDetails, taskDTORequest);

        //then
        assertNotNull(response);
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(this.taskRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(testUser);
        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(response.title()).isEqualTo("Title");
        verify(this.userRepository).findByEmail("testemail@mail.ru");
        verify(this.mapper).dtoToEntity(taskDTORequest);
        verify(this.mapper).toResponse(any(Task.class));
        verifyNoMoreInteractions(this.taskRepository, this.userRepository, this.mapper);
    }

    @Test
    void createTask_UserNotFound_ThrowNotFound() {
        CreateTaskDTORequest taskDTORequest = new CreateTaskDTORequest("Title", "desc");

        when(this.userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> this.taskService.createTask(testUserDetails, taskDTORequest))
                .isInstanceOf(NotFoundException.class);

        verify(this.userRepository).findByEmail(testUser.getEmail());
        verifyNoMoreInteractions(this.taskRepository, this.mapper);
    }

    @Test
    void updateTask_PayloadIsValid_ReturnValidResponseEntity() {
        // given
        UpdateTaskDTORequest updateTaskDTORequest = new UpdateTaskDTORequest("New title", "new desc", false);
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.ofNullable(testTask));
        doAnswer(inv -> {
            Task target = inv.getArgument(1);
            target.setTitle(updateTaskDTORequest.title());
            target.setDescription(updateTaskDTORequest.description());
            target.setActive(updateTaskDTORequest.isActive());
            return null;
        }).when(this.mapper).updateEntityFromDto(updateTaskDTORequest, testTask);

        when(this.taskRepository.save(testTask)).thenReturn(testTask);
        when(this.mapper.toResponse(testTask)).thenReturn(new TaskDTOResponse(
                taskId,
                updateTaskDTORequest.title(),
                updateTaskDTORequest.description(),
                updateTaskDTORequest.isActive(),
                testTask.getCreatedAt()
        ));

        // when
        TaskDTOResponse result = taskService.updateTask(testUserDetails, taskId, updateTaskDTORequest);

        // then
        assertThat(result.title()).isEqualTo("New title");
        verify(this.taskRepository).findById(taskId);
        verify(this.mapper).updateEntityFromDto(updateTaskDTORequest, testTask);
        verify(this.taskRepository).save(testTask);
        verify(this.mapper).toResponse(testTask);
    }

    @Test
    void updateTask_TaskNotFound_ThrowNotFound(){
        UpdateTaskDTORequest updateTaskDTORequest = new UpdateTaskDTORequest("New title", "new desc", false);
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.taskService.updateTask(testUserDetails, taskId, updateTaskDTORequest))
                .isInstanceOf(NotFoundException.class);

        verify(this.taskRepository).findById(taskId);
        verifyNoMoreInteractions(this.taskRepository, this.userRepository, this.mapper);
    }

    @Test
    void updateTask_OwningInvalid_ThrowOwningDeniedException(){
        UpdateTaskDTORequest updateTaskDTORequest = new UpdateTaskDTORequest("New title", "new desc", false);
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.ofNullable(testTask));

        User fakeUser = new User();
        fakeUser.setId(3L);
        CustomUserDetails fakeDetails = new CustomUserDetails(fakeUser);

        assertThatThrownBy(() -> this.taskService.updateTask(fakeDetails, taskId, updateTaskDTORequest))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.taskRepository).findById(taskId);
        verifyNoMoreInteractions(this.taskRepository, this.mapper);
    }

    @Test
    void deleteTask_PayloadValid_NoReturn() {
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.ofNullable(testTask));

        this.taskService.deleteTask(testUserDetails, taskId);

        verify(this.taskRepository).findById(taskId);
        verify(this.taskRepository).delete(testTask);
    }

    @Test
    void deleteTask_OwningInvalid_ThrowOwningDeniedException() {
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.ofNullable(testTask));

        User fakeUser = new User();
        fakeUser.setId(3L);
        CustomUserDetails fakeDetails = new CustomUserDetails(fakeUser);

        assertThatThrownBy(() -> this.taskService.deleteTask(fakeDetails, taskId))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.taskRepository).findById(taskId);
        verifyNoMoreInteractions(this.taskRepository, this.mapper);
    }

    @Test
    void getTaskById_PayloadValid_ReturnValidResponseEntity() {
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.ofNullable(testTask));
        when(this.mapper.toResponse(testTask)).thenReturn(testTaskResponse);

        TaskDTOResponse result = this.taskService.getTaskById(testUserDetails, taskId);

        assertThat(result.taskId()).isEqualTo(taskId);
        verify(this.taskRepository).findById(taskId);
        verify(this.mapper).toResponse(testTask);
    }

    @Test
    void getTaskById_OwningInvalid_ThrowOwningDeniedException() {
        when(this.taskRepository.findById(taskId)).thenReturn(Optional.ofNullable(testTask));

        User fakeUser = new User();
        fakeUser.setId(3L);
        CustomUserDetails fakeDetails = new CustomUserDetails(fakeUser);

        assertThatThrownBy(() -> this.taskService.getTaskById(fakeDetails, taskId))
                .isInstanceOf(OwningDeniedException.class);

        verify(this.taskRepository).findById(taskId);
        verifyNoMoreInteractions(this.taskRepository, this.mapper, this.userRepository);
    }

    @Test
    void getAllTasksByUser_PayloadValid_ReturnValidResponseEntity() {
        Task task2 = new Task();
        task2.setUser(testUser);
        task2.setTitle("Task 2");
        task2.setDescription("Desc 2");

        when(this.taskRepository.findByUserId(testUser.getId())).thenReturn(List.of(testTask, task2));
        when(this.mapper.toResponse(testTask)).thenReturn(testTaskResponse);
        when(this.mapper.toResponse(task2)).thenReturn(new TaskDTOResponse(task2.getId(), task2.getTitle(), task2.getDescription(), true, Instant.now()));

        List<TaskDTOResponse> result = this.taskService.getAllTasksByUser(testUserDetails);

        assertThat(result).hasSize(2);
        verify(this.taskRepository).findByUserId(testUser.getId());
        verify(this.mapper).toResponse(testTask);
        verify(this.mapper).toResponse(task2);
    }

    // Спросить есть ли смысл в этом методе
    @Test
    void getAllTasksByUser_PayloadValid_ReturnValidEmptyListResponse() {
        User user = new User();
        user.setId(2L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(this.taskRepository.findByUserId(2L)).thenReturn(List.of());

        List<TaskDTOResponse> result = this.taskService.getAllTasksByUser(userDetails);

        assertThat(result).isEmpty();
        verify(this.taskRepository).findByUserId(2L);
        verifyNoMoreInteractions(this.mapper);
    }

    // Есть ли смысл добавить два варианта
    @Test
    void isExist_ReturnBoolean() {
        when(this.taskRepository.existsById(5L)).thenReturn(true);
        when(this.taskRepository.existsById(7L)).thenReturn(false);
        assertThat(taskService.isExist(5L)).isTrue();
        assertThat(taskService.isExist(7L)).isFalse();
        verify(taskRepository).existsById(5L);
        verify(taskRepository).existsById(7L);
    }

    @Test
    void isActive_PayloadValid_ReturnTrueFlag() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        assertThat(taskService.isActive(taskId)).isTrue();
    }

    @Test
    void isActive_PayloadValid_ReturnFalseFlag() {
        testTask.setActive(false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        assertThat(taskService.isActive(taskId)).isFalse();
    }

    @Test
    void isActive_TaskNotFound_ThrowNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.taskService.isActive(taskId))
                .isInstanceOf(NotFoundException.class);

        verify(this.taskRepository).findById(taskId);
        verifyNoMoreInteractions(this.taskRepository, this.userRepository, this.mapper);
    }
}