package org.example.komatoro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.komatoro.dto.request.user.UserCreateDTORequest;
import org.example.komatoro.dto.request.userSettings.UserSettingsDTORequest;
import org.example.komatoro.model.Role;
import org.example.komatoro.utils.JwtTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTestUtil jwtTestUtil;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registration_RequestIsValid_ReturnUserDto() throws Exception{
        UserCreateDTORequest user = new UserCreateDTORequest(
                "User",
                "Passworf25",
                "ksdjbs@mail.ru",
                Role.USER
        );

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(user));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE)
                );
    }

    @Test
    void registration_AllFieldsAreInvalid_ReturnException() throws Exception {
        UserCreateDTORequest request = new UserCreateDTORequest(
                "",
                "",
                "",
                null
        );

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(4)
                );
    }

    @ParameterizedTest
    @CsvSource({
            "username, , Username is required",
            "password, , Password is required",
            "email, , Email is required"
    })
    void registration_WithRequiredFieldIsBlankOrNull_ReturnException(
            String fieldName, String value, String expectedMessage
    ) throws Exception{

        UserCreateDTORequest invalidUser = new UserCreateDTORequest(
                fieldName.equals("username") ? value : "User",
                fieldName.equals("password") ? value : "Passworf25",
                fieldName.equals("email") ? value : "ksdjbs@mail.ru",
                Role.USER
        );

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(invalidUser));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.status").value(400),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.path").value("/api/v1/users/registration")
                );
    }

    @Test
    void registration_EmailInvalid_ReturnException() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "username": "User",
                            "password": "Passworf25",
                            "email": "hdfgjer",
                            "role": "USER"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.status").value(400),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors.email").exists(),
                        jsonPath("$.path").value("/api/v1/users/registration")
                );
    }

    @Test
    void registration_PasswordInvalid_ReturnException() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "username": "User",
                            "password": "kdfhvbd",
                            "email": "hdfgjer@mail.ru",
                            "role": "USER"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.status").value(400),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors.password").exists(),
                        jsonPath("$.path").value("/api/v1/users/registration")
                );
    }

    @Test
    void registration_PasswordTooSmall_ReturnException() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "username": "User",
                            "password": "kdf",
                            "email": "hdfgjer@mail.ru",
                            "role": "USER"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.status").value(400),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors.password").exists(),
                        jsonPath("$.path").value("/api/v1/users/registration")
                );
    }

    @Test
    void registration_UsernameTooSmall_ReturnException() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/registration")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "username": "U",
                            "password": "dfnbdk34",
                            "email": "hdfgjer@mail.ru",
                            "role": "USER"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.status").value(400),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors.username").exists(),
                        jsonPath("$.path").value("/api/v1/users/registration")
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/user_settings.sql"})
    void updateSettings_RequestIsValid_ReturnUserWithSettingDto() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");
        UserSettingsDTORequest request = new UserSettingsDTORequest(30,30,10,2);

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/updateSettings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.id").value(1),
                        jsonPath("$.email").value("kolin@mail.ru")
                );
    }

    @ParameterizedTest
    @CsvSource({
            "pomodoroMinutes, 9",
            "pomodoroMinutes, 481",
            "longBreakMinutes, 4",
            "longBreakMinutes, 241",
            "shortBreakMinutes, 4",
            "shortBreakMinutes, 121",
            "longBreakInterval, 1",
            "longBreakInterval, 25"
    })
    void updateSettings_MinMaxValueInvalid_ReturnException(String fieldName, String value) throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        UserSettingsDTORequest request = new UserSettingsDTORequest(
                fieldName.equals("pomodoroMinutes") ? Integer.parseInt(value) : 30,
                fieldName.equals("longBreakMinutes") ? Integer.parseInt(value) : 20,
                fieldName.equals("shortBreakMinutes") ? Integer.parseInt(value) : 10,
                fieldName.equals("longBreakInterval") ? Integer.parseInt(value) : 5
        );

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/users/updateSettings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request));

        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.path").value("/api/v1/users/updateSettings")
                );
    }
}