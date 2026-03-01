package org.example.komatoro.service;

import lombok.extern.slf4j.Slf4j;
import org.example.komatoro.dto.response.user.UserDTOResponse;
import org.example.komatoro.dto.response.user.UserWithSettingsDtoResponse;
import org.example.komatoro.mapper.UserMapper;
import org.example.komatoro.dto.request.user.UserCreateDTORequest;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.exeption.UserNotFoundException;
import org.example.komatoro.model.User;
import org.example.komatoro.model.UserDailyStats;
import org.example.komatoro.model.UserSettings;
import org.example.komatoro.repository.IUserRepository;
import org.example.komatoro.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional
@Service
public class UserService implements IUserService{
    private final IUserRepository repository;
    private final IUserSettingsService settingsService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(IUserRepository repository, IUserSettingsService settingsService, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.settingsService = settingsService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTOResponse registration(UserCreateDTORequest userCreateDTO) {
        log.debug("Try registration user with email {}", userCreateDTO.email());

        Optional<User> user = repository.findByEmail(userCreateDTO.email());
        if (user.isPresent()){
            throw new RuntimeException("User with email " + userCreateDTO.email() + " already exist");
        }

        User createdUser = userMapper.dtoCreateToEntity(userCreateDTO);
        createdUser.addDailyStats(new UserDailyStats(createdUser));
        createdUser.setSettings(new UserSettings(createdUser));
        createdUser.setPassword(passwordEncoder.encode(createdUser.getPassword()));
        User savedUser = repository.save(createdUser);

        log.debug("Made userDTO for repository for registration for user email {}", userCreateDTO.email());
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserWithSettingsDtoResponse updateSettings(UserDetails userDetails, UserSettingsDTORequest updateSettingsDTO) {
        Long userId = this.getUserIdFromUserDetails(userDetails);
        log.debug("Try update user {} settings", userId);
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserSettings userSettings = settingsService.updateUserSettings(userId, updateSettingsDTO);
        user.setSettings(userSettings);

        log.debug("Made userDTO for updating settings in repository for user {}", userId);
        return userMapper.toResponse(user, userSettings);
    }

    @Override
    public UserDTOResponse getById(UserDetails userDetails) {
        Long userId = this.getUserIdFromUserDetails(userDetails);
        log.debug("Try to get user by id {}", userId);
        return userMapper.toResponse(repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId)));
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails){
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getUserId();
        } else {
            log.warn("UserDetails is not an instance of CustomUserDetails. Unable to extract user information.");
            throw new RuntimeException("Invalid user details. Not an instance of CustomUserDetails.");
        }
    }

    private UserDTOResponse convertToResponseDto(User user){
        return new UserDTOResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.isVerified());
    }
}
