package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    private final Map<Long, Set<Long>> friends = new ConcurrentHashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);

        if (user.getId() == null) {
            throw new ValidationException("Id must not be null");
        }

        getById(user.getId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userStorage.update(user);
    }

    public User getById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);

        friends.computeIfAbsent(userId,
                key -> ConcurrentHashMap.newKeySet()).add(friendId);

        friends.computeIfAbsent(friendId,
                key -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getById(userId);
        getById(friendId);

        Set<Long> userFriends = friends.get(userId);

        if (userFriends != null) {
            userFriends.remove(friendId);
        }

        Set<Long> friendFriends = friends.get(friendId);

        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    public List<User> getFriends(Long userId) {
        getById(userId);

        return friends.getOrDefault(userId, Set.of())
                .stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        getById(userId);
        getById(otherUserId);

        Set<Long> firstFriends = friends.getOrDefault(userId, Set.of());
        Set<Long> secondFriends = friends.getOrDefault(otherUserId, Set.of());

        return firstFriends.stream()
                .filter(secondFriends::contains)
                .map(this::getById)
                .collect(Collectors.toList());
    }

    private void validate(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }

        if (user.getEmail() == null
                || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }

        if (user.getLogin() == null
                || user.getLogin().isBlank()
                || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный login");
        }

        if (user.getBirthday() != null
                && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}