package com.cinema.cinema_backend.dto;

public class HallDTO {
    private Long id;
    private String name;
    private Integer rows;
    private Integer seatsPerRow;
    private Long cinemaId;
    private String cinemaName;
    private Integer totalSeats;

    public HallDTO() {}

    public HallDTO(Long id, String name, Integer rows, Integer seatsPerRow,
                   Long cinemaId, String cinemaName) {
        this.id = id;
        this.name = name;
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
        this.totalSeats = rows * seatsPerRow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
        if (rows != null && seatsPerRow != null) {
            this.totalSeats = rows * seatsPerRow;
        }
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(Integer seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
        if (rows != null && seatsPerRow != null) {
            this.totalSeats = rows * seatsPerRow;
        }
    }

    public Long getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Long cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}