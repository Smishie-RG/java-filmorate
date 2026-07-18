package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReferenceDataControllerTest {

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
        filmStorage.findAll()
                .forEach(
                        film -> filmStorage.delete(film.getId())
                );

        userStorage.findAll()
                .forEach(
                        user -> userStorage.delete(user.getId())
                );
    }

    @Test
    void shouldReturnAllGenresAndGenreById()
            throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Комедия")
                );

        mockMvc.perform(get("/genres/{id}", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(
                        jsonPath("$.name")
                                .value("Драма")
                );

        mockMvc.perform(get("/genres/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllMpaRatingsAndMpaById()
            throws Exception {
        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"));

        mockMvc.perform(get("/mpa/{id}", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(
                        jsonPath("$.name")
                                .value("PG-13")
                );

        mockMvc.perform(get("/mpa/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateFilmWithMpaAndGenres()
            throws Exception {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                169
        );

        film.setMpa(new Mpa(3, null));

        film.setGenres(
                new LinkedHashSet<>(
                        List.of(
                                new Genre(2, null),
                                new Genre(1, null),
                                new Genre(2, null)
                        )
                )
        );

        mockMvc.perform(
                        post("/films")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                film
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mpa.id").value(3))
                .andExpect(
                        jsonPath("$.mpa.name")
                                .value("PG-13")
                )
                .andExpect(
                        jsonPath("$.genres.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.genres[0].id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.genres[0].name")
                                .value("Комедия")
                )
                .andExpect(
                        jsonPath("$.genres[1].id")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.genres[1].name")
                                .value("Драма")
                );
    }

    @Test
    void shouldReturnNotFoundForUnknownMpaOrGenreOnFilmCreate()
            throws Exception {
        Film filmWithUnknownMpa = createFilm();

        filmWithUnknownMpa.setMpa(
                new Mpa(999, null)
        );

        mockMvc.perform(
                        post("/films")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                filmWithUnknownMpa
                                        )
                                )
                )
                .andExpect(status().isNotFound());

        Film filmWithUnknownGenre = createFilm();

        filmWithUnknownGenre.setGenres(
                new LinkedHashSet<>(
                        List.of(
                                new Genre(999, null)
                        )
                )
        );

        mockMvc.perform(
                        post("/films")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                filmWithUnknownGenre
                                        )
                                )
                )
                .andExpect(status().isNotFound());
    }

    private Film createFilm() {
        Film film = new Film(
                "Film",
                "Description",
                LocalDate.of(2000, 1, 1),
                100
        );

        film.setMpa(new Mpa(1, null));

        return film;
    }
}