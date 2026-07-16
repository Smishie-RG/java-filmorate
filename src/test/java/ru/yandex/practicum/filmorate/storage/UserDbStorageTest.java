package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserStorage userStorage;

    @Test
    void shouldCreateAndFindUserById() {
        User createdUser = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        Optional<User> userOptional =
                userStorage.findById(createdUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId())
                            .isEqualTo(createdUser.getId());

                    assertThat(user.getEmail())
                            .isEqualTo("first@mail.ru");

                    assertThat(user.getLogin())
                            .isEqualTo("first");
                });
    }

    @Test
    void shouldFindAllUsers() {
        User first = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        User second = userStorage.create(
                createUser("second@mail.ru", "second")
        );

        List<User> users = userStorage.findAll();

        assertThat(users)
                .extracting(User::getId)
                .containsExactly(
                        first.getId(),
                        second.getId()
                );
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        user.setEmail("updated@mail.ru");
        user.setLogin("updated");
        user.setName("Updated");

        User updatedUser = userStorage.update(user);

        assertThat(updatedUser.getEmail())
                .isEqualTo("updated@mail.ru");

        assertThat(userStorage.findById(user.getId()))
                .isPresent()
                .hasValueSatisfying(stored -> {
                    assertThat(stored.getLogin())
                            .isEqualTo("updated");

                    assertThat(stored.getName())
                            .isEqualTo("Updated");
                });
    }

    @Test
    void shouldDeleteUser() {
        User user = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        userStorage.delete(user.getId());

        assertThat(userStorage.findById(user.getId()))
                .isEmpty();

        assertThatThrownBy(
                () -> userStorage.delete(user.getId())
        ).hasMessage("Пользователь не найден");
    }

    @Test
    void shouldAddFriendOnlyForRequester() {
        User first = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        User second = userStorage.create(
                createUser("second@mail.ru", "second")
        );

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.addFriend(first.getId(), second.getId());

        Set<Long> firstFriends =
                userStorage.getFriendIds(first.getId());

        Set<Long> secondFriends =
                userStorage.getFriendIds(second.getId());

        assertThat(firstFriends)
                .containsExactly(second.getId());

        assertThat(secondFriends).isEmpty();
    }

    @Test
    void shouldRemoveFriend() {
        User first = userStorage.create(
                createUser("first@mail.ru", "first")
        );

        User second = userStorage.create(
                createUser("second@mail.ru", "second")
        );

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.removeFriend(first.getId(), second.getId());

        assertThat(userStorage.getFriendIds(first.getId()))
                .isEmpty();
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