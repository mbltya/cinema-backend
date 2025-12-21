package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.dto.SessionDTO;
import com.cinema.cinema_backend.dto.UpdateSessionDTO;
import com.cinema.cinema_backend.entity.Session;
import com.cinema.cinema_backend.entity.Movie;
import com.cinema.cinema_backend.entity.Hall;
import com.cinema.cinema_backend.exception.ResourceNotFoundException;
import com.cinema.cinema_backend.repository.SessionRepository;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.HallRepository;
import com.cinema.cinema_backend.service.dto.CreateSessionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    @Transactional(readOnly = true)
    public List<SessionDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SessionDTO getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
        return convertToDTO(session);
    }

    @Transactional
    public SessionDTO createSession(CreateSessionDTO dto) {
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", dto.getMovieId()));

        Hall hall = hallRepository.findById(dto.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", dto.getHallId()));

        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Session start time must be in the future");
        }

        validateSessionCreation(movie, hall, dto);

        Session session = new Session();
        session.setMovie(movie);
        session.setHall(hall);
        session.setStartTime(dto.getStartTime());
        session.setPrice(dto.getPrice());
        session.setFormat(dto.getFormat() != null ? dto.getFormat() : "2D");

        checkForScheduleConflicts(session);

        Session savedSession = sessionRepository.save(session);
        return convertToDTO(savedSession);
    }

    @Transactional
    public SessionDTO updateSession(Long id, UpdateSessionDTO dto) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));

        if (dto.getMovieId() != null) {
            Movie movie = movieRepository.findById(dto.getMovieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie", dto.getMovieId()));
            session.setMovie(movie);
        }

        if (dto.getHallId() != null) {
            Hall hall = hallRepository.findById(dto.getHallId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hall", dto.getHallId()));
            session.setHall(hall);
        }

        if (dto.getStartTime() != null) {
            if (dto.getStartTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Session start time must be in the future");
            }
            session.setStartTime(dto.getStartTime());
        }

        if (dto.getPrice() != null) {
            session.setPrice(dto.getPrice());
        }

        if (dto.getFormat() != null) {
            session.setFormat(dto.getFormat());
        }

        if (dto.getStartTime() != null || dto.getHallId() != null) {
            checkForScheduleConflicts(session);
        }

        Session updatedSession = sessionRepository.save(session);
        return convertToDTO(updatedSession);
    }

    @Transactional
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Session", id);
        }

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));

        if (!session.getTickets().isEmpty()) {
            throw new IllegalStateException("Cannot delete session with existing tickets");
        }

        sessionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByMovie(Long movieId) {
        return sessionRepository.findByMovieId(movieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByHall(Long hallId) {
        return sessionRepository.findByHallId(hallId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByCinema(Long cinemaId) {
        return sessionRepository.findByCinemaId(cinemaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getUpcomingSessions() {
        return sessionRepository.findUpcomingSessions(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return sessionRepository.findByStartTimeBetween(startOfDay, endOfDay).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionDTO> getSessionsByMovieAndDate(Long movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return sessionRepository.findByMovieId(movieId).stream()
                .filter(session ->
                        !session.getStartTime().isBefore(startOfDay) &&
                                !session.getStartTime().isAfter(endOfDay))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getAvailableSeats(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        int totalSeats = session.getHall().getRows() * session.getHall().getSeatsPerRow();
        int bookedSeats = session.getTickets().stream()
                .filter(ticket -> ticket.getStatus().name().equals("CONFIRMED") ||
                        ticket.getStatus().name().equals("PENDING"))
                .mapToInt(ticket -> 1)
                .sum();

        return totalSeats - bookedSeats;
    }

    private void validateSessionCreation(Movie movie, Hall hall, CreateSessionDTO dto) {
        if (movie.getAgeRating() != null && movie.getAgeRating() > 18) {
        }

        if ("IMAX".equals(dto.getFormat()) && !hall.getName().contains("IMAX")) {
            throw new IllegalArgumentException("IMAX format is only available in IMAX halls");
        }
    }

    private void checkForScheduleConflicts(Session session) {
        List<Session> conflicts = sessionRepository.findConflictingSessions(
                session.getHall().getId(),
                session.getStartTime(),
                session.getEndTime()
        );

        conflicts = conflicts.stream()
                .filter(c -> !c.getId().equals(session.getId()))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Session conflicts with existing sessions in this hall. Conflicting session IDs: " +
                            conflicts.stream().map(s -> s.getId().toString()).collect(Collectors.joining(", "))
            );
        }
    }

    private SessionDTO convertToDTO(Session session) {
        if (session == null) return null;

        return new SessionDTO(
                session.getId(),
                session.getMovie() != null ? session.getMovie().getId() : null,
                session.getMovie() != null ? session.getMovie().getTitle() : null,
                session.getHall() != null ? session.getHall().getId() : null,
                session.getHall() != null ? session.getHall().getName() : null,
                session.getHall() != null && session.getHall().getCinema() != null ?
                        session.getHall().getCinema().getId() : null,
                session.getHall() != null && session.getHall().getCinema() != null ?
                        session.getHall().getCinema().getName() : null,
                session.getStartTime(),
                session.getEndTime(),
                session.getPrice(),
                session.getFormat()
        );
    }
}