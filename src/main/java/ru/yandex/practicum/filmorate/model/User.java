package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class User {
    @Setter
    private Long id;
    private final String email;
    private final String login;
    private final String name;
    private final LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name == null || name.isBlank() ? login : name;
        this.birthday = birthday;
    }
}