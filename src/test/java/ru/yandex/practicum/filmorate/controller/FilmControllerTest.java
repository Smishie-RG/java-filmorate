package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

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
    void shouldFindAllFilms() {
        Film firstFilm = makeValidFilm();
        Film secondFilm = new Film(
                "Inception",
                "Film about dreams",
                LocalDate.of(2010, 7, 16),
                148
        );

        controller.create(firstFilm);
        controller.create(secondFilm);

        List<Film> films = controller.findAll();

        assertEquals(2, films.size());
        assertEquals(1L, films.get(0).getId());
        assertEquals(2L, films.get(1).getId());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = controller.create(makeValidFilm());
        Film updatedFilm = new Film(
                "Film Updated",
                "New film update description",
                LocalDate.of(1989, 4, 17),
                190
        );
        updatedFilm.setId(film.getId());

        Film result = controller.update(updatedFilm);

        assertEquals(film.getId(), result.getId());
        assertEquals("Film Updated", result.getName());
        assertEquals("New film update description", result.getDescription());
        assertEquals(LocalDate.of(1989, 4, 17), result.getReleaseDate());
        assertEquals(190, result.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenFilmIsNull() {
        assertThrows(ValidationException.class, () -> controller.create(null));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Film film = new Film(null, "Film about space", LocalDate.of(2014, 11, 6), 169);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = new Film(" ", "Film about space", LocalDate.of(2014, 11, 6), 169);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionIsLongerThan200Characters() {
        Film film = new Film(
                "Interstellar",
                "a".repeat(201),
                LocalDate.of(2014, 11, 6),
                169
        );

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldCreateFilmWhenDescriptionHas200Characters() {
        Film film = new Film(
                "Interstellar",
                "a".repeat(200),
                LocalDate.of(2014, 11, 6),
                169
        );

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBeforeMinDate() {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(1895, 12, 27),
                169
        );

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldCreateFilmWhenReleaseDateIsMinDate() {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(1895, 12, 28),
                169
        );

        assertDoesNotThrow(() -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNull() {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                null
        );

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                0
        );

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                -1
        );

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionWhenUpdateFilmIdIsNull() {
        Film film = makeValidFilm();

        assertThrows(ValidationException.class, () -> controller.update(film));
    }

    @Test
    void shouldThrowExceptionWhenFilmIsNotFound() {
        Film film = makeValidFilm();
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(film));
    }

    private Film makeValidFilm() {
        return new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                169
        );
    }
}