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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                // Настройка CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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

                // Настраиваем правила авторизации (Spring Security 6+ синтаксис)
                .authorizeHttpRequests(authz -> authz
                        // ====== ПУБЛИЧНЫЕ ЭНДПОИНТЫ ======
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/users").permitAll() // Регистрация

                        // ✅ ДОБАВЛЕНО: Тестовый эндпоинт профиля - публичный
                        .requestMatchers("/api/profile/test").permitAll()

                        // ✅ ДОБАВЛЕНО: Основные эндпоинты профиля - требуют аутентификации
                        .requestMatchers("/api/profile/**").permitAll()

                        // ====== GET запросы (публичные) ======
                        .requestMatchers("GET", "/api/movies/**").permitAll()
                        .requestMatchers("GET", "/api/sessions/**").permitAll()
                        .requestMatchers("GET", "/api/cinemas/**").permitAll()
                        .requestMatchers("GET", "/api/halls/**").permitAll()

                        // ====== ВСЕ ДРУГИЕ ЗАПРОСЫ (требуют аутентификации) ======
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

                        // ====== ПОЛЬЗОВАТЕЛИ ======
                        .requestMatchers("GET", "/api/users/**").authenticated()
                        .requestMatchers("PUT", "/api/users/**").authenticated()
                        .requestMatchers("DELETE", "/api/users/**").hasRole("ADMIN")

                        // ====== ВСЕ ОСТАЛЬНЫЕ ======
                        .anyRequest().permitAll()
                )

                // Добавляем JWT фильтр
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}