package com.cinema.cinema_backend;

import com.cinema.cinema_backend.entity.Role;
import com.cinema.cinema_backend.entity.User;
import com.cinema.cinema_backend.entity.Movie;
import com.cinema.cinema_backend.entity.Cinema;
import com.cinema.cinema_backend.entity.Hall;
import com.cinema.cinema_backend.entity.Session;
import com.cinema.cinema_backend.repository.UserRepository;
import com.cinema.cinema_backend.repository.MovieRepository;
import com.cinema.cinema_backend.repository.CinemaRepository;
import com.cinema.cinema_backend.repository.HallRepository;
import com.cinema.cinema_backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== Запуск DataSeeder для PostgreSQL ===");

        // 1. Создаем тестовых пользователей
        createUsers();

        // 2. Создаем тестовые фильмы
        createMovies();

        // 3. Создаем кинотеатры и залы
        createCinemasAndHalls();

        // 4. Создаем сеансы
        createSessions();

        System.out.println("=== DataSeeder завершен ===");
    }

    private void createUsers() {
        // Администратор
        if (!userRepository.existsByEmail("admin@cinema.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@cinema.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Создан администратор: admin@cinema.com / admin123");
        } else {
            System.out.println("Администратор уже существует");
        }

        // Пользователь
        if (!userRepository.existsByEmail("user@cinema.com")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@cinema.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(Role.USER);
            userRepository.save(user);
            System.out.println("Создан пользователь: user@cinema.com / user123");
        } else {
            System.out.println("Пользователь уже существует");
        }

        long userCount = userRepository.count();
        System.out.println("Всего пользователей в базе: " + userCount);
    }

    private void createMovies() {
        if (movieRepository.count() == 0) {
            System.out.println("Создание тестовых фильмов...");

            // Фильм 1: Интерстеллар
            Movie movie1 = new Movie();
            movie1.setTitle("Интерстеллар");
            movie1.setGenre("Фантастика, Драма, Приключения");
            movie1.setDuration(169);
            movie1.setDescription("Группа исследователей использует червоточину для путешествия за пределы нашей галактики в поисках нового дома для человечества.");
            movie1.setPosterUrl("https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_.jpg");
            movie1.setTrailerUrl("https://www.youtube.com/watch?v=zSWdZVtXT7E");
            movie1.setAgeRating(12);
            movieRepository.save(movie1);

            // Фильм 2: Начало
            Movie movie2 = new Movie();
            movie2.setTitle("Начало");
            movie2.setGenre("Фантастика, Триллер, Боевик");
            movie2.setDuration(148);
            movie2.setDescription("Вор, специализирующийся на краже секретов через сны, получает задание внедрить идею в подсознание человека.");
            movie2.setPosterUrl("https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_.jpg");
            movie2.setTrailerUrl("https://www.youtube.com/watch?v=YoHD9XEInc0");
            movie2.setAgeRating(12);
            movieRepository.save(movie2);

            // Фильм 3: Зеленая миля
            Movie movie3 = new Movie();
            movie3.setTitle("Зеленая миля");
            movie3.setGenre("Драма, Фэнтези, Криминал");
            movie3.setDuration(189);
            movie3.setDescription("Пол Эджкомб — начальник блока смертников в тюрьме «Холодная гора». Он рассказывает историю одного из своих подопечных — Джона Коффи.");
            movie3.setPosterUrl("https://m.media-amazon.com/images/M/MV5BMTUxMzQyNjA5MF5BMl5BanBnXkFtZTYwOTU2NTY3._V1_FMjpg_UX1000_.jpg");
            movie3.setTrailerUrl("https://www.youtube.com/watch?v=Ki4haFrqSrw");
            movie3.setAgeRating(16);
            movieRepository.save(movie3);

            // Фильм 4: Побег из Шоушенка
            Movie movie4 = new Movie();
            movie4.setTitle("Побег из Шоушенка");
            movie4.setGenre("Драма");
            movie4.setDuration(142);
            movie4.setDescription("Бухгалтер Энди Дюфрейн обвинён в убийстве собственной жены и её любовника. Оказавшись в тюрьме под названием Шоушенк, он сталкивается с жестокостью и беззаконием.");
            movie4.setPosterUrl("https://m.media-amazon.com/images/M/MV5BNDE3ODcxYzMtY2YzZC00NmNlLWJiNDMtZDViZWM2MzIxZDYwXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_FMjpg_UX1000_.jpg");
            movie4.setTrailerUrl("https://www.youtube.com/watch?v=PLl99DlL6b4");
            movie4.setAgeRating(16);
            movieRepository.save(movie4);

            // Фильм 5: Форрест Гамп
            Movie movie5 = new Movie();
            movie5.setTitle("Форрест Гамп");
            movie5.setGenre("Драма, Мелодрама, Комедия");
            movie5.setDuration(142);
            movie5.setDescription("От лица главного героя Форреста Гампа, слабоумного безобидного человека, рассказывается история его необыкновенной жизни.");
            movie5.setPosterUrl("https://m.media-amazon.com/images/M/MV5BNWIwODRlZTUtY2U3ZS00Yzg1LWJhNzYtMmZiYmEyNmU1NjMzXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_FMjpg_UX1000_.jpg");
            movie5.setTrailerUrl("https://www.youtube.com/watch?v=bLvqoHBptjg");
            movie5.setAgeRating(12);
            movieRepository.save(movie5);

            System.out.println("Создано 5 тестовых фильмов");
        } else {
            System.out.println("Фильмы уже существуют, всего: " + movieRepository.count());
        }
    }

    private void createCinemasAndHalls() {
        if (cinemaRepository.count() == 0) {
            System.out.println("Создание кинотеатров и залов...");

            // Кинотеатр 1
            Cinema cinema1 = new Cinema();
            cinema1.setName("Кинотеатр 'Октябрь'");
            cinema1.setCity("Минск");
            cinema1.setAddress("пр. Независимости, 73");
            cinemaRepository.save(cinema1);

            // Залы для кинотеатра 1
            Hall hall1 = new Hall();
            hall1.setName("Зал 1 (Большой)");
            hall1.setRows(15);
            hall1.setSeatsPerRow(20);
            hall1.setCinema(cinema1);
            hallRepository.save(hall1);

            Hall hall2 = new Hall();
            hall2.setName("Зал 2 (3D)");
            hall2.setRows(10);
            hall2.setSeatsPerRow(15);
            hall2.setCinema(cinema1);
            hallRepository.save(hall2);

            // Кинотеатр 2
            Cinema cinema2 = new Cinema();
            cinema2.setName("Кинотеатр 'Москва'");
            cinema2.setCity("Минск");
            cinema2.setAddress("пр. Победителей, 13");
            cinemaRepository.save(cinema2);

            // Залы для кинотеатра 2
            Hall hall3 = new Hall();
            hall3.setName("IMAX зал");
            hall3.setRows(12);
            hall3.setSeatsPerRow(25);
            hall3.setCinema(cinema2);
            hallRepository.save(hall3);

            Hall hall4 = new Hall();
            hall4.setName("VIP зал");
            hall4.setRows(8);
            hall4.setSeatsPerRow(10);
            hall4.setCinema(cinema2);
            hallRepository.save(hall4);

            System.out.println("Создано 2 кинотеатра и 4 зала");
        } else {
            System.out.println("Кинотеатры уже существуют");
        }
    }

    private void createSessions() {
        if (sessionRepository.count() == 0) {
            System.out.println("Создание тестовых сеансов...");

            // Получаем фильмы
            Movie interstellar = movieRepository.findByTitleContainingIgnoreCase("Интерстеллар")
                    .stream().findFirst().orElse(null);
            Movie inception = movieRepository.findByTitleContainingIgnoreCase("Начало")
                    .stream().findFirst().orElse(null);
            Movie greenMile = movieRepository.findByTitleContainingIgnoreCase("Зеленая миля")
                    .stream().findFirst().orElse(null);

            // Получаем залы
            Hall hall1 = hallRepository.findAll().stream()
                    .filter(h -> h.getName().contains("Зал 1"))
                    .findFirst().orElse(null);
            Hall hall3 = hallRepository.findAll().stream()
                    .filter(h -> h.getName().contains("IMAX"))
                    .findFirst().orElse(null);
            Hall hall4 = hallRepository.findAll().stream()
                    .filter(h -> h.getName().contains("VIP"))
                    .findFirst().orElse(null);

            if (interstellar != null && hall1 != null) {
                // Сеансы на сегодня
                Session session1 = new Session();
                session1.setMovie(interstellar);
                session1.setHall(hall1);
                session1.setStartTime(LocalDateTime.now().plusHours(2));
                session1.setPrice(12.50);
                session1.setFormat("2D");
                sessionRepository.save(session1);

                Session session2 = new Session();
                session2.setMovie(interstellar);
                session2.setHall(hall1);
                session2.setStartTime(LocalDateTime.now().plusHours(5));
                session2.setPrice(15.00);
                session2.setFormat("3D");
                sessionRepository.save(session2);
            }

            if (inception != null && hall3 != null) {
                Session session3 = new Session();
                session3.setMovie(inception);
                session3.setHall(hall3);
                session3.setStartTime(LocalDateTime.now().plusHours(3));
                session3.setPrice(18.00);
                session3.setFormat("IMAX");
                sessionRepository.save(session3);
            }

            if (greenMile != null && hall4 != null) {
                Session session4 = new Session();
                session4.setMovie(greenMile);
                session4.setHall(hall4);
                session4.setStartTime(LocalDateTime.now().plusHours(4));
                session4.setPrice(25.00);
                session4.setFormat("VIP");
                sessionRepository.save(session4);
            }

            System.out.println("Создано несколько тестовых сеансов");
        } else {
            System.out.println("Сеансы уже существуют");
        }
    }
}