package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.dto.AuthRequestDTO;
import com.cinema.cinema_backend.dto.AuthResponseDTO;
import com.cinema.cinema_backend.entity.User;
import com.cinema.cinema_backend.entity.Role;
import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    @Transactional
    public AuthResponseDTO register(User user) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Шифрование пароля
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Установка роли по умолчанию
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        // Сохранение пользователя
        User savedUser = userRepository.save(user);

        // Генерация токена
        UserDetails userDetails = loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDTO(token, savedUser.getEmail(), savedUser.getRole().name());
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        // Аутентификация
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Генерация токена
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        // Получение пользователя для роли
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new AuthResponseDTO(token, user.getEmail(), user.getRole().name());
    }
}