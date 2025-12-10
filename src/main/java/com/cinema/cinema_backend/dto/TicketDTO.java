package com.cinema.cinema_backend.dto;

import com.cinema.cinema_backend.entity.TicketStatus;
import java.time.LocalDateTime;

public class TicketDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long sessionId;
    private String movieTitle;
    private LocalDateTime sessionTime;
    private Integer rowNumber;
    private Integer seatNumber;
    private Double price;
    private LocalDateTime purchaseTime;
    private TicketStatus status;
    private String qrCode;

    // Constructors
    public TicketDTO() {}

    public TicketDTO(Long id, Long userId, String userName, Long sessionId,
                     String movieTitle, LocalDateTime sessionTime,
                     Integer rowNumber, Integer seatNumber, Double price,
                     LocalDateTime purchaseTime, TicketStatus status, String qrCode) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.sessionId = sessionId;
        this.movieTitle = movieTitle;
        this.sessionTime = sessionTime;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.price = price;
        this.purchaseTime = purchaseTime;
        this.status = status;
        this.qrCode = qrCode;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public LocalDateTime getSessionTime() { return sessionTime; }
    public void setSessionTime(LocalDateTime sessionTime) { this.sessionTime = sessionTime; }

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }

    public Integer getSeatNumber() { return seatNumber; }
    public void setSeatNumber(Integer seatNumber) { this.seatNumber = seatNumber; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public LocalDateTime getPurchaseTime() { return purchaseTime; }
    public void setPurchaseTime(LocalDateTime purchaseTime) { this.purchaseTime = purchaseTime; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}