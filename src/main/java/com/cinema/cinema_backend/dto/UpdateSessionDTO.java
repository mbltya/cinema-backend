package com.cinema.cinema_backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class UpdateSessionDTO {

    private Long movieId; // опционально при обновлении
    private Long hallId; // опционально при обновлении

    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime; // опционально при обновлении

    @Positive(message = "Price must be positive")
    private Double price; // опционально при обновлении

    private String format; // опционально при обновлении

    // Конструкторы
    public UpdateSessionDTO() {}

    public UpdateSessionDTO(Long movieId, Long hallId, LocalDateTime startTime,
                            Double price, String format) {
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.price = price;
        this.format = format;
    }

    // Геттеры и сеттеры
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}