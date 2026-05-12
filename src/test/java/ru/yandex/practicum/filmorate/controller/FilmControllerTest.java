package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private final FilmController controller = new FilmController(
            new FilmService(
                    new InMemoryFilmStorage(),
                    new InMemoryUserStorage()
            )
    );

    @Test
    void shouldCreateFilm() {
        Film film = makeValidFilm();

        Film createdFilm = controller.create(film);

        assertEquals(1L, createdFilm.getId());
    }

    @Test
    void shouldFindAllFilms() {
        controller.create(makeValidFilm());
        controller.create(new Film("Inception", "Film about dreams",
                LocalDate.of(2010, 7, 16), 148));

        List<Film> films = controller.findAll();

        assertEquals(2, films.size());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = controller.create(makeValidFilm());

        Film updated = new Film(
                "Updated",
                "Desc",
                LocalDate.of(2000, 1, 1),
                120
        );
        updated.setId(film.getId());

        Film result = controller.update(updated);

        assertEquals("Updated", result.getName());
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        Film film = makeValidFilm();
        film.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(film));
    }

    @Test
    void shouldAddLike() {
        Film film = controller.create(makeValidFilm());

        controller.addLike(film.getId(), 1L);

        List<Film> popular = controller.popular(1);

        assertEquals(1, popular.size());
        assertEquals(film.getId(), popular.get(0).getId());
    }

    @Test
    void shouldRemoveLike() {
        Film film = controller.create(makeValidFilm());

        controller.addLike(film.getId(), 1L);
        controller.removeLike(film.getId(), 1L);

        List<Film> popular = controller.popular(1);

        assertEquals(0, popular.size());
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