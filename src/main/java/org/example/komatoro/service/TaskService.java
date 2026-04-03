package org.example.komatoro.service;

import lombok.extern.slf4j.Slf4j;
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
import org.example.komatoro.security.jwt.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@Service
public class TaskService implements ITaskService{
    private final ITaskRepository taskRepository;
    private final IUserRepository userRepository;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskService(ITaskRepository taskRepository, IUserRepository userRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskDTOResponse createTask(UserDetails userDetails, CreateTaskDTORequest taskDTO) {
        User user = this.getUserFromUserDetails(userDetails);

        Task task = taskMapper.dtoToEntity(taskDTO);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskDTOResponse updateTask(UserDetails userDetails, Long taskId, UpdateTaskDTORequest taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(taskId, Task.class));

        owningTaskCheck(task, userDetails);

        taskMapper.updateEntityFromDto(taskDTO, task);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public TaskDTOResponse completeTask(UserDetails userDetails, Long taskId){
        return this.updateTask(userDetails, taskId,
                new UpdateTaskDTORequest(null, null, false));
    }

    @Override
    public TaskDTOResponse activateTask(UserDetails userDetails, Long taskId){
        return this.updateTask(userDetails, taskId,
                new UpdateTaskDTORequest(null, null, true));
    }

    @Override
    public void deleteTask(UserDetails userDetails, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(taskId, Task.class));

        owningTaskCheck(task, userDetails);

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    @Override
    public TaskDTOResponse getTaskById(UserDetails userDetails, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(taskId, Task.class));

        owningTaskCheck(task, userDetails);

        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TaskDTOResponse> getAllTasksByUser(UserDetails userDetails) {
        Long userId = this.getUserIdFromUserDetails(userDetails);

        return taskRepository.findByUserId(userId).stream().map(taskMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isExist(Long taskId){
        return taskRepository.existsById(taskId);
    }

    @Override
    public boolean isActive(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(taskId, Task.class))
                .isActive();
    }

    private TaskDTOResponse convertResponseDto(Task task){
        return new TaskDTOResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isActive(),
                task.getCreatedAt()
        );
    }

    private void owningTaskCheck(Task task, UserDetails userDetails){
        Long userId = this.getUserIdFromUserDetails(userDetails);

        if (!task.getUser().getId().equals(userId)){
            throw new OwningDeniedException();
        }
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails){
        Long userId;
        if (userDetails instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) userDetails).getUserId();

        } else if (userDetails instanceof TokenUser){
            User user = this.getUserFromUserDetails(userDetails);
            userId = user.getId();

        } else {
            log.warn("UserDetails is not an instance of CustomUserDetails or TokenUser. " +
                    "Unable to extract user information.");
            throw new RuntimeException("Invalid user details. Not an instance of CustomUserDetails or TokenUser.");
        }

        return userId;
    }

    private User getUserFromUserDetails(UserDetails userDetails){
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException(userDetails.getUsername(), User.class));
    }
}
