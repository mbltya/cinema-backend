package com.cinema.cinema_backend.dto;

import java.time.LocalDateTime;

public class SessionDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long hallId;
    private String hallName;
    private Long cinemaId;
    private String cinemaName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;
    private String format;

    public SessionDTO(Long id, Long movieId, String movieTitle, Long hallId,
                      String hallName, Long cinemaId, String cinemaName,
                      LocalDateTime startTime, LocalDateTime endTime,
                      Double price, String format) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.hallId = hallId;
        this.hallName = hallName;
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.format = format;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public Long getCinemaId() { return cinemaId; }
    public void setCinemaId(Long cinemaId) { this.cinemaId = cinemaId; }

    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}