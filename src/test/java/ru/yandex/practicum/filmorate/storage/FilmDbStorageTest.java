package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        UserDbStorage.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Test
    void shouldCreateAndFindFilmById() {
        Film createdFilm = filmStorage.create(createFilm());

        Optional<Film> filmOptional =
                filmStorage.findById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName())
                            .isEqualTo("Interstellar");

                    assertThat(film.getMpa().getId())
                            .isEqualTo(1);

                    assertThat(film.getMpa().getName())
                            .isEqualTo("G");

                    assertThat(film.getGenres())
                            .extracting(Genre::getId)
                            .containsExactly(1, 2);
                });
    }

    @Test
    void shouldFindAllFilms() {
        Film first = filmStorage.create(createFilm());

        Film secondFilm = createFilm();
        secondFilm.setName("Inception");

        Film second = filmStorage.create(secondFilm);

        List<Film> films = filmStorage.findAll();

        assertThat(films)
                .extracting(Film::getId)
                .containsExactly(
                        first.getId(),
                        second.getId()
                );
    }

    @Test
    void shouldUpdateFilmWithMpaAndGenres() {
        Film film = filmStorage.create(createFilm());

        film.setName("Updated");
        film.setMpa(new Mpa(2, null));
        film.setGenres(
                new LinkedHashSet<>(
                        List.of(new Genre(3, null))
                )
        );

        Film updatedFilm = filmStorage.update(film);

        assertThat(updatedFilm.getName())
                .isEqualTo("Updated");

        assertThat(updatedFilm.getMpa().getId())
                .isEqualTo(2);

        assertThat(updatedFilm.getMpa().getName())
                .isEqualTo("PG");

        assertThat(updatedFilm.getGenres())
                .extracting(Genre::getId)
                .containsExactly(3);
    }

    @Test
    void shouldDeleteFilm() {
        Film film = filmStorage.create(createFilm());

        filmStorage.delete(film.getId());

        assertThat(filmStorage.findById(film.getId()))
                .isEmpty();

        assertThatThrownBy(
                () -> filmStorage.delete(film.getId())
        ).hasMessage("Фильм не найден");
    }

    @Test
    void shouldAddLikeOnlyOnceAndCountLikes() {
        Film film = filmStorage.create(createFilm());
        User user = userStorage.create(createUser());

        filmStorage.addLike(
                film.getId(),
                user.getId()
        );

        filmStorage.addLike(
                film.getId(),
                user.getId()
        );

        assertThat(
                filmStorage.getLikesCount(film.getId())
        ).isEqualTo(1);
    }

    @Test
    void shouldRemoveLike() {
        Film film = filmStorage.create(createFilm());
        User user = userStorage.create(createUser());

        filmStorage.addLike(
                film.getId(),
                user.getId()
        );

        filmStorage.removeLike(
                film.getId(),
                user.getId()
        );

        assertThat(
                filmStorage.getLikesCount(film.getId())
        ).isZero();
    }

    @Test
    void shouldFindPopularFilms() {
        Film firstFilm = createFilm();
        firstFilm.setName("First");
        firstFilm = filmStorage.create(firstFilm);

        Film secondFilm = createFilm();
        secondFilm.setName("Second");
        secondFilm = filmStorage.create(secondFilm);

        User firstUser = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        User secondUser = userStorage.create(
                createUser("second@mail.ru", "second")
        );

        filmStorage.addLike(
                firstFilm.getId(),
                firstUser.getId()
        );

        filmStorage.addLike(
                secondFilm.getId(),
                firstUser.getId()
        );

        filmStorage.addLike(
                secondFilm.getId(),
                secondUser.getId()
        );

        List<Film> popularFilms =
                filmStorage.findPopular(1);

        assertThat(popularFilms)
                .extracting(Film::getId)
                .containsExactly(secondFilm.getId());

        assertThat(popularFilms.get(0).getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2);
    }

    private Film createFilm() {
        Film film = new Film(
                "Interstellar",
                "Film about space",
                LocalDate.of(2014, 11, 6),
                169
        );

        film.setMpa(new Mpa(1, null));

        film.setGenres(
                new LinkedHashSet<>(
                        List.of(
                                new Genre(1, null),
                                new Genre(2, null)
                        )
                )
        );

        return film;
    }

    private User createUser() {
        return createUser("user@mail.ru", "user");
    }

    private User createUser(String email, String login) {
        return new User(
                email,
                login,
                login,
                LocalDate.of(2000, 1, 1)
        );
    }
}