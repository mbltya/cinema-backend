package com.cinema.cinema_backend;

import com.cinema.cinema_backend.entity.Role;
import com.cinema.cinema_backend.entity.User;
import com.cinema.cinema_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Запуск DataSeeder для PostgreSQL ===");

        // Очищаем таблицу (опционально, можно закомментировать)
        // userRepository.deleteAll();

        // Создаем тестового администратора
        if (!userRepository.existsByEmail("admin@cinema.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@cinema.com");
            admin.setPassword(passwordEncoder.encode("admin123")); // Хешируем пароль!
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("Создан администратор: admin@cinema.com / admin123");
        } else {
            System.out.println("Администратор уже существует");
        }

        // Создаем тестового пользователя
        if (!userRepository.existsByEmail("user@cinema.com")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@cinema.com");
            user.setPassword(passwordEncoder.encode("user123")); // Хешируем пароль!
            user.setRole(Role.USER);

            userRepository.save(user);
            System.out.println("Создан пользователь: user@cinema.com / user123");
        } else {
            System.out.println("Пользователь уже существует");
        }

        // Выводим количество пользователей
        long userCount = userRepository.count();
        System.out.println("Всего пользователей в базе: " + userCount);
        System.out.println("=== DataSeeder завершен ===");
    }
}