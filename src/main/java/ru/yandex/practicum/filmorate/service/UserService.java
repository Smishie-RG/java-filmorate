package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);
        setDefaultName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);

        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        getById(user.getId());
        setDefaultName(user);
        return userStorage.update(user);
    }

    public User getById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);
        userStorage.addFriend(userId, friendId);
        userStorage.addFriend(friendId, userId);
        log.debug("Пользователи id={} и id={} добавлены в друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);
        userStorage.removeFriend(userId, friendId);
        userStorage.removeFriend(friendId, userId);
        log.debug("Пользователи id={} и id={} удалены из друзей", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        getById(userId);

        return userStorage.getFriendIds(userId).stream()
                .map(this::getById)
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        getById(userId);
        getById(otherUserId);

        Set<Long> commonFriendIds = userStorage.getFriendIds(userId);
        commonFriendIds.retainAll(userStorage.getFriendIds(otherUserId));

        return commonFriendIds.stream()
                .map(this::getById)
                .sorted(Comparator.comparing(User::getId))
                .toList();
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validate(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }

        if (user.getLogin() == null
                || user.getLogin().isBlank()
                || user.getLogin().chars().anyMatch(Character::isWhitespace)) {
            throw new ValidationException("Некорректный логин");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}