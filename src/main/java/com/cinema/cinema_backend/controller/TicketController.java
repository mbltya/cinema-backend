package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.dto.TicketDTO;
import com.cinema.cinema_backend.security.CustomUserDetails;
import com.cinema.cinema_backend.service.TicketService;
import com.cinema.cinema_backend.service.dto.CreateTicketDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // Получить все билеты (только ADMIN)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TicketDTO> getAllTickets() {
        return ticketService.getAllTickets();
    }

    // Получить билет по ID (владелец или ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        try {
            TicketDTO ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Получить билеты пользователя (владелец или ADMIN)
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public List<TicketDTO> getTicketsByUser(@PathVariable Long userId, Principal principal) {
        System.out.println("Principal ID: " + ((CustomUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getId());
        System.out.println("Requested user ID: " + userId);
        return ticketService.getTicketsByUser(userId);
    }

    // Получить активные билеты пользователя (владелец или ADMIN)
    @GetMapping("/user/{userId}/active")
    @PreAuthorize("isAuthenticated()")
    public List<TicketDTO> getActiveTicketsByUser(@PathVariable Long userId) {
        return ticketService.getActiveTicketsByUser(userId);
    }

    // Получить билеты на сеанс (ADMIN или связанные пользователи)
    @GetMapping("/session/{sessionId}")
    public List<TicketDTO> getTicketsBySession(@PathVariable Long sessionId) {
        return ticketService.getTicketsBySession(sessionId);
    }

    // Проверить доступность места (публичный)
    @GetMapping("/check-seat")
    public ResponseEntity<Boolean> checkSeatAvailability(
            @RequestParam Long sessionId,
            @RequestParam Integer rowNumber,
            @RequestParam Integer seatNumber) {
        boolean isAvailable = ticketService.checkSeatAvailability(sessionId, rowNumber, seatNumber);
        return ResponseEntity.ok(isAvailable);
    }

    // Создать билет (аутентифицированный пользователь)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createTicket(@Valid @RequestBody CreateTicketDTO dto) {
        try {
            TicketDTO createdTicket = ticketService.createTicket(dto);
            return ResponseEntity.ok(createdTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Подтвердить оплату билета (владелец или ADMIN)
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmTicket(@PathVariable Long id) {
        try {
            TicketDTO confirmedTicket = ticketService.confirmTicket(id);
            return ResponseEntity.ok(confirmedTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Отменить билет (владелец или ADMIN)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id) {
        try {
            TicketDTO cancelledTicket = ticketService.cancelTicket(id);
            return ResponseEntity.ok(cancelledTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Удалить билет (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}