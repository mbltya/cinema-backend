package com.cinema.cinema_backend;

import com.cinema.cinema_backend.entity.*;
import com.cinema.cinema_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Очистка базы данных в правильном порядке
        ticketRepository.deleteAll();
        sessionRepository.deleteAll();
        hallRepository.deleteAll();
        cinemaRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();

        System.out.println("Database cleared");

        // Создание пользователей
        User admin = new User("admin", "admin@cinema.com",
                passwordEncoder.encode("admin123"), Role.ADMIN);
        User user = new User("user", "user@cinema.com",
                passwordEncoder.encode("user123"), Role.USER);
        User john = new User("john_doe", "john@example.com",
                passwordEncoder.encode("password123"), Role.USER);

        userRepository.saveAll(Arrays.asList(admin, user, john));
        System.out.println("Test users created");

        // Создание фильмов
        Movie movie1 = new Movie("Интерстеллар", "Фантастика", 169,
                "Экипаж исследователей путешествует через червоточину в космосе.");
        movie1.setAgeRating(12);
        movie1.setPosterUrl("https://example.com/poster1.jpg");

        Movie movie2 = new Movie("Начало", "Триллер", 148,
                "Вор, крадущий корпоративные секреты, получает задание внедрить идею.");
        movie2.setAgeRating(16);
        movie2.setPosterUrl("https://example.com/poster2.jpg");

        Movie movie3 = new Movie("Побег из Шоушенка", "Драма", 142,
                "Два заключенных заводят дружбу в тюрьме.");
        movie3.setAgeRating(18);
        movie3.setPosterUrl("https://example.com/poster3.jpg");

        movieRepository.saveAll(Arrays.asList(movie1, movie2, movie3));
        System.out.println("Test movies created");

        // Создание кинотеатров
        Cinema cinema1 = new Cinema("Кинотеатр Победа", "Минск", "ул. Интернациональная, 20");
        Cinema cinema2 = new Cinema("Silver Screen", "Минск", "ТРЦ Galileo, пр-т Победителей, 9");

        cinemaRepository.saveAll(Arrays.asList(cinema1, cinema2));
        System.out.println("Test cinemas created");

        // Создание залов
        Cinema savedCinema1 = cinemaRepository.findById(1L).orElseThrow();
        Cinema savedCinema2 = cinemaRepository.findById(2L).orElseThrow();

        Hall hall1 = new Hall("Зал 1", 10, 15, savedCinema1);
        Hall hall2 = new Hall("Зал 2", 8, 12, savedCinema1);
        Hall hall3 = new Hall("IMAX", 12, 20, savedCinema2);

        hallRepository.saveAll(Arrays.asList(hall1, hall2, hall3));
        System.out.println("Test halls created");

        // Создание сеансов
        Movie savedMovie1 = movieRepository.findById(1L).orElseThrow();
        Movie savedMovie2 = movieRepository.findById(2L).orElseThrow();
        Movie savedMovie3 = movieRepository.findById(3L).orElseThrow();
        Hall savedHall1 = hallRepository.findById(1L).orElseThrow();
        Hall savedHall2 = hallRepository.findById(2L).orElseThrow();
        Hall savedHall3 = hallRepository.findById(3L).orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        Session session1 = new Session(savedMovie1, savedHall1,
                now.plusDays(1).withHour(18).withMinute(0), 12.5, "2D");
        Session session2 = new Session(savedMovie2, savedHall2,
                now.plusDays(1).withHour(20).withMinute(30), 15.0, "3D");
        Session session3 = new Session(savedMovie1, savedHall1,
                now.plusDays(2).withHour(16).withMinute(0), 10.0, "2D");
        Session session4 = new Session(savedMovie3, savedHall3,
                now.plusDays(3).withHour(19).withMinute(0), 20.0, "IMAX");

        sessionRepository.saveAll(Arrays.asList(session1, session2, session3, session4));
        System.out.println("Test sessions created");

        // Создание тестовых билетов
        User savedUser = userRepository.findByUsername("user").orElseThrow();
        User savedJohn = userRepository.findByUsername("john_doe").orElseThrow();
        Session savedSession1 = sessionRepository.findById(1L).orElseThrow();
        Session savedSession2 = sessionRepository.findById(2L).orElseThrow();

        // Билет 1: Подтвержденный
        Ticket ticket1 = new Ticket();
        ticket1.setUser(savedUser);
        ticket1.setSession(savedSession1);
        ticket1.setRowNumber(5);
        ticket1.setSeatNumber(7);
        ticket1.setPrice(savedSession1.getPrice());
        ticket1.setPurchaseTime(LocalDateTime.now().minusHours(2));
        ticket1.setStatus(TicketStatus.CONFIRMED);
        ticket1.setQrCode("TICKET-ABC123");

        // Билет 2: Ожидающий оплаты
        Ticket ticket2 = new Ticket();
        ticket2.setUser(savedJohn);
        ticket2.setSession(savedSession1);
        ticket2.setRowNumber(3);
        ticket2.setSeatNumber(10);
        ticket2.setPrice(savedSession1.getPrice());
        ticket2.setPurchaseTime(LocalDateTime.now().minusMinutes(30));
        ticket2.setStatus(TicketStatus.PENDING);
        ticket2.setQrCode("TICKET-DEF456");

        // Билет 3: Отмененный
        Ticket ticket3 = new Ticket();
        ticket3.setUser(savedUser);
        ticket3.setSession(savedSession2);
        ticket3.setRowNumber(2);
        ticket3.setSeatNumber(5);
        ticket3.setPrice(savedSession2.getPrice());
        ticket3.setPurchaseTime(LocalDateTime.now().minusDays(1));
        ticket3.setStatus(TicketStatus.CANCELLED);
        ticket3.setQrCode("TICKET-GHI789");

        // Билет 4: Использованный
        Ticket ticket4 = new Ticket();
        ticket4.setUser(savedJohn);
        ticket4.setSession(savedSession2);
        ticket4.setRowNumber(4);
        ticket4.setSeatNumber(8);
        ticket4.setPrice(savedSession2.getPrice());
        ticket4.setPurchaseTime(LocalDateTime.now().minusDays(2));
        ticket4.setStatus(TicketStatus.USED);
        ticket4.setQrCode("TICKET-JKL012");

        ticketRepository.saveAll(Arrays.asList(ticket1, ticket2, ticket3, ticket4));
        System.out.println("Test tickets created");
        System.out.println("Total tickets in DB: " + ticketRepository.count());
    }
}