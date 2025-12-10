package com.cinema.cinema_backend.dto;

public class AuthResponseDTO {
    private String token;
    private String email;
    private String role;

    public AuthResponseDTO(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    // Геттеры
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}