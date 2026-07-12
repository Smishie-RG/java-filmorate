package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
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
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        getById(filmId);
        ensureUserExists(userId);
        filmStorage.addLike(filmId, userId);
        log.debug("Пользователь id={} поставил лайк фильму id={}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        getById(filmId);
        ensureUserExists(userId);
        filmStorage.removeLike(filmId, userId);
        log.debug("Пользователь id={} удалил лайк у фильма id={}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        if (count < 0) {
            throw new ValidationException("Количество фильмов не может быть отрицательным");
        }

        return filmStorage.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Film film) -> filmStorage.getLikesCount(film.getId()))
                        .reversed()
                        .thenComparing(Film::getId))
                .limit(count)
                .toList();
    }

    private void ensureUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private void validate(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}