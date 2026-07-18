package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6);

        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactly(
                        1,
                        2,
                        3,
                        4,
                        5,
                        6
                );
    }

    @Test
    void shouldFindGenreById() {
        assertThat(genreStorage.findById(1))
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId())
                            .isEqualTo(1);

                    assertThat(genre.getName())
                            .isEqualTo("Комедия");
                });

        assertThat(genreStorage.findById(999))
                .isEmpty();
    }

    @Test
    void shouldFindGenresByIds() {
        List<Genre> genres =
                genreStorage.findByIds(Set.of(3, 1, 5));

        assertThat(genres)
                .extracting(Genre::getId)
                .containsExactly(1, 3, 5);
    }
}