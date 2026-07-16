package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private UserStorage userStorage;

    @BeforeEach
    void clearStorage() {
        filmStorage.findAll().forEach(film -> filmStorage.delete(film.getId()));
        userStorage.findAll().forEach(user -> userStorage.delete(user.getId()));
    }

    @Test
    void shouldCreateUserAndUseLoginAsName() throws Exception {
        User user = new User(
                "user@mail.ru",
                "login",
                "",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@mail.ru"))
                .andExpect(jsonPath("$.name").value("login"));
    }

    @Test
    void shouldFindAllUsers() throws Exception {
        createUser(makeValidUser("first@mail.ru", "first", "First"));
        createUser(makeValidUser("second@mail.ru", "second", "Second"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        long userId = createUser(makeValidUser("first@mail.ru", "first", "First"));
        User updatedUser = makeValidUser("updated@mail.ru", "updated", "Updated");
        updatedUser.setId(userId);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("updated@mail.ru"));
    }

    @Test
    void shouldReturnBadRequestForInvalidUser() throws Exception {
        User invalidUser = new User(
                "invalid-email",
                "invalid login",
                "Name",
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddFriendOnlyForRequester() throws Exception {
        long firstUserId = createUser(
                makeValidUser(
                        "first@mail.ru",
                        "first",
                        "First"
                )
        );

        long secondUserId = createUser(
                makeValidUser(
                        "second@mail.ru",
                        "second",
                        "Second"
                )
        );

        mockMvc.perform(
                        put(
                                "/users/{id}/friends/{friendId}",
                                firstUserId,
                                secondUserId
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/users/{id}/friends",
                                firstUserId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].id")
                                .value(secondUserId)
                );

        mockMvc.perform(
                        get(
                                "/users/{id}/friends",
                                secondUserId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        long firstUserId = createUser(makeValidUser("first@mail.ru", "first", "First"));
        long secondUserId = createUser(makeValidUser("second@mail.ru", "second", "Second"));
        long commonUserId = createUser(makeValidUser("common@mail.ru", "common", "Common"));

        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUserId, commonUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", secondUserId, commonUserId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", firstUserId, secondUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commonUserId));
    }

    @Test
    void shouldRemoveFriendOnlyForRequester() throws Exception {
        long firstUserId = createUser(
                makeValidUser(
                        "first@mail.ru",
                        "first",
                        "First"
                )
        );

        long secondUserId = createUser(
                makeValidUser(
                        "second@mail.ru",
                        "second",
                        "Second"
                )
        );

        mockMvc.perform(
                        put(
                                "/users/{id}/friends/{friendId}",
                                firstUserId,
                                secondUserId
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete(
                                "/users/{id}/friends/{friendId}",
                                firstUserId,
                                secondUserId
                        )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/users/{id}/friends",
                                firstUserId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(
                        get(
                                "/users/{id}/friends",
                                secondUserId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldNotAddDuplicateFriend() throws Exception {
        long firstUserId = createUser(makeValidUser("first@mail.ru", "first", "First"));
        long secondUserId = createUser(makeValidUser("second@mail.ru", "second", "Second"));

        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUserId, secondUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUserId, secondUserId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", firstUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenFriendDoesNotExist() throws Exception {
        long userId = createUser(makeValidUser("first@mail.ru", "first", "First"));

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId, 999999L))
                .andExpect(status().isNotFound());
    }

    private long createUser(User user) throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("id").asLong();
    }

    private User makeValidUser(String email, String login, String name) {
        return new User(
                email,
                login,
                name,
                LocalDate.of(2000, 1, 1)
        );
    }
}