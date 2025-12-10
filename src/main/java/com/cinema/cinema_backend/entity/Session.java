package com.cinema.cinema_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private Double price;

    private String format; // 2D, 3D, IMAX

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void calculateEndTime() {
        if (movie != null && startTime != null && movie.getDuration() != null) {
            this.endTime = startTime.plusMinutes(movie.getDuration());
        }
    }

    // Constructors
    public Session() {}

    public Session(Movie movie, Hall hall, LocalDateTime startTime, Double price, String format) {
        this.movie = movie;
        this.hall = hall;
        this.startTime = startTime;
        this.price = price;
        this.format = format;
        calculateEndTime();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) {
        this.movie = movie;
        calculateEndTime();
    }

    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        calculateEndTime();
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
}