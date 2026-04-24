package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailIndex = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User findUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return user;
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (emailIndex.containsKey(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        emailIndex.put(user.getEmail(), user.getId());
        return user;
    }

    public User update(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        User existingUser = users.get(updatedUser.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь с id = " + updatedUser.getId() + " не найден");
        }
        if (updatedUser.getEmail() != null) {
            if (!Objects.equals(existingUser.getEmail(), updatedUser.getEmail())) {
                if (emailIndex.containsKey(updatedUser.getEmail())) {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                }
                emailIndex.remove(existingUser.getEmail());
                emailIndex.put(updatedUser.getEmail(), existingUser.getId());
                existingUser.setEmail(updatedUser.getEmail());
            }
        }
        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(updatedUser.getPassword());
        }
        return existingUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}