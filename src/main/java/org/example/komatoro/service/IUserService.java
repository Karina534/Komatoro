package org.example.komatoro.service;

import org.example.komatoro.dto.request.user.UserCreateDTORequest;
import org.example.komatoro.dto.TemporaryEntityDTO.UserDTO;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.dto.response.user.UserDTOResponse;
import org.example.komatoro.dto.response.user.UserWithSettingsDtoResponse;
import org.example.komatoro.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface IUserService {
    UserDTOResponse registration(UserCreateDTORequest userCreateDTO);
    UserWithSettingsDtoResponse updateSettings(UserDetails userDetails, UserSettingsDTORequest updateSettingsDTO);
    UserDTOResponse getById(UserDetails userDetails);
}
