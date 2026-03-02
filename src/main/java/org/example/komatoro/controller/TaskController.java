package org.example.komatoro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.dto.request.task.CreateTaskDTORequest;
import org.example.komatoro.dto.TemporaryEntityDTO.TaskDTO;
import org.example.komatoro.dto.request.task.UpdateTaskDTORequest;
import org.example.komatoro.dto.response.task.TaskDTOResponse;
import org.example.komatoro.security.CustomUserDetails;
import org.example.komatoro.service.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final ITaskService taskService;

    @Autowired
    public TaskController(ITaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task for a user")
    @PostMapping
    public ResponseEntity<TaskDTOResponse> createTask(
            @Valid @RequestBody CreateTaskDTORequest createTaskDTO,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TaskDTOResponse created = taskService.createTask(userDetails, createTaskDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TaskDTOResponse>> getAllTasksByUser(
                @AuthenticationPrincipal UserDetails userDetails
    ){
        List<TaskDTOResponse> list = taskService.getAllTasksByUser(userDetails);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTOResponse> updateTask(
            @PathVariable("taskId") @NotNull Long taskId,
            @Valid @RequestBody UpdateTaskDTORequest updateTaskDTO,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        TaskDTOResponse updated = taskService.updateTask(userDetails, taskId, updateTaskDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{taskId}/complete")
    public ResponseEntity<TaskDTOResponse> completeTask(
            @PathVariable("taskId") @NotNull Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TaskDTOResponse updated = taskService.completeTask(userDetails, taskId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{taskId}/activate")
    public ResponseEntity<TaskDTOResponse> activateTask(
            @PathVariable("taskId") @NotNull Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TaskDTOResponse updated = taskService.activateTask(userDetails, taskId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("taskId") @NotNull Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        taskService.deleteTask(userDetails, taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTOResponse> getTaskById(
            @PathVariable("taskId") @NotNull Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        TaskDTOResponse task = taskService.getTaskById(userDetails, taskId);
        return ResponseEntity.ok(task);
    }
}
