package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.dto.AuthRequestDTO;
import com.cinema.cinema_backend.dto.AuthResponseDTO;
import com.cinema.cinema_backend.entity.Role;
import com.cinema.cinema_backend.entity.User;
import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponseDTO register(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        User savedUser = userRepository.save(user);

        // ✅ Генерируем токен по email
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponseDTO(token, savedUser.getEmail(), savedUser.getRole().name());
    }

    public AuthResponseDTO login(AuthRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Генерируем токен по email
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponseDTO(token, user.getEmail(), user.getRole().name());
    }
}
