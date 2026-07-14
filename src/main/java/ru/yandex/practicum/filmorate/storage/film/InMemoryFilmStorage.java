package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> likes = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong();

    @Override
    public List<Film> findAll() {
        List<Film> result = new ArrayList<>(films.values());
        result.sort(Comparator.comparing(Film::getId));
        return result;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        film.setId(counter.incrementAndGet());
        films.put(film.getId(), film);
        likes.put(film.getId(), ConcurrentHashMap.newKeySet());
        log.debug("Создан фильм с id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        ensureFilmExists(film.getId());
        films.put(film.getId(), film);
        log.debug("Обновлён фильм с id={}", film.getId());
        return film;
    }

    @Override
    public void delete(Long id) {
        ensureFilmExists(id);
        films.remove(id);
        likes.remove(id);
        log.debug("Удалён фильм с id={}", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        ensureFilmExists(filmId);
        likes.computeIfAbsent(filmId, key -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        ensureFilmExists(filmId);
        likes.computeIfAbsent(filmId, key -> ConcurrentHashMap.newKeySet()).remove(userId);
    }

    @Override
    public int getLikesCount(Long filmId) {
        ensureFilmExists(filmId);
        return likes.getOrDefault(filmId, Set.of()).size();
    }

    private void ensureFilmExists(Long id) {
        if (id == null || !films.containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
    }
}