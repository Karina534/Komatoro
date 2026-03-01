package org.example.komatoro.service;

import org.example.komatoro.dto.request.task.CreateTaskDTORequest;
import org.example.komatoro.dto.request.task.UpdateTaskDTORequest;
import org.example.komatoro.dto.response.task.TaskDTOResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ITaskService {
    TaskDTOResponse createTask(UserDetails userDetails, CreateTaskDTORequest taskDTO);
    TaskDTOResponse updateTask(UserDetails userDetails, Long taskId, UpdateTaskDTORequest taskDTO);
    void deleteTask(UserDetails userDetails, Long taskId);
    TaskDTOResponse getTaskById(UserDetails userDetails, Long taskId);
    List<TaskDTOResponse> getAllTasksByUser(UserDetails userDetails);
    TaskDTOResponse activateTask(UserDetails userDetails, Long taskId);
    TaskDTOResponse completeTask(UserDetails userDetails, Long taskId);
    boolean isExist(Long taskId);
    boolean isCompleted(Long taskId);
}
