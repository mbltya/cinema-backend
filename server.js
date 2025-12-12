const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 5000;

app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true
}));
app.use(express.json());

// Проверка работы
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'Cinema API работает',
    endpoints: [
      '/api/auth/register',
      '/api/auth/login', 
      '/api/movies',
      '/api/sessions',
      '/api/tickets'
    ]
  });
});

// Регистрация
app.post('/api/auth/register', (req, res) => {
  console.log('Регистрация:', req.body);
  
  const { username, email, password, role = 'USER' } = req.body;
  
  res.json({ 
    success: true, 
    message: 'Регистрация успешна',
    user: { 
      id: Date.now(),
      username,
      email,
      role,
      name: username 
    }
  });
});

// Вход
app.post('/api/auth/login', (req, res) => {
  console.log('Вход:', req.body);
  
  const { email, password } = req.body;
  
  res.json({
    success: true,
    message: 'Вход выполнен',
    token: 'test-jwt-token-' + Date.now(),
    user: { 
      id: 1,
      email: email,
      username: email.split('@')[0],
      name: email.split('@')[0],
      role: 'USER'
    }
  });
});

// Фильмы - ИСПРАВЛЕННЫЙ ВАРИАНТ
app.get('/api/movies', (req, res) => {
  const movies = [
    { 
      id: 1, 
      title: 'Интерстеллар', 
      description: 'Фантастика о путешествии сквозь червоточину',
      duration: 169, // ← из duration_minutes в duration
      duration_minutes: 169, // оставляем для совместимости
      release_year: 2014,
      genre: 'Фантастика',
      posterUrl: 'https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_FMjpg_UX1000_.jpg',
      poster_url: 'https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_FMjpg_UX1000_.jpg'
    },
    { 
      id: 2, 
      title: 'Криминальное чтиво', 
      description: 'Культовый фильм Квентина Тарантино',
      duration: 154, // ← из duration_minutes в duration
      duration_minutes: 154,
      release_year: 1994,
      genre: 'Криминал',
      posterUrl: 'https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtNTBlZi00MTRlLWFjM2ItYzViMjE3YzI5MjljXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg',
      poster_url: 'https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtNTBlZi00MTRlLWFjM2ItYzViMjE3YzI5MjljXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg'
    },
    { 
      id: 3, 
      title: 'Зеленая миля', 
      description: 'Драма о тюремном надзирателе и заключенном с необычными способностями',
      duration: 189,
      duration_minutes: 189,
      release_year: 1999,
      genre: 'Драма',
      posterUrl: 'https://m.media-amazon.com/images/M/MV5BMTUxMzQyNjA5MF5BMl5BanBnXkFtZTYwOTU2NTY3._V1_FMjpg_UX1000_.jpg',
      poster_url: 'https://m.media-amazon.com/images/M/MV5BMTUxMzQyNjA5MF5BMl5BanBnXkFtZTYwOTU2NTY3._V1_FMjpg_UX1000_.jpg',
      ageRating: '16+'
    },
    { 
      id: 4, 
      title: 'Побег из Шоушенка', 
      description: 'История о дружбе и надежде в тюрьме',
      duration: 142,
      duration_minutes: 142,
      release_year: 1994,
      genre: 'Драма',
      posterUrl: 'https://m.media-amazon.com/images/M/MV5BNDE3ODcxYzMtY2YzZC00NmNlLWJiNDMtZDViZWM2MzIxZDYwXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_FMjpg_UX1000_.jpg',
      poster_url: 'https://m.media-amazon.com/images/M/MV5BNDE3ODcxYzMtY2YzZC00NmNlLWJiNDMtZDViZWM2MzIxZDYwXkEyXkFqcGdeQXVyNjAwNDUxODI@._V1_FMjpg_UX1000_.jpg',
      ageRating: '16+'
    }
  ];
  
  res.json({
    success: true,
    movies: movies
  });
});

// Сеансы - заглушки
app.get('/api/sessions', (req, res) => {
  const sessions = [
    {
      id: 1,
      movieId: 1,
      movieTitle: 'Интерстеллар',
      startTime: '2025-12-15T18:00:00',
      endTime: '2025-12-15T21:09:00',
      hallId: 1,
      hallName: 'Зал 1',
      price: 450,
      availableSeats: 120
    },
    {
      id: 2,
      movieId: 2,
      movieTitle: 'Криминальное чтиво',
      startTime: '2025-12-15T20:00:00',
      endTime: '2025-12-15T22:34:00',
      hallId: 2,
      hallName: 'Зал 2',
      price: 350,
      availableSeats: 80
    }
  ];
  
  res.json({ 
    success: true, 
    sessions: sessions 
  });
});

app.get('/api/sessions/upcoming', (req, res) => {
  res.json({ 
    success: true, 
    sessions: [] 
  });
});

// Кинотеатры
app.get('/api/cinemas', (req, res) => {
  const cinemas = [
    {
      id: 1,
      name: 'Киномакс',
      city: 'Москва',
      address: 'ул. Тверская, д. 15',
      phone: '+7 (495) 123-45-67'
    },
    {
      id: 2,
      name: 'IMAX Cinema',
      city: 'Москва',
      address: 'пр. Мира, д. 211',
      phone: '+7 (495) 987-65-43'
    }
  ];
  
  res.json({ 
    success: true, 
    cinemas: cinemas 
  });
});

// Залы
app.get('/api/halls', (req, res) => {
  const halls = [
    {
      id: 1,
      cinemaId: 1,
      name: 'Зал 1',
      capacity: 150,
      has3D: true,
      hasDolbyAtmos: true
    },
    {
      id: 2,
      cinemaId: 1,
      name: 'Зал 2',
      capacity: 100,
      has3D: true,
      hasDolbyAtmos: false
    }
  ];
  
  res.json({ 
    success: true, 
    halls: halls 
  });
});

// Билеты - заглушки
app.get('/api/tickets', (req, res) => {
  res.json({ 
    success: true, 
    tickets: [] 
  });
});

// Поиск фильмов
app.get('/api/movies/search', (req, res) => {
  const { title } = req.query;
  
  // Здесь будет реальный поиск
  res.json({ 
    success: true, 
    movies: [],
    searchQuery: title 
  });
});

// Получение фильма по ID
app.get('/api/movies/:id', (req, res) => {
  const { id } = req.params;
  
  // Здесь будет реальный запрос к БД
  res.json({ 
    success: true, 
    movie: null,
    message: `Фильм с ID ${id} не найден`
  });
});

// Создание заказа (билета)
app.post('/api/tickets', (req, res) => {
  console.log('Создание заказа:', req.body);
  
  res.json({
    success: true,
    message: 'Заказ создан (тестовый режим)',
    orderId: Date.now(),
    total: req.body.totalPrice || 0
  });
});

// Проверка места
app.get('/api/tickets/check-seat', (req, res) => {
  const { sessionId, rowNumber, seatNumber } = req.query;
  
  res.json({
    success: true,
    available: Math.random() > 0.5, // 50% шанс что место свободно
    sessionId,
    rowNumber,
    seatNumber
  });
});

app.listen(PORT, () => {
  console.log('🎬 Cinema Backend запущен на http://localhost:' + PORT);
  console.log('📡 API: http://localhost:' + PORT + '/api');
  console.log('📋 Доступные эндпоинты:');
  console.log('  - GET  /api/health');
  console.log('  - POST /api/auth/register');
  console.log('  - POST /api/auth/login');
  console.log('  - GET  /api/movies');
  console.log('  - GET  /api/sessions');
  console.log('  - GET  /api/cinemas');
  console.log('  - GET  /api/halls');
  console.log('  - POST /api/tickets');
});