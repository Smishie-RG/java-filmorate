package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private final FilmController controller = new FilmController();

    @Test
    void shouldCreateFilm() {
        Film film = makeValidFilm();

        Film createdFilm = controller.create(film);

        assertEquals(1L, createdFilm.getId());
    }

    @Test
    void shouldThrowExceptionWhenFilmIsNull() {
        assertThrows(ValidationException.class, () -> controller.create(null));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Film film = makeValidFilm();
        film.setName(null);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = makeValidFilm();
        film.setName(" ");

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200Characters() {
        Film film = makeValidFilm();
        film.setDescription("a".repeat(201));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldCreateFilmWhenDescriptionHas200Characters() {
        Film film = makeValidFilm();
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinDate() {
        Film film = makeValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldCreateFilmWhenReleaseDateIsMinDate() {
        Film film = makeValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = makeValidFilm();
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = makeValidFilm();
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    private Film makeValidFilm() {
        Film film = new Film();
        film.setName("Interstellar");
        film.setDescription("Film about space");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        return film;
    }
}