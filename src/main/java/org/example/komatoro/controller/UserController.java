package org.example.komatoro.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.dto.request.user.UserCreateDTORequest;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.dto.response.user.UserDTOResponse;
import org.example.komatoro.dto.response.user.UserWithSettingsDtoResponse;
import org.example.komatoro.mapper.UserMapper;
import org.example.komatoro.repository.IUserDailyStatsRepository;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.repository.IUserSettingsRepository;
import org.example.komatoro.service.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final IUserService userService;

    public UserController(IUserRepository repository, IUserService userService, IUserSettingsRepository settingsRepository, IUserDailyStatsRepository dailyStatsRepository, UserMapper userMapper) {
        this.userService = userService;
    }

    @PostMapping("/registration")
    public ResponseEntity<UserDTOResponse> registration(
            @RequestBody @Valid  UserCreateDTORequest createDTORequest
    ){

        UserDTOResponse response = userService.registration(createDTORequest);

        log.info("User created");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateSettings")
    public ResponseEntity<?> updateSettings(
            @RequestBody @Valid UserSettingsDTORequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        UserWithSettingsDtoResponse response = userService.updateSettings(userDetails, request);
        return ResponseEntity.ok(response);
    }
}

