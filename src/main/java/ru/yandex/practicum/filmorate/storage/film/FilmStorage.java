package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAll();

    Optional<Film> findById(Long id);

    Film create(Film film);

    Film update(Film film);

    void delete(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    int getLikesCount(Long filmId);
}