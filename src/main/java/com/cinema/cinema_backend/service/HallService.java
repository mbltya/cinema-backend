package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.entity.Hall;
import com.cinema.cinema_backend.exception.ResourceNotFoundException;
import com.cinema.cinema_backend.repository.HallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HallService {

    @Autowired
    private HallRepository hallRepository;

    @Transactional(readOnly = true)
    public List<Hall> getAllHalls() {
        return hallRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Hall> getHallById(Long id) {
        return hallRepository.findById(id);
    }

    @Transactional
    public Hall createHall(Hall hall) {
        return hallRepository.save(hall);
    }

    @Transactional
    public Hall updateHall(Long id, Hall hallDetails) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", id));

        if (hallDetails.getName() != null) {
            hall.setName(hallDetails.getName());
        }
        if (hallDetails.getRows() != null) {
            hall.setRows(hallDetails.getRows());
        }
        if (hallDetails.getSeatsPerRow() != null) {
            hall.setSeatsPerRow(hallDetails.getSeatsPerRow());
        }

        return hallRepository.save(hall);
    }

    @Transactional
    public void deleteHall(Long id) {
        if (!hallRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hall", id);
        }
        hallRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Hall> getHallsByCinemaId(Long cinemaId) {
        return hallRepository.findByCinemaId(cinemaId);
    }
}