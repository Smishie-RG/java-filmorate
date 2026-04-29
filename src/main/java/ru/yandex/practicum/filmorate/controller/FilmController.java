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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        validate(newFilm);

        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm == null) {
            log.warn("Ошибка обновления фильма: фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());

        log.info("Обновлён фильм: {}", oldFilm);
        return oldFilm;
    }

    private void validate(Film film) {
        if (film == null) {
            log.warn("Ошибка валидации фильма: пустой запрос");
            throw new ValidationException("Фильм не может быть пустым");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: название пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации фильма: описание длиннее 200 символов");
            throw new ValidationException("Описание фильма не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации фильма: дата релиза раньше {}", MIN_RELEASE_DATE);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка валидации фильма: продолжительность должна быть положительной");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}