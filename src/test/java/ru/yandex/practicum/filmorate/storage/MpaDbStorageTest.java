package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaStorage mpaStorage;

    @Test
    void shouldFindAllMpaRatings() {
        List<Mpa> ratings =
                mpaStorage.findAll();

        assertThat(ratings).hasSize(5);

        assertThat(ratings)
                .extracting(Mpa::getId)
                .containsExactly(
                        1,
                        2,
                        3,
                        4,
                        5
                );

        assertThat(ratings)
                .extracting(Mpa::getName)
                .containsExactly(
                        "G",
                        "PG",
                        "PG-13",
                        "R",
                        "NC-17"
                );
    }

    @Test
    void shouldFindMpaById() {
        assertThat(mpaStorage.findById(3))
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId())
                            .isEqualTo(3);

                    assertThat(mpa.getName())
                            .isEqualTo("PG-13");
                });

        assertThat(mpaStorage.findById(999))
                .isEmpty();
    }
}