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

    @GetMapping
    public List<SessionDTO> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable Long id) {
        try {
            SessionDTO session = sessionService.getSessionById(id);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/movie/{movieId}")
    public List<SessionDTO> getSessionsByMovie(@PathVariable Long movieId) {
        return sessionService.getSessionsByMovie(movieId);
    }

    @GetMapping("/hall/{hallId}")
    public List<SessionDTO> getSessionsByHall(@PathVariable Long hallId) {
        return sessionService.getSessionsByHall(hallId);
    }

    @GetMapping("/upcoming")
    public List<SessionDTO> getUpcomingSessions() {
        return sessionService.getUpcomingSessions();
    }

    @GetMapping("/cinema/{cinemaId}")
    public List<SessionDTO> getSessionsByCinema(@PathVariable Long cinemaId) {
        return sessionService.getSessionsByCinema(cinemaId);
    }

    @GetMapping("/date/{date}")
    public List<SessionDTO> getSessionsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return sessionService.getSessionsByDate(date);
    }

    @GetMapping("/{id}/available-seats")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long id) {
        try {
            int availableSeats = sessionService.getAvailableSeats(id);
            return ResponseEntity.ok(availableSeats);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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