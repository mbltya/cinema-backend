package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.dto.HallDTO;
import com.cinema.cinema_backend.entity.Hall;
import com.cinema.cinema_backend.service.HallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/halls")
public class HallController {

    @Autowired
    private HallService hallService;

    // Получить все залы (публичный)
    @GetMapping
    public List<HallDTO> getAllHalls() {
        return hallService.getAllHalls().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить зал по ID (публичный)
    @GetMapping("/{id}")
    public ResponseEntity<HallDTO> getHallById(@PathVariable Long id) {
        return hallService.getHallById(id)
                .map(hall -> ResponseEntity.ok(convertToDTO(hall)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Получить залы по кинотеатру (публичный)
    @GetMapping("/cinema/{cinemaId}")
    public List<HallDTO> getHallsByCinema(@PathVariable Long cinemaId) {
        return hallService.getHallsByCinemaId(cinemaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Создать зал (только ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HallDTO> createHall(@RequestBody Hall hall) {
        try {
            Hall createdHall = hallService.createHall(hall);
            return ResponseEntity.ok(convertToDTO(createdHall));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Обновить зал (только ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HallDTO> updateHall(@PathVariable Long id, @RequestBody Hall hall) {
        try {
            Hall updatedHall = hallService.updateHall(id, hall);
            return ResponseEntity.ok(convertToDTO(updatedHall));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Удалить зал (только ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        try {
            hallService.deleteHall(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Вспомогательный метод для преобразования в DTO
    private HallDTO convertToDTO(Hall hall) {
        if (hall == null) {
            return null;
        }

        HallDTO dto = new HallDTO();
        dto.setId(hall.getId());
        dto.setName(hall.getName());
        dto.setRows(hall.getRows());
        dto.setSeatsPerRow(hall.getSeatsPerRow());

        if (hall.getCinema() != null) {
            dto.setCinemaId(hall.getCinema().getId());
            dto.setCinemaName(hall.getCinema().getName());
        }

        return dto;
    }
}