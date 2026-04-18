package org.example.komatoro.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.komatoro.utils.JwtTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TomatoSessionControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTestUtil jwtTestUtil;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Sql({"/sql/users.sql"})
    void startTomatoSession_RequestIsValid_ReturnSessionDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/start")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content("""
            {
                "intendedMinutes": 25
            }
            """);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isCreated(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.id").isNumber(),
            jsonPath("$.userId").value(1),
            jsonPath("$.taskId").doesNotExist(),
            jsonPath("$.type").value("TIMER"),
            jsonPath("$.intendedMinutes").value(25),
            jsonPath("$.status").value("RUNNING")
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 481})
    @Sql({"/sql/users.sql"})
    void startTomatoSession_IntendedMinutesOutOfRange_ReturnException(int intendedMinutes) throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/start")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                    {
                        "intendedMinutes": %d
                    }
                    """.formatted(intendedMinutes));

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.path").value("/api/v1/sessions/start")
                );

    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void startTomatoSession_RunningSessionAlreadyExist_ReturnBadRequest() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/start")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                    {
                        "intendedMinutes": 30
                    }
                    """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("RUNNING_SESSION_ALREADY_EXIST"),
                        jsonPath("$.path").value("/api/v1/sessions/start")
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql", "/sql/finish_tomato_session.sql"})
    void getAllUserTomatoSessions_UserHasSessions_ReturnSessionList() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.get("/api/v1/sessions/all")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.length()").value(2),
            jsonPath("$[0].userId").value(1)
        );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void getCurrentActiveSessionByUser_ActiveSessionExist_ReturnSessionDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.get("/api/v1/sessions/active")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.id").value(1),
            jsonPath("$.status").value("RUNNING")
        );
    }

    @Test
    @Sql({"/sql/users.sql"})
    void getCurrentActiveSessionByUser_ActiveSessionNotExist_ReturnNotFound() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        performRequest("GET", "/api/v1/sessions/active", token, null)
                .andExpect(status().isNotFound());
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void getSessionById_RequestIsValid_ReturnSessionDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.get("/api/v1/sessions/{sessionId}", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.id").value(1),
            jsonPath("$.intendedMinutes").value(25)
        );
    }

    // Вариант для не существующего sessionId

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void pauseSession_RequestIsValid_ReturnPausedSessionDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/{sessionId}/pause", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.id").value(1),
            jsonPath("$.status").value("PAUSED")
        );
    }

    // Вариант когда sessionId не существует

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/paused_tomato_session.sql"})
    void resumeSession_RequestIsValid_ReturnRunningSessionDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/{sessionId}/resume", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.id").value(1),
            jsonPath("$.status").value("RUNNING")
        );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void extendTomatoSession_RequestIsValid_ReturnExtendedSessionDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/sessions/{sessionId}/extend", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content("""
            {
                "addMinutes": 5
            }
            """);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.id").value(1),
            jsonPath("$.intendedMinutes").value(30)
        );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void extendTomatoSession_AddMinutesIsInvalid_ReturnException() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/sessions/{sessionId}/extend", 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                   {
                       "addMinutes": 0
                   }
                   """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.path").value("/api/v1/sessions/%d/extend".formatted(1))
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void extendTomatoSession_IntendedMinutesExceeded_ReturnException() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/sessions/{sessionId}/extend", 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                   {
                       "addMinutes": 480
                   }
                   """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isUnprocessableEntity(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("INVALID_SESSION_PARAMETER")
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void finishSession_RequestIsValid_ReturnNoContent() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/{sessionId}/finish", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content("""
            {
                "status": "COMPLETED"
            }
            """);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isNoContent()
        );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void finishSession_StatusIsMissing_ReturnBadRequest() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/sessions/{sessionId}/finish", 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{}");

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.path").value("/api/v1/sessions/%d/finish".formatted(1))
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/running_tomato_session.sql"})
    void deleteSession_RequestIsValid_ReturnNoContent() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var deleteRequest = MockMvcRequestBuilders.delete("/api/v1/sessions/{sessionId}", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(deleteRequest)
        .andDo(print())
        .andExpectAll(
            status().isNoContent()
        );

    var getRequest = MockMvcRequestBuilders.get("/api/v1/sessions/{sessionId}", 1)
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(getRequest)
        .andDo(print())
        .andExpectAll(
            status().isNotFound(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.code").value("NOT_FOUND")
        );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/task.sql", "/sql/user_settings.sql", "/sql/finish_tomato_session.sql"})
    void getSessionTypeRecommendation_RequestIsValid_ReturnRecommendationDto() throws Exception {
    String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

    var requestBuilder = MockMvcRequestBuilders.get("/api/v1/sessions/recommendation")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON_VALUE);

    mockMvc.perform(requestBuilder)
        .andDo(print())
        .andExpectAll(
            status().isOk(),
            content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
            jsonPath("$.type").value("TIMER")
        );
    }

    @ParameterizedTest
    @MethodSource("unauthorizedRequests")
    void endpoint_UserIsNotAuthenticated_ReturnForbidden(String method, String path, String body) throws Exception {
        performRequest(method, path, null, body).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("invalidSessionIdRequests")
    @Sql({"/sql/users.sql"})
    void endpoint_SessionIdIsInvalidType_ReturnBadRequest(String method, String path, String body) throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");
        performRequest(method, path, token, body).andExpectAll(
                status().isBadRequest(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                jsonPath("$.code").value("INVALID_PARAMETER"),
                jsonPath("$.path").value(path)
        );
    }

    @ParameterizedTest
    @MethodSource("notFoundRequests")
    @Sql({"/sql/users.sql"})
    void endpoint_SessionNotFound_ReturnNotFound(String method, String path, String body) throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");
        performRequest(method, path, token, body).andExpectAll(
                status().isNotFound(),
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                jsonPath("$.code").value("NOT_FOUND"),
                jsonPath("$.path").value(path)
        );
    }

    private ResultActions performRequest(String method, String path, String token, String body) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = switch (method) {
            case "GET" -> MockMvcRequestBuilders.get(path);
            case "POST" -> MockMvcRequestBuilders.post(path);
            case "PATCH" -> MockMvcRequestBuilders.patch(path);
            case "DELETE" -> MockMvcRequestBuilders.delete(path);
            default -> throw new IllegalArgumentException("Unsupported method " + method);
        };

        requestBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        if (body != null) {
            requestBuilder.content(body);
        }

        return mockMvc.perform(requestBuilder).andDo(print());
    }

    private static Stream<Arguments> unauthorizedRequests() {
        return Stream.of(
                Arguments.of("POST", "/api/v1/sessions/start", "{\"intendedMinutes\":25}"),
                Arguments.of("GET", "/api/v1/sessions/all", null),
                Arguments.of("GET", "/api/v1/sessions/active", null),
                Arguments.of("GET", "/api/v1/sessions/1", null),
                Arguments.of("POST", "/api/v1/sessions/1/pause", null),
                Arguments.of("POST", "/api/v1/sessions/1/resume", null),
                Arguments.of("PATCH", "/api/v1/sessions/1/extend", "{\"addMinutes\":1}"),
                Arguments.of("POST", "/api/v1/sessions/1/finish", "{\"status\":\"COMPLETED\"}"),
                Arguments.of("DELETE", "/api/v1/sessions/1", null),
                Arguments.of("GET", "/api/v1/sessions/recommendation", null)
        );
    }

    private static Stream<Arguments> invalidSessionIdRequests() {
        return Stream.of(
                Arguments.of("GET", "/api/v1/sessions/null", null),
                Arguments.of("POST", "/api/v1/sessions/null/pause", null),
                Arguments.of("POST", "/api/v1/sessions/null/resume", null),
                Arguments.of("PATCH", "/api/v1/sessions/null/extend", "{\"addMinutes\":1}"),
                Arguments.of("POST", "/api/v1/sessions/null/finish", "{\"status\":\"COMPLETED\"}"),
                Arguments.of("DELETE", "/api/v1/sessions/null", null)
        );
    }

    private static Stream<Arguments> notFoundRequests() {
        return Stream.of(
                Arguments.of("GET", "/api/v1/sessions/999", null),
                Arguments.of("POST", "/api/v1/sessions/999/pause", null),
                Arguments.of("POST", "/api/v1/sessions/999/resume", null),
                Arguments.of("PATCH", "/api/v1/sessions/999/extend", "{\"addMinutes\":1}"),
                Arguments.of("POST", "/api/v1/sessions/999/finish", "{\"status\":\"COMPLETED\"}"),
                Arguments.of("DELETE", "/api/v1/sessions/999", null)
        );
    }
}