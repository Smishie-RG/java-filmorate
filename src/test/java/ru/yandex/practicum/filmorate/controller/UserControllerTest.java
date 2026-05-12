package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private final UserController controller = new UserController(
            new UserService(new InMemoryUserStorage())
    );

    @Test
    void shouldCreateUser() {
        User user = makeValidUser();

        User created = controller.create(user);

        assertEquals(1L, created.getId());
    }

    @Test
    void shouldFindAllUsers() {
        controller.create(makeValidUser());
        controller.create(new User("second@mail.ru", "second", "Second",
                LocalDate.of(1995, 5, 5)));

        List<User> users = controller.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void shouldUpdateUser() {
        User user = controller.create(makeValidUser());

        User updated = new User(
                "mail@yandex.ru",
                "login2",
                "name2",
                LocalDate.of(1990, 1, 1)
        );
        updated.setId(user.getId());

        User result = controller.update(updated);

        assertEquals("mail@yandex.ru", result.getEmail());
    }

    @Test
    void shouldAddAndGetFriends() {
        User u1 = controller.create(makeValidUser());
        User u2 = controller.create(new User(
                "u2@mail.ru", "u2", "U2", LocalDate.of(2000, 1, 1)
        ));

        controller.addFriend(u1.getId(), u2.getId());

        List<User> friends = controller.getFriends(u1.getId());

        assertEquals(1, friends.size());
    }

    @Test
    void shouldGetCommonFriends() {
        User u1 = controller.create(makeValidUser());
        User u2 = controller.create(new User("u2@mail.ru", "u2", "U2", LocalDate.of(2000, 1, 1)));
        User u3 = controller.create(new User("u3@mail.ru", "u3", "U3", LocalDate.of(2000, 1, 1)));

        controller.addFriend(u1.getId(), u3.getId());
        controller.addFriend(u2.getId(), u3.getId());

        List<User> common = controller.getCommonFriends(u1.getId(), u2.getId());

        assertEquals(1, common.size());
    }

    @Test
    void shouldRemoveFriend() {
        User u1 = controller.create(makeValidUser());
        User u2 = controller.create(new User("u2@mail.ru", "u2", "U2", LocalDate.of(2000, 1, 1)));

        controller.addFriend(u1.getId(), u2.getId());
        controller.removeFriend(u1.getId(), u2.getId());

        List<User> friends = controller.getFriends(u1.getId());

        assertEquals(0, friends.size());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> controller.findById(999L));
    }

    private User makeValidUser() {
        return new User(
                "user@mail.ru",
                "login",
                "name",
                LocalDate.of(2000, 1, 1)
        );
    }
}