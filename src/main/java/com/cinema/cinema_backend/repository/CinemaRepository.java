package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    List<Cinema> findByCity(String city);
}
