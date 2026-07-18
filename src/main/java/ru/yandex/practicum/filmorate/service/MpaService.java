package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage
    ) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa getById(Integer id) {
        return mpaStorage.findById(id)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Рейтинг MPA не найден"
                        )
                );
    }
}