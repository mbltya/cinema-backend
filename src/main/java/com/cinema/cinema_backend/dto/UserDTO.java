package com.cinema.cinema_backend.dto;

import com.cinema.cinema_backend.entity.Role;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private Boolean isBlocked;

    public UserDTO(Long id, String username, String email, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public UserDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getIsBlocked() {return isBlocked;}
    public void setIsBlocked(Boolean isBlocked) {this.isBlocked = isBlocked;}
}