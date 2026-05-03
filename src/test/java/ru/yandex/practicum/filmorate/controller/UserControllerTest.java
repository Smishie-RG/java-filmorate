package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private final UserController controller = new UserController();

    @Test
    void shouldCreateUser() {
        User user = makeValidUser();

        User createdUser = controller.create(user);

        assertEquals(1L, createdUser.getId());
    }

    @Test
    void shouldFindAllUsers() {
        User firstUser = makeValidUser();
        User secondUser = new User(
                "second@mail.ru",
                "second_login",
                "Second User",
                LocalDate.of(1995, 5, 5)
        );

        controller.create(firstUser);
        controller.create(secondUser);

        List<User> users = controller.findAll();

        assertEquals(2, users.size());
        assertEquals(1L, users.get(0).getId());
        assertEquals(2L, users.get(1).getId());
    }

    @Test
    void shouldUpdateUser() {
        User user = controller.create(makeValidUser());
        User updatedUser = new User(
                "mail@yandex.ru",
                "doloreUpdate",
                "est adipisicing",
                LocalDate.of(1976, 9, 20)
        );
        updatedUser.setId(user.getId());

        User result = controller.update(updatedUser);

        assertEquals(user.getId(), result.getId());
        assertEquals("mail@yandex.ru", result.getEmail());
        assertEquals("doloreUpdate", result.getLogin());
        assertEquals("est adipisicing", result.getName());
        assertEquals(LocalDate.of(1976, 9, 20), result.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNull() {
        assertThrows(ValidationException.class, () -> controller.create(null));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = new User(null, "user_login", "User Name", LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = new User(" ", "user_login", "User Name", LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = new User("usermail.ru", "user_login", "User Name", LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = new User("user@mail.ru", null, "User Name", LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = new User("user@mail.ru", " ", "User Name", LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = new User("user@mail.ru", "user login", "User Name", LocalDate.of(2000, 1, 1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = new User("user@mail.ru", "user_login", null, LocalDate.of(2000, 1, 1));

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() {
        User user = new User("user@mail.ru", "user_login", " ", LocalDate.of(2000, 1, 1));

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = new User(
                "user@mail.ru",
                "user_login",
                "User Name",
                LocalDate.now().plusDays(1)
        );

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = new User("user@mail.ru", "user_login", "User Name", LocalDate.now());

        assertDoesNotThrow(() -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenUpdateUserIdIsNull() {
        User user = makeValidUser();

        assertThrows(ValidationException.class, () -> controller.update(user));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotFound() {
        User user = makeValidUser();
        user.setId(999L);

        assertThrows(NotFoundException.class, () -> controller.update(user));
    }

    private User makeValidUser() {
        return new User(
                "user@mail.ru",
                "user_login",
                "User Name",
                LocalDate.of(2000, 1, 1)
        );
    }
}