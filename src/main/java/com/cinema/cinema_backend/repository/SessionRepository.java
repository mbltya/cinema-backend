package com.cinema.cinema_backend.repository;

import com.cinema.cinema_backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // Найти все сеансы по ID фильма
    List<Session> findByMovieId(Long movieId);

    // Найти все сеансы по ID зала
    List<Session> findByHallId(Long hallId);

    // Найти все сеансы по кинотеатру
    @Query("SELECT s FROM Session s WHERE s.hall.cinema.id = :cinemaId")
    List<Session> findByCinemaId(@Param("cinemaId") Long cinemaId);

    // Найти сеансы по дате (в определенный день)
    @Query("SELECT s FROM Session s WHERE DATE(s.startTime) = DATE(:date)")
    List<Session> findByDate(@Param("date") LocalDateTime date);

    // Найти сеансы в промежутке времени
    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Найти конфликтующие сеансы (для проверки расписания)
    @Query("SELECT s FROM Session s WHERE s.hall.id = :hallId AND " +
            "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Session> findConflictingSessions(
            @Param("hallId") Long hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Найти будущие сеансы
    @Query("SELECT s FROM Session s WHERE s.startTime > :now ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessions(@Param("now") LocalDateTime now);

    // Также добавьте этот метод для совместимости
    default List<Session> findUpcomingSessions() {
        return findUpcomingSessions(LocalDateTime.now());
    }
}