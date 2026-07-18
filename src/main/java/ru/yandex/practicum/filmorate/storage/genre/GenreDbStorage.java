package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sql = """
                SELECT genre_id, name
                FROM genres
                WHERE genre_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Genre> findByIds(Set<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(
                ", ",
                Collections.nCopies(ids.size(), "?")
        );

        String sql = """
                SELECT genre_id, name
                FROM genres
                WHERE genre_id IN (%s)
                ORDER BY genre_id
                """.formatted(placeholders);

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                ids.toArray()
        );
    }

    private Genre mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        return new Genre(
                resultSet.getInt("genre_id"),
                resultSet.getString("name")
        );
    }
}