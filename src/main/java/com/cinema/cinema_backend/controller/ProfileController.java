package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.entity.User;
import com.cinema.cinema_backend.entity.Role; // Импортируем enum
import com.cinema.cinema_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Проверка аутентификации
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("error", "Not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            String email = authentication.getName();
            User user = userService.findByEmail(email).orElse(null);

            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            // Заполняем данные профиля
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());

            // Преобразуем Enum Role в String
            Role role = user.getRole();
            if (role != null) {
                response.put("role", role.name()); // "USER" или "ADMIN"
            } else {
                response.put("role", "USER"); // Значение по умолчанию
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Простой тестовый эндпоинт
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile API is working");
        return ResponseEntity.ok(response);
    }
}