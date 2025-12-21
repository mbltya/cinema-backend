package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.dto.TicketDTO;
import com.cinema.cinema_backend.entity.*;
import com.cinema.cinema_backend.exception.ResourceNotFoundException;
import com.cinema.cinema_backend.repository.TicketRepository;
import com.cinema.cinema_backend.repository.SessionRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.service.dto.CreateTicketDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO createTicket(CreateTicketDTO dto) {
        System.out.println("\n=== CREATING TICKET ===");
        System.out.println("User ID: " + dto.getUserId());
        System.out.println("Session ID: " + dto.getSessionId());
        System.out.println("Row: " + dto.getRowNumber() + ", Seat: " + dto.getSeatNumber());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", dto.getSessionId()));

        System.out.println("Session exists: " + (session != null));
        System.out.println("Session time: " + session.getStartTime());

        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            System.out.println("ERROR: Session already started");
            throw new IllegalArgumentException("Cannot book ticket for past session");
        }

        System.out.println("Checking seat availability...");
        validateSeatAvailability(session.getId(), dto.getRowNumber(), dto.getSeatNumber());

        Hall hall = session.getHall();
        System.out.println("Hall rows: " + hall.getRows() + ", seats per row: " + hall.getSeatsPerRow());

        if (dto.getRowNumber() > hall.getRows() || dto.getRowNumber() <= 0) {
            throw new IllegalArgumentException("Invalid row number. Hall has " + hall.getRows() + " rows");
        }
        if (dto.getSeatNumber() > hall.getSeatsPerRow() || dto.getSeatNumber() <= 0) {
            throw new IllegalArgumentException("Invalid seat number. Hall has " + hall.getSeatsPerRow() + " seats per row");
        }

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setSession(session);
        ticket.setRowNumber(dto.getRowNumber());
        ticket.setSeatNumber(dto.getSeatNumber());
        ticket.setPrice(session.getPrice());
        ticket.setPurchaseTime(LocalDateTime.now());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setQrCode(generateQrCode());

        Ticket savedTicket = ticketRepository.save(ticket);
        return convertToDTO(savedTicket);
    }

    @Transactional
    public List<TicketDTO> createMultipleTickets(List<CreateTicketDTO> dtos) {
        if (dtos.isEmpty()) {
            throw new IllegalArgumentException("No tickets to create");
        }

        Long sessionId = dtos.get(0).getSessionId();
        Long userId = dtos.get(0).getUserId();

        for (CreateTicketDTO dto : dtos) {
            if (!dto.getSessionId().equals(sessionId)) {
                throw new IllegalArgumentException("All tickets must be for the same session");
            }
            if (!dto.getUserId().equals(userId)) {
                throw new IllegalArgumentException("All tickets must be for the same user");
            }
        }

        for (CreateTicketDTO dto : dtos) {
            validateSeatAvailability(dto.getSessionId(), dto.getRowNumber(), dto.getSeatNumber());
        }

        List<TicketDTO> createdTickets = new ArrayList<>();
        for (CreateTicketDTO dto : dtos) {
            createdTickets.add(createTicket(dto));
        }

        return createdTickets;
    }

    @Transactional
    public TicketDTO confirmTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalStateException("Ticket is not in pending status. Current status: " + ticket.getStatus());
        }

        if (ticket.getSession().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot confirm ticket for past session");
        }

        ticket.setStatus(TicketStatus.CONFIRMED);
        ticketRepository.save(ticket);

        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getSession().getStartTime().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalStateException("Cannot cancel ticket less than 1 hour before session");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO useTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed tickets can be marked as used");
        }

        if (ticket.getSession().getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot use ticket before session start time");
        }

        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);

        return convertToDTO(ticket);
    }

    @Transactional
    public TicketDTO updateTicket(Long ticketId, CreateTicketDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalStateException("Can only update tickets in PENDING status");
        }

        if (!ticket.getRowNumber().equals(dto.getRowNumber()) ||
                !ticket.getSeatNumber().equals(dto.getSeatNumber())) {
            validateSeatAvailability(ticket.getSession().getId(), dto.getRowNumber(), dto.getSeatNumber());

            Hall hall = ticket.getSession().getHall();
            if (dto.getRowNumber() > hall.getRows() || dto.getRowNumber() <= 0) {
                throw new IllegalArgumentException("Invalid row number");
            }
            if (dto.getSeatNumber() > hall.getSeatsPerRow() || dto.getSeatNumber() <= 0) {
                throw new IllegalArgumentException("Invalid seat number");
            }

            ticket.setRowNumber(dto.getRowNumber());
            ticket.setSeatNumber(dto.getSeatNumber());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return convertToDTO(updatedTicket);
    }

    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket", id);
        }
        ticketRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getActiveTicketsByUser(Long userId) {
        return ticketRepository.findActiveTicketsByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByUserAndStatus(Long userId, String status) {
        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            return ticketRepository.findByUserIdAndStatus(userId, ticketStatus).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ticket status: " + status);
        }
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsBySession(Long sessionId) {
        return ticketRepository.findBySessionId(sessionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getBookedSeats(Long sessionId) {
        return ticketRepository.findBySessionId(sessionId).stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING ||
                        ticket.getStatus() == TicketStatus.CONFIRMED)
                .map(ticket -> ticket.getRowNumber() + "-" + ticket.getSeatNumber())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean checkSeatAvailability(Long sessionId, Integer rowNumber, Integer seatNumber) {
        return !ticketRepository.isSeatTaken(sessionId, rowNumber, seatNumber);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTicketStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Ticket> tickets = ticketRepository.findByPurchaseTimeBetween(startDateTime, endDateTime);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("period", startDate + " to " + endDate);
        statistics.put("totalTickets", tickets.size());
        statistics.put("totalRevenue", tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.CANCELLED)
                .mapToDouble(Ticket::getPrice)
                .sum());

        Map<TicketStatus, Long> statusCount = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
        statistics.put("statusDistribution", statusCount);

        Map<Long, Long> sessionCount = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getSession().getId(), Collectors.counting()));
        statistics.put("ticketsPerSession", sessionCount);

        return statistics;
    }

    public boolean isTicketOwner(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));
        return ticket.getUser().getId().equals(userId);
    }

    private void validateSeatAvailability(Long sessionId, Integer rowNumber, Integer seatNumber) {
        System.out.println("Validating seat: session=" + sessionId +
                ", row=" + rowNumber + ", seat=" + seatNumber);

        boolean seatTaken = ticketRepository.isSeatTaken(sessionId, rowNumber, seatNumber);
        System.out.println("Seat taken: " + seatTaken);

        if (seatTaken) {
            List<Ticket> existingTickets = ticketRepository.findBySessionId(sessionId);
            existingTickets.stream()
                    .filter(t -> t.getRowNumber().equals(rowNumber) && t.getSeatNumber().equals(seatNumber))
                    .forEach(t -> System.out.println("Existing ticket: " + t.getId() + " status: " + t.getStatus()));

            throw new IllegalArgumentException("Seat row " + rowNumber + ", seat " + seatNumber + " is already taken");
        }
    }

    private String generateQrCode() {
        return "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TicketDTO convertToDTO(Ticket ticket) {
        if (ticket == null) return null;

        return new TicketDTO(
                ticket.getId(),
                ticket.getUser() != null ? ticket.getUser().getId() : null,
                ticket.getUser() != null ? ticket.getUser().getUsername() : null,
                ticket.getSession() != null ? ticket.getSession().getId() : null,
                ticket.getSession() != null && ticket.getSession().getMovie() != null ?
                        ticket.getSession().getMovie().getTitle() : null,
                ticket.getSession() != null ? ticket.getSession().getStartTime() : null,
                ticket.getRowNumber(),
                ticket.getSeatNumber(),
                ticket.getPrice(),
                ticket.getPurchaseTime(),
                ticket.getStatus(),
                ticket.getQrCode()
        );
    }
}