package com.cinema.cinema_backend.dto;

import java.util.List;

public class CinemaWithHallsDTO {
    private Long id;
    private String name;
    private String city;
    private String address;
    private List<HallDTO> halls;

    // Конструкторы, геттеры и сеттеры
    public CinemaWithHallsDTO() {}

    public CinemaWithHallsDTO(Long id, String name, String city, String address, List<HallDTO> halls) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.halls = halls;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<HallDTO> getHalls() { return halls; }
    public void setHalls(List<HallDTO> halls) { this.halls = halls; }
}