package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Primary
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        String sql = """
                SELECT user_id, email, login, name, birthday
                FROM users
                ORDER BY user_id
                """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = """
                SELECT user_id, email, login, name, birthday
                FROM users
                WHERE user_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id)
                .stream()
                .findFirst();
    }

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(
                    4,
                    user.getBirthday() == null
                            ? null
                            : Date.valueOf(user.getBirthday())
            );

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить id созданного пользователя"
            );
        }

        user.setId(key.longValue());

        log.debug("Создан пользователь с id={}", user.getId());

        return user;
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET email = ?,
                    login = ?,
                    name = ?,
                    birthday = ?
                WHERE user_id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null
                        ? null
                        : Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        log.debug("Обновлён пользователь с id={}", user.getId());

        return user;
    }

    @Override
    public void delete(Long id) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM users WHERE user_id = ?",
                id
        );

        if (deletedRows == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        log.debug("Удалён пользователь с id={}", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = """
                MERGE INTO friendships (user_id, friend_id)
                KEY (user_id, friend_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = """
                DELETE FROM friendships
                WHERE user_id = ? AND friend_id = ?
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        String sql = """
                SELECT friend_id
                FROM friendships
                WHERE user_id = ?
                ORDER BY friend_id
                """;

        List<Long> friendIds = jdbcTemplate.queryForList(
                sql,
                Long.class,
                userId
        );

        return new HashSet<>(friendIds);
    }

    private User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();

        user.setId(resultSet.getLong("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));

        Date birthday = resultSet.getDate("birthday");

        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }

        return user;
    }
}