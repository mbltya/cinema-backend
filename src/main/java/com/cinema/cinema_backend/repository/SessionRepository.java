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

    List<Session> findByMovieId(Long movieId);

    List<Session> findByHallId(Long hallId);

    @Query("SELECT s FROM Session s WHERE s.hall.cinema.id = :cinemaId")
    List<Session> findByCinemaId(@Param("cinemaId") Long cinemaId);

    @Query("SELECT s FROM Session s WHERE DATE(s.startTime) = DATE(:date)")
    List<Session> findByDate(@Param("date") LocalDateTime date);

    List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Session s WHERE s.hall.id = :hallId AND " +
            "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Session> findConflictingSessions(
            @Param("hallId") Long hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Session s WHERE s.startTime > :now ORDER BY s.startTime ASC")
    List<Session> findUpcomingSessions(@Param("now") LocalDateTime now);

    default List<Session> findUpcomingSessions() {
        return findUpcomingSessions(LocalDateTime.now());
    }
}