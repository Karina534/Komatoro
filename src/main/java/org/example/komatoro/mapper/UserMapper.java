package org.example.komatoro.mapper;

import org.example.komatoro.dto.request.user.LoginUserDTORequest;
import org.example.komatoro.dto.request.user.UserCreateDTORequest;
import org.example.komatoro.dto.response.user.UserDTOResponse;
import org.example.komatoro.dto.response.user.UserWithSettingsDtoResponse;
import org.example.komatoro.model.User;
import org.example.komatoro.model.UserSettings;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role", defaultValue = "USER")
    User dtoCreateToEntity(UserCreateDTORequest sto);

    UserDTOResponse toResponse(User user);

    List<UserDTOResponse> toResponseList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "todayStats", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    User dtoLoginToEntity(LoginUserDTORequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "todayStats", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "verified", source = "verified")
    void updateUserFromDto(@MappingTarget User user, UserDTOResponse dto);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "verified", source = "user.verified")
    @Mapping(target = "pomodoroMinutes", source = "settings.pomodoroMinutes")
    @Mapping(target = "longBreakMinutes", source = "settings.longBreakMinutes")
    @Mapping(target = "shortBreakMinutes", source = "settings.shortBreakMinutes")
    @Mapping(target = "longBreakInterval", source = "settings.longBreakInterval")
    UserWithSettingsDtoResponse toResponse(User user, UserSettings settings);
}
