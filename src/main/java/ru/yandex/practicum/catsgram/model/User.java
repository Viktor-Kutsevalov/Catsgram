package ru.yandex.practicum.catsgram.model;

import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(of = {"email"})
public class User {
    Long id;
    String username;
    String email;
    String password;
    Instant registrationDate;
}
