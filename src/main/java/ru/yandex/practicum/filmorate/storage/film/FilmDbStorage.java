package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Primary
@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String BASE_SELECT = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.mpa_id,
                   m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON m.mpa_id = f.mpa_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query(
                BASE_SELECT + " ORDER BY f.film_id",
                this::mapRow
        );

        loadGenres(films);

        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> film = jdbcTemplate.query(
                        BASE_SELECT + " WHERE f.film_id = ?",
                        this::mapRow,
                        id
                )
                .stream()
                .findFirst();

        film.ifPresent(value -> loadGenres(List.of(value)));

        return film;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        String sql = """
                INSERT INTO films (
                    name,
                    description,
                    release_date,
                    duration,
                    mpa_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    new String[]{"film_id"}
            );

            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(
                    3,
                    film.getReleaseDate() == null
                            ? null
                            : Date.valueOf(film.getReleaseDate())
            );
            statement.setInt(4, film.getDuration());

            if (film.getMpa() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, film.getMpa().getId());
            }

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить id созданного фильма"
            );
        }

        film.setId(key.longValue());
        saveGenres(film);

        log.debug("Создан фильм с id={}", film.getId());

        return findById(film.getId()).orElseThrow();
    }

    @Override
    @Transactional
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE film_id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() == null
                        ? null
                        : Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null
                        ? null
                        : film.getMpa().getId(),
                film.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Фильм не найден");
        }

        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        saveGenres(film);

        log.debug("Обновлён фильм с id={}", film.getId());

        return findById(film.getId()).orElseThrow();
    }

    @Override
    public void delete(Long id) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM films WHERE film_id = ?",
                id
        );

        if (deletedRows == 0) {
            throw new NotFoundException("Фильм не найден");
        }

        log.debug("Удалён фильм с id={}", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = """
                MERGE INTO film_likes (film_id, user_id)
                KEY (film_id, user_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = """
                DELETE FROM film_likes
                WHERE film_id = ? AND user_id = ?
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM film_likes
                WHERE film_id = ?
                """,
                Integer.class,
                filmId
        );

        return count == null ? 0 : count;
    }

    @Override
    public List<Film> findPopular(int count) {
        String sql = """
                SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       m.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON m.mpa_id = f.mpa_id
                LEFT JOIN film_likes fl ON fl.film_id = f.film_id
                GROUP BY f.film_id,
                         f.name,
                         f.description,
                         f.release_date,
                         f.duration,
                         m.mpa_id,
                         m.name
                ORDER BY COUNT(fl.user_id) DESC, f.film_id
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(
                sql,
                this::mapRow,
                count
        );

        loadGenres(films);

        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        Set<Integer> genreIds = new LinkedHashSet<>();

        for (Genre genre : film.getGenres()) {
            if (genre != null && genre.getId() != null) {
                genreIds.add(genre.getId());
            }
        }

        if (genreIds.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.batchUpdate(
                sql,
                genreIds,
                genreIds.size(),
                (statement, genreId) -> {
                    statement.setLong(1, film.getId());
                    statement.setInt(2, genreId);
                }
        );
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        Map<Long, Film> filmsById = new LinkedHashMap<>();

        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>());
            filmsById.put(film.getId(), film);
        }

        String placeholders = String.join(
                ", ",
                Collections.nCopies(filmsById.size(), "?")
        );

        String sql = """
                SELECT fg.film_id, g.genre_id, g.name
                FROM film_genres fg
                JOIN genres g ON g.genre_id = fg.genre_id
                WHERE fg.film_id IN (%s)
                ORDER BY fg.film_id, g.genre_id
                """.formatted(placeholders);

        List<FilmGenre> filmGenres = jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new FilmGenre(
                        resultSet.getLong("film_id"),
                        new Genre(
                                resultSet.getInt("genre_id"),
                                resultSet.getString("name")
                        )
                ),
                filmsById.keySet().toArray()
        );

        for (FilmGenre filmGenre : filmGenres) {
            filmsById.get(filmGenre.filmId())
                    .getGenres()
                    .add(filmGenre.genre());
        }
    }

    private Film mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        Film film = new Film();

        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));

        Date releaseDate = resultSet.getDate("release_date");

        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(resultSet.getInt("duration"));

        int mpaId = resultSet.getInt("mpa_id");

        if (!resultSet.wasNull()) {
            film.setMpa(
                    new Mpa(
                            mpaId,
                            resultSet.getString("mpa_name")
                    )
            );
        }

        return film;
    }

    private record FilmGenre(Long filmId, Genre genre) {
    }
}