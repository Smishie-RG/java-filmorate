package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE =
            LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final Map<Long, Set<Long>> likes = new ConcurrentHashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validate(film);

        if (film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        getById(film.getId());

        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Фильм не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        getById(filmId);

        userStorage.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь не найден"));

        likes.computeIfAbsent(
                filmId,
                k -> ConcurrentHashMap.newKeySet()
        ).add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        getById(filmId);

        userStorage.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь не найден"));

        Set<Long> filmLikes = likes.get(filmId);

        if (filmLikes != null) {
            filmLikes.remove(userId);
        }
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll()
                .stream()
                .sorted((f1, f2) ->
                        Integer.compare(
                                getLikesCount(f2.getId()),
                                getLikesCount(f1.getId())
                        )
                )
                .limit(count)
                .collect(Collectors.toList());
    }

    private int getLikesCount(Long filmId) {
        return likes.getOrDefault(filmId, Set.of()).size();
    }

    private void validate(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм пуст");
        }

        if (film.getName() == null
                || film.getName().isBlank()) {
            throw new ValidationException("Название пустое");
        }

        if (film.getDescription() != null
                && film.getDescription().length() > 200) {
            throw new ValidationException(
                    "Описание больше 200 символов"
            );
        }

        if (film.getReleaseDate() != null
                && film.getReleaseDate()
                .isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException(
                    "Дата релиза некорректна"
            );
        }

        if (film.getDuration() == null
                || film.getDuration() <= 0) {
            throw new ValidationException(
                    "Продолжительность должна быть положительной"
            );
        }
    }
}