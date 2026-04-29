package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

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
    void shouldThrowExceptionWhenUserIsNull() {
        assertThrows(ValidationException.class, () -> controller.create(null));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = makeValidUser();
        user.setEmail(null);

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        User user = makeValidUser();
        user.setEmail(" ");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = makeValidUser();
        user.setEmail("usermail.ru");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = makeValidUser();
        user.setLogin(null);

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        User user = makeValidUser();
        user.setLogin(" ");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = makeValidUser();
        user.setLogin("user login");

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = makeValidUser();
        user.setName(null);

        User createdUser = controller.create(user);

        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() {
        User user = makeValidUser();
        user.setName(" ");

        User createdUser = controller.create(user);

        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldCreateUserWhenBirthdayIsToday() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> controller.create(user));
    }

    private User makeValidUser() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user_login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}