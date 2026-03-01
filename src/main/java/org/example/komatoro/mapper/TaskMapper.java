package org.example.komatoro.mapper;

import org.example.komatoro.dto.request.task.CreateTaskDTORequest;
import org.example.komatoro.dto.request.task.UpdateTaskDTORequest;
import org.example.komatoro.dto.response.task.TaskDTOResponse;
import org.example.komatoro.model.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    Task dtoToEntity(CreateTaskDTORequest taskDTORequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "active", source = "isActive")
    void updateEntityFromDto(UpdateTaskDTORequest updateDto, @MappingTarget Task task);

    List<TaskDTOResponse> toListResponse(List<Task> tasks);

    @Mapping(target = "taskId", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "createdAt", source = "createdAt")
    TaskDTOResponse toResponse(Task task);
}
