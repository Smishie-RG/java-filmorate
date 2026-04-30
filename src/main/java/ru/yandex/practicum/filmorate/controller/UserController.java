package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    @GetMapping
    public List<User> findAll() {
        List<User> result = new ArrayList<>(users.values());
        result.sort(Comparator.comparing(User::getId));
        return result;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validate(user);

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        validate(newUser);

        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка обновления пользователя: пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        User updatedUser = new User(
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday()
        );
        updatedUser.setId(newUser.getId());

        users.put(updatedUser.getId(), updatedUser);

        log.info("Обновлён пользователь: {}", updatedUser);
        return updatedUser;
    }

    private void validate(User user) {
        if (user == null) {
            log.warn("Ошибка валидации пользователя: пустой запрос");
            throw new ValidationException("Пользователь не может быть пустым");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации пользователя: некорректный email");
            throw new ValidationException("Электронная почта должна быть указана и содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации пользователя: некорректный логин");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации пользователя: дата рождения в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private long getNextId() {
        return counter.incrementAndGet();
    }
}