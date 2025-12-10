package com.cinema.cinema_backend.config;

import com.cinema.cinema_backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для REST API
                .csrf(csrf -> csrf.disable())

                // Разрешаем фреймы для H2 консоли
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                // Настраиваем управление сессиями как STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Настраиваем правила авторизации
                .authorizeHttpRequests(authz -> authz
                        // Публичные эндпоинты (доступ без аутентификации)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("POST", "/api/users").permitAll() // Регистрация публичная

                        // GET запросы для просмотра данных - публичные
                        .requestMatchers("GET", "/api/movies/**").permitAll()
                        .requestMatchers("GET", "/api/sessions/**").permitAll()
                        .requestMatchers("GET", "/api/cinemas/**").permitAll()
                        .requestMatchers("GET", "/api/halls/**").permitAll()

                        // Все POST/PUT/DELETE операции требуют аутентификации
                        .requestMatchers("POST", "/api/movies/**").authenticated()
                        .requestMatchers("PUT", "/api/movies/**").authenticated()
                        .requestMatchers("DELETE", "/api/movies/**").authenticated()

                        .requestMatchers("POST", "/api/sessions/**").authenticated()
                        .requestMatchers("PUT", "/api/sessions/**").authenticated()
                        .requestMatchers("DELETE", "/api/sessions/**").authenticated()

                        .requestMatchers("POST", "/api/tickets/**").authenticated()
                        .requestMatchers("PUT", "/api/tickets/**").authenticated()
                        .requestMatchers("DELETE", "/api/tickets/**").authenticated()

                        .requestMatchers("POST", "/api/cinemas/**").authenticated()
                        .requestMatchers("PUT", "/api/cinemas/**").authenticated()
                        .requestMatchers("DELETE", "/api/cinemas/**").authenticated()

                        .requestMatchers("POST", "/api/halls/**").authenticated()
                        .requestMatchers("PUT", "/api/halls/**").authenticated()
                        .requestMatchers("DELETE", "/api/halls/**").authenticated()

                        // Управление пользователями
                        .requestMatchers("GET", "/api/users/**").authenticated()
                        .requestMatchers("PUT", "/api/users/**").authenticated()
                        .requestMatchers("DELETE", "/api/users/**").hasRole("ADMIN")

                        // Все остальные запросы
                        .anyRequest().authenticated()
                )

                // Добавляем JWT фильтр
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}