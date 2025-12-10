package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HallRepository extends JpaRepository<Hall, Long> {
    List<Hall> findByCinemaId(Long cinemaId);
}
