package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> friends = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    @Override
    public List<User> findAll() {
        List<User> result = new ArrayList<>(users.values());
        result.sort(Comparator.comparing(User::getId));
        return result;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        user.setId(counter.incrementAndGet());
        users.put(user.getId(), user);
        friends.put(user.getId(), ConcurrentHashMap.newKeySet());
        log.debug("Создан пользователь с id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        ensureUserExists(user.getId());
        users.put(user.getId(), user);
        log.debug("Обновлён пользователь с id={}", user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        ensureUserExists(id);
        users.remove(id);
        friends.remove(id);
        friends.values().forEach(userFriends -> userFriends.remove(id));
        log.debug("Удалён пользователь с id={}", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        ensureUserExists(userId);
        ensureUserExists(friendId);
        friends.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        ensureUserExists(userId);
        ensureUserExists(friendId);
        friends.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).remove(friendId);
    }

    @Override
    public Set<Long> getFriendIds(Long userId) {
        ensureUserExists(userId);
        return new HashSet<>(friends.getOrDefault(userId, Set.of()));
    }

    private void ensureUserExists(Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }
}