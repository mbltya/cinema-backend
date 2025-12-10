package com.cinema.cinema_backend.service;

import com.cinema.cinema_backend.dto.TicketDTO;
import com.cinema.cinema_backend.entity.*;
import com.cinema.cinema_backend.exception.ResourceNotFoundException;
import com.cinema.cinema_backend.repository.TicketRepository;
import com.cinema.cinema_backend.repository.SessionRepository;
import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.service.dto.CreateTicketDTO;
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

    // Получить все билеты
    @Transactional(readOnly = true)
    public List<TicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить билет по ID
    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return convertToDTO(ticket);
    }

    // Создать билет (бронирование)
    @Transactional
    public TicketDTO createTicket(CreateTicketDTO dto) {
        // Проверка пользователя
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        // Проверка сеанса
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session", dto.getSessionId()));

        // Проверка, что сеанс еще не прошел
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book ticket for past session");
        }

        // Проверка возраста пользователя для возрастного ограничения
        if (session.getMovie().getAgeRating() != null && session.getMovie().getAgeRating() > 16) {
            // Здесь можно добавить логику проверки возраста пользователя
            // Например, если у пользователя есть поле даты рождения
        }

        // Проверка доступности места
        validateSeatAvailability(session.getId(), dto.getRowNumber(), dto.getSeatNumber());

        // Проверка, что место существует в зале
        Hall hall = session.getHall();
        if (dto.getRowNumber() > hall.getRows() || dto.getRowNumber() <= 0) {
            throw new IllegalArgumentException("Invalid row number. Hall has " + hall.getRows() + " rows");
        }
        if (dto.getSeatNumber() > hall.getSeatsPerRow() || dto.getSeatNumber() <= 0) {
            throw new IllegalArgumentException("Invalid seat number. Hall has " + hall.getSeatsPerRow() + " seats per row");
        }

        // Создание билета
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

    // Массовое создание билетов
    @Transactional
    public List<TicketDTO> createMultipleTickets(List<CreateTicketDTO> dtos) {
        if (dtos.isEmpty()) {
            throw new IllegalArgumentException("No tickets to create");
        }

        // Проверяем, что все билеты на один сеанс и одного пользователя
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

        // Проверяем доступность всех мест
        for (CreateTicketDTO dto : dtos) {
            validateSeatAvailability(dto.getSessionId(), dto.getRowNumber(), dto.getSeatNumber());
        }

        // Создаем билеты
        List<TicketDTO> createdTickets = new ArrayList<>();
        for (CreateTicketDTO dto : dtos) {
            createdTickets.add(createTicket(dto));
        }

        return createdTickets;
    }

    // Подтвердить оплату
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

    // Отменить билет
    @Transactional
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        // Проверяем, можно ли отменить билет
        if (ticket.getSession().getStartTime().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalStateException("Cannot cancel ticket less than 1 hour before session");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        return convertToDTO(ticket);
    }

    // Пометить билет как использованный
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

    // Обновить билет
    @Transactional
    public TicketDTO updateTicket(Long ticketId, CreateTicketDTO dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        // Можно обновлять только билеты в статусе PENDING
        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalStateException("Can only update tickets in PENDING status");
        }

        // Проверяем новое место, если оно изменилось
        if (!ticket.getRowNumber().equals(dto.getRowNumber()) ||
                !ticket.getSeatNumber().equals(dto.getSeatNumber())) {
            validateSeatAvailability(ticket.getSession().getId(), dto.getRowNumber(), dto.getSeatNumber());

            // Проверяем, что новое место существует в зале
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

    // Удалить билет
    @Transactional
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket", id);
        }
        ticketRepository.deleteById(id);
    }

    // Получить билеты пользователя
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить активные билеты пользователя
    @Transactional(readOnly = true)
    public List<TicketDTO> getActiveTicketsByUser(Long userId) {
        return ticketRepository.findActiveTicketsByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить билеты пользователя по статусу
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

    // Получить билеты на сеанс
    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsBySession(Long sessionId) {
        return ticketRepository.findBySessionId(sessionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получить забронированные места на сеанс
    @Transactional(readOnly = true)
    public List<String> getBookedSeats(Long sessionId) {
        return ticketRepository.findBySessionId(sessionId).stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PENDING ||
                        ticket.getStatus() == TicketStatus.CONFIRMED)
                .map(ticket -> ticket.getRowNumber() + "-" + ticket.getSeatNumber())
                .collect(Collectors.toList());
    }

    // Проверить доступность места
    @Transactional(readOnly = true)
    public boolean checkSeatAvailability(Long sessionId, Integer rowNumber, Integer seatNumber) {
        return !ticketRepository.isSeatTaken(sessionId, rowNumber, seatNumber);
    }

    // Получить статистику по билетам
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

        // Статистика по статусам
        Map<TicketStatus, Long> statusCount = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
        statistics.put("statusDistribution", statusCount);

        // Статистика по сеансам
        Map<Long, Long> sessionCount = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getSession().getId(), Collectors.counting()));
        statistics.put("ticketsPerSession", sessionCount);

        return statistics;
    }

    // Вспомогательные методы
    private void validateSeatAvailability(Long sessionId, Integer rowNumber, Integer seatNumber) {
        boolean seatTaken = ticketRepository.isSeatTaken(sessionId, rowNumber, seatNumber);
        if (seatTaken) {
            throw new IllegalArgumentException("Seat row " + rowNumber + ", seat " + seatNumber + " is already taken");
        }
    }

    private String generateQrCode() {
        return "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Преобразование в DTO
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