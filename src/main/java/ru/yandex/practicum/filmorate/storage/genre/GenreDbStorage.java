package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository("genreDbStorage")
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        String sql = """
                SELECT genre_id, name
                FROM genres
                ORDER BY genre_id
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new Genre(
                        resultSet.getInt("genre_id"),
                        resultSet.getString("name")
                )
        );
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sql = """
                SELECT genre_id, name
                FROM genres
                WHERE genre_id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        (resultSet, rowNum) -> new Genre(
                                resultSet.getInt("genre_id"),
                                resultSet.getString("name")
                        ),
                        id
                )
                .stream()
                .findFirst();
    }
}