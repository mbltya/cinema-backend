package com.cinema.cinema_backend.controller;

import com.cinema.cinema_backend.dto.CinemaWithHallsDTO;
import com.cinema.cinema_backend.dto.HallDTO;
import com.cinema.cinema_backend.entity.Cinema;
import com.cinema.cinema_backend.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    // Получить все кинотеатры (публичный)
    @GetMapping
    public List<CinemaWithHallsDTO> getAllCinemas() {
        return cinemaService.getAllCinemas().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить кинотеатр по ID (публичный)
    @GetMapping("/{id}")
    public ResponseEntity<CinemaWithHallsDTO> getCinemaById(@PathVariable Long id) {
        return cinemaService.getCinemaById(id)
                .map(cinema -> ResponseEntity.ok(convertToDTO(cinema)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Получить кинотеатры по городу (публичный)
    @GetMapping("/city/{city}")
    public List<CinemaWithHallsDTO> getCinemasByCity(@PathVariable String city) {
        return cinemaService.getCinemasByCity(city).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Создать кинотеатр (только ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaWithHallsDTO> createCinema(@RequestBody Cinema cinema) {
        try {
            Cinema createdCinema = cinemaService.createCinema(cinema);
            return ResponseEntity.ok(convertToDTO(createdCinema));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Обновить кинотеатр (только ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CinemaWithHallsDTO> updateCinema(@PathVariable Long id, @RequestBody Cinema cinema) {
        try {
            Cinema updatedCinema = cinemaService.updateCinema(id, cinema);
            return ResponseEntity.ok(convertToDTO(updatedCinema));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Удалить кинотеатр (только ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCinema(@PathVariable Long id) {
        try {
            cinemaService.deleteCinema(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Вспомогательный метод для преобразования в DTO
    private CinemaWithHallsDTO convertToDTO(Cinema cinema) {
        if (cinema == null) {
            return null;
        }

        CinemaWithHallsDTO dto = new CinemaWithHallsDTO();
        dto.setId(cinema.getId());
        dto.setName(cinema.getName());
        dto.setCity(cinema.getCity());
        dto.setAddress(cinema.getAddress());

        return dto;
    }
}