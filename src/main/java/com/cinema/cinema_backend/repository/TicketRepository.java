package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.entity.Ticket;
import com.cinema.cinema_backend.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Найти все билеты пользователя
    List<Ticket> findByUserId(Long userId);

    // Найти все билеты на сеанс
    List<Ticket> findBySessionId(Long sessionId);

    // Найти билеты по статусу
    List<Ticket> findByStatus(TicketStatus status);

    // Найти активные билеты пользователя (не отмененные и не использованные)
    @Query("SELECT t FROM Ticket t WHERE t.user.id = :userId AND t.status IN ('PENDING', 'CONFIRMED')")
    List<Ticket> findActiveTicketsByUser(@Param("userId") Long userId);

    // Проверить, занято ли место на сеансе
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE " +
            "t.session.id = :sessionId AND " +
            "t.rowNumber = :rowNumber AND " +
            "t.seatNumber = :seatNumber AND " +
            "t.status IN ('PENDING', 'CONFIRMED')")
    boolean isSeatTaken(
            @Param("sessionId") Long sessionId,
            @Param("rowNumber") Integer rowNumber,
            @Param("seatNumber") Integer seatNumber);

    // Найти билеты, купленные за период
    List<Ticket> findByPurchaseTimeBetween(LocalDateTime start, LocalDateTime end);

    // Найти билеты пользователя по статусу
    List<Ticket> findByUserIdAndStatus(Long userId, TicketStatus status);
}