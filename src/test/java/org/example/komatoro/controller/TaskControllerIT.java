package org.example.komatoro.controller;

import org.example.komatoro.utils.JwtTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TaskControllerIT {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Test
    @Sql("/sql/users.sql")
    void createTask_RequestIsValid_ReturnNewTaskDto() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                    {
                        "title": "New Task",
                        "description": "New description"
                    }
                    """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.taskId").value(1),
                        jsonPath("$.title").value("New Task"),
                        jsonPath("$.description").value("New description"),
                        jsonPath("$.isActive").value(true)
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void createTask_RequestIsInvalid_ReturnException() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .locale(Locale.of("ru", "RU"))
                .content("""
                    {
                        "title": null,
                        "description": "New description"
                    }
                    """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.status").value(400),
                        jsonPath("$.error").value("Validation failed"),
                        jsonPath("$.errors.title").value("Title is required"),
                        jsonPath("$.path").value("/api/v1/tasks")
                );
    }

    @Test
    void createTask_UserIsNotAuthorized_ReturnForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .locale(Locale.of("ru", "RU"))
                .content("""
                    {
                        "title": "New Title",
                        "description": "New description"
                    }
                    """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/tascks.sql"})
    void getAllTasksByUser_ReturnsTasksDTOList() throws Exception {
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/tasks")
            .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        content().json("""
                            [
                                {"taskId": 1, "title": "Title 1", "description": "Title 1 description", "isActive": true, "createdAt": "2026-04-17T12:40:18.725735Z"},
                                {"taskId": 3, "title": "Title 3", "description": "Title 3 description", "isActive": false, "createdAt": "2026-01-17T12:40:18.725735Z"}
                            ]
                            """)
                );
    }

    @Test
    void getAllTasksByUser_UserIsNotAuthorized_ReturnForbidden() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/tascks.sql"})
    void updateTask_RequestIsValid_ReturnTaskDTO() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.put("/api/v1/tasks/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "title": "Updated Title",
                            "description": "Updated description"
                        }
                        """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.taskId").value(1),
                        jsonPath("$.title").value("Updated Title"),
                        jsonPath("$.description").value("Updated description")
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void updateTask_TaskIdIsNull_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.put("/api/v1/tasks/{taskId}", "null")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("INVALID_PARAMETER"),
                        jsonPath("$.path").value("/api/v1/tasks/null")
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void updateTask_TaskIsNotExist_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.put("/api/v1/tasks/5")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "title": "Updated Title",
                            "description": "Updated description"
                        }
                        """);;

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("NOT_FOUND")
                );
    }

    @Test
    void updateTask_UserIsNotAuthenticated_ReturnForbidden() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.put("/api/v1/tasks/1")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            "title": "Title",
                            "description": "description"
                        }
                        """);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/tascks.sql"})
    void completeTask_RequestIsValid_ReturnTaskDto() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/1/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.taskId").value(1),
                        jsonPath("$.isActive").value(false)
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void completeTask_TaskIdIsNull_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/{taskId}/complete", "null")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("INVALID_PARAMETER"),
                        jsonPath("$.path").value("/api/v1/tasks/null/complete")
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void completeTask_TaskIdNotExist_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/5/complete")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        """
                        {
                            "title": "Title",
                            "description": "description"
                        }
                        """
                );

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("NOT_FOUND")
                );
    }

    @Test
    void completeTask_UserIsNotAuthenticated_ReturnForbidden() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/1/complete")
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/tascks.sql"})
    void activateTask_RequestIsValid_ReturnTaskDTO() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/3/activate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.taskId").value(3),
                        jsonPath("$.isActive").value(true)
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void activateTask_TaskIdIsNull_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/{taskId}/activate", "null")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("INVALID_PARAMETER"),
                        jsonPath("$.path").value("/api/v1/tasks/null/activate")
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void activateTask_TaskIdNotExist_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/5/activate")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        """
                        {
                            "title": "Title",
                            "description": "description"
                        }
                        """
                );

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("NOT_FOUND")
                );
    }

    @Test
    void activateTask_UserIsNotAuthenticates_ReturnForbidden() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.patch("/api/v1/tasks/3/activate")
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/tascks.sql"})
    void deleteTask_RequestIsValid_ReturnNoContent() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.delete("/api/v1/tasks/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNoContent()
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void deleteTask_TaskIdIsNull_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.delete("/api/v1/tasks/null")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("INVALID_PARAMETER"),
                        jsonPath("$.path").value("/api/v1/tasks/null")
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void deleteTask_TaskIdNotExist_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.delete("/api/v1/tasks/5")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        """
                        {
                            "title": "Title",
                            "description": "description"
                        }
                        """
                );

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("NOT_FOUND")
                );
    }

    @Test
    void deleteTask_UserIsNotAuthenticates_ReturnForbidden() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.delete("/api/v1/tasks/3")
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }

    @Test
    @Sql({"/sql/users.sql", "/sql/tascks.sql"})
    void getTaskById_RequestIsValid_ReturnTaskDto() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/tasks/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.taskId").value(1)
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void getTaskById_TaskIdIsNull_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");
        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/tasks/null")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("INVALID_PARAMETER"),
                        jsonPath("$.path").value("/api/v1/tasks/null")
                );
    }

    @Test
    @Sql("/sql/users.sql")
    void getTaskById_TaskIdNotExist_ReturnException() throws Exception{
        String token = jwtTestUtil.generateAccessTokenString("kolin@mail.ru");

        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/tasks/5")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        """
                        {
                            "title": "Title",
                            "description": "description"
                        }
                        """
                );

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE),
                        jsonPath("$.code").value("NOT_FOUND")
                );
    }

    @Test
    void getTaskById_UserIsNotAuthenticates_ReturnForbidden() throws Exception{
        var requestBuilder = MockMvcRequestBuilders.get("/api/v1/tasks/3")
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}