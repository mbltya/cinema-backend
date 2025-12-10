package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.dto.SessionDTO;
import com.cinema.cinema_backend.dto.UpdateSessionDTO;
import com.cinema.cinema_backend.service.SessionService;
import com.cinema.cinema_backend.service.dto.CreateSessionDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    // Получить все сеансы (публичный)
    @GetMapping
    public List<SessionDTO> getAllSessions() {
        return sessionService.getAllSessions();
    }

    // Получить сеанс по ID (публичный)
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        try {
            SessionDTO session = sessionService.getSessionById(id);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Получить сеансы по фильму (публичный)
    @GetMapping("/movie/{movieId}")
    public List<SessionDTO> getSessionsByMovie(@PathVariable Long movieId) {
        return sessionService.getSessionsByMovie(movieId);
    }

    // Получить сеансы по залу (публичный)
    @GetMapping("/hall/{hallId}")
    public List<SessionDTO> getSessionsByHall(@PathVariable Long hallId) {
        return sessionService.getSessionsByHall(hallId);
    }

    // Получить предстоящие сеансы (публичный)
    @GetMapping("/upcoming")
    public List<SessionDTO> getUpcomingSessions() {
        return sessionService.getUpcomingSessions();
    }

    // Получить сеансы по кинотеатру (публичный)
    @GetMapping("/cinema/{cinemaId}")
    public List<SessionDTO> getSessionsByCinema(@PathVariable Long cinemaId) {
        return sessionService.getSessionsByCinema(cinemaId);
    }

    // Получить сеансы на дату (публичный)
    @GetMapping("/date/{date}")
    public List<SessionDTO> getSessionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return sessionService.getSessionsByDate(date);
    }

    // Получить доступные места на сеансе (публичный)
    @GetMapping("/{id}/available-seats")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long id) {
        try {
            int availableSeats = sessionService.getAvailableSeats(id);
            return ResponseEntity.ok(availableSeats);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Создать новый сеанс (только ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSession(@Valid @RequestBody CreateSessionDTO dto) {
        try {
            SessionDTO createdSession = sessionService.createSession(dto);
            return ResponseEntity.ok(createdSession);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Обновить сеанс (только ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSession(@PathVariable Long id,
                                           @Valid @RequestBody UpdateSessionDTO dto) {
        try {
            SessionDTO updatedSession = sessionService.updateSession(id, dto);
            return ResponseEntity.ok(updatedSession);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Удалить сеанс (только ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        try {
            sessionService.deleteSession(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}