package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.entity.Cinema;
import com.cinema.cinema_backend.exception.ResourceNotFoundException;
import com.cinema.cinema_backend.repository.CinemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CinemaService {

    @Autowired
    private CinemaRepository cinemaRepository;

    @Transactional(readOnly = true)
    public List<Cinema> getAllCinemas() {
        return cinemaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cinema> getCinemaById(Long id) {
        return cinemaRepository.findById(id);
    }

    @Transactional
    public Cinema createCinema(Cinema cinema) {
        return cinemaRepository.save(cinema);
    }

    @Transactional
    public Cinema updateCinema(Long id, Cinema cinemaDetails) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", id));

        if (cinemaDetails.getName() != null) {
            cinema.setName(cinemaDetails.getName());
        }
        if (cinemaDetails.getCity() != null) {
            cinema.setCity(cinemaDetails.getCity());
        }
        if (cinemaDetails.getAddress() != null) {
            cinema.setAddress(cinemaDetails.getAddress());
        }

        return cinemaRepository.save(cinema);
    }

    @Transactional
    public void deleteCinema(Long id) {
        if (!cinemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cinema", id);
        }
        cinemaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Cinema> getCinemasByCity(String city) {
        return cinemaRepository.findByCity(city);
    }
}