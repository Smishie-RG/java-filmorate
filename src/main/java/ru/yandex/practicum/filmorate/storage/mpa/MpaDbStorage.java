package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        String sql = """
                SELECT mpa_id, name
                FROM mpa
                ORDER BY mpa_id
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new Mpa(
                        resultSet.getInt("mpa_id"),
                        resultSet.getString("name")
                )
        );
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        String sql = """
                SELECT mpa_id, name
                FROM mpa
                WHERE mpa_id = ?
                """;

        return jdbcTemplate.query(
                        sql,
                        (resultSet, rowNum) -> new Mpa(
                                resultSet.getInt("mpa_id"),
                                resultSet.getString("name")
                        ),
                        id
                )
                .stream()
                .findFirst();
    }
}