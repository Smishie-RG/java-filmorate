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
import ru.yandex.practicum.filmorate.model.Film;
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
class FilmControllerTest {

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
    void shouldCreateAndFindFilmById() throws Exception {
        long filmId = createFilm(makeValidFilm());

        mockMvc.perform(get("/films/{id}", filmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value("Interstellar"));
    }

    @Test
    void shouldFindAllFilms() throws Exception {
        createFilm(makeValidFilm());
        createFilm(new Film(
                "Inception",
                "Film about dreams",
                LocalDate.of(2010, 7, 16),
                148
        ));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUpdateFilm() throws Exception {
        long filmId = createFilm(makeValidFilm());
        Film updatedFilm = new Film(
                "Updated",
                "Updated description",
                LocalDate.of(2000, 1, 1),
                120
        );
        updatedFilm.setId(filmId);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldReturnBadRequestForInvalidFilm() throws Exception {
        Film invalidFilm = new Film(
                "Invalid",
                "Description",
                LocalDate.of(1895, 12, 27),
                0
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenFilmDoesNotExist() throws Exception {
        mockMvc.perform(get("/films/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddAndRemoveLike() throws Exception {
        long firstFilmId = createFilm(makeValidFilm());
        long secondFilmId = createFilm(new Film(
                "Inception",
                "Film about dreams",
                LocalDate.of(2010, 7, 16),
                148
        ));
        long firstUserId = createUser(makeValidUser("first@mail.ru", "first"));
        long secondUserId = createUser(makeValidUser("second@mail.ru", "second"));

        mockMvc.perform(put("/films/{id}/like/{userId}", firstFilmId, firstUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilmId, firstUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilmId, secondUserId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular").param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(secondFilmId));

        mockMvc.perform(delete("/films/{id}/like/{userId}", secondFilmId, firstUserId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/films/{id}/like/{userId}", secondFilmId, secondUserId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular").param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(firstFilmId));
    }

    @Test
    void shouldCountDuplicateLikeOnce() throws Exception {
        long firstFilmId = createFilm(makeValidFilm());
        long secondFilmId = createFilm(new Film(
                "Inception",
                "Film about dreams",
                LocalDate.of(2010, 7, 16),
                148
        ));
        long firstUserId = createUser(makeValidUser("first@mail.ru", "first"));
        long secondUserId = createUser(makeValidUser("second@mail.ru", "second"));

        mockMvc.perform(put("/films/{id}/like/{userId}", firstFilmId, firstUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", firstFilmId, firstUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilmId, firstUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilmId, secondUserId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular").param("count", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(secondFilmId));
    }

    @Test
    void shouldReturnBadRequestForNegativePopularCount() throws Exception {
        mockMvc.perform(get("/films/popular").param("count", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNonNumericPopularCount() throws Exception {
        mockMvc.perform(get("/films/popular").param("count", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenLikeUserDoesNotExist() throws Exception {
        long filmId = createFilm(makeValidFilm());

        mockMvc.perform(put("/films/{id}/like/{userId}", filmId, 999999L))
                .andExpect(status().isNotFound());
    }

    private long createFilm(Film film) throws Exception {
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("id").asLong();
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

    private Film makeValidFilm() {
        return new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                169
        );
    }

    private User makeValidUser(String email, String login) {
        return new User(
                email,
                login,
                login,
                LocalDate.of(2000, 1, 1)
        );
    }
}