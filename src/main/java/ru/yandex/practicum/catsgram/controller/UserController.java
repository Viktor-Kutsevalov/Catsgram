package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailIndex = new HashMap<>(); // для быстрого поиска по email

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        // Проверяем, что email указан
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }

        // Проверяем уникальность email
        if (emailIndex.containsKey(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        // Формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        // Сохраняем пользователя
        users.put(user.getId(), user);
        emailIndex.put(user.getEmail(), user.getId());

        return user;
    }

    @PutMapping
    public User update(@RequestBody User updatedUser) {
        // Проверяем, что id указан
        if (updatedUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        User existingUser = users.get(updatedUser.getId());
        if (existingUser == null) {
            throw new RuntimeException("Пользователь с id = " + updatedUser.getId() + " не найден");
        }

        // Обновляем email, если он указан и отличается
        if (updatedUser.getEmail() != null) {
            if (!Objects.equals(existingUser.getEmail(), updatedUser.getEmail())) {
                // Проверяем уникальность нового email
                if (emailIndex.containsKey(updatedUser.getEmail())) {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                }

                // Удаляем старый email из индекса и добавляем новый
                emailIndex.remove(existingUser.getEmail());
                emailIndex.put(updatedUser.getEmail(), existingUser.getId());
                existingUser.setEmail(updatedUser.getEmail());
            }
        }

        // Обновляем username, если он указан
        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        // Обновляем password, если он указан
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        return existingUser;
    }

    // Вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}