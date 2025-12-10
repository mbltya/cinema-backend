package com.cinema.cinema_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Integer rows;

    @NotNull
    private Integer seatsPerRow;

    @ManyToOne
    @JoinColumn(name = "cinema_id")
    private Cinema cinema;

    public Hall() {}

    public Hall(String name, Integer rows, Integer seatsPerRow, Cinema cinema) {
        this.name = name;
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
        this.cinema = cinema;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }

    public Integer getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(Integer seatsPerRow) { this.seatsPerRow = seatsPerRow; }

    public Cinema getCinema() { return cinema; }
    public void setCinema(Cinema cinema) { this.cinema = cinema; }

    public Integer getTotalSeats() {
        return rows * seatsPerRow;
    }
}
