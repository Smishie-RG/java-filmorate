package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class Film {
    @Setter
    private Long id;
    private final String name;
    private final String description;
    private final LocalDate releaseDate;
    private final Integer duration;

    public Film(String name, String description, LocalDate releaseDate, Integer duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}