const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');

const app = express();
const PORT = 5000;

// Middleware
app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true
}));
app.use(express.json());

// Подключение к PostgreSQL
const pool = new Pool({
  host: 'localhost',
  port: 5432,
  database: 'cinema_db',
  user: 'postgres',
  password: '1111',
});

// Проверка работы
app.get('/api/health', async (req, res) => {
  try {
    const result = await pool.query('SELECT NOW() as time');
    res.json({
      status: 'OK',
      message: 'Сервер работает',
      databaseTime: result.rows[0].time
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Регистрация - возвращаем полные данные
app.post('/api/auth/register', (req, res) => {
  console.log('Регистрация:', req.body);

  const { username, email, password, role = 'USER' } = req.body;

  res.json({
    success: true,
    message: 'Регистрация успешна',
    token: 'auth-token-' + Date.now(),
    user: {
      id: Date.now(),
      username,
      email,
      role,
      name: username
    }
  });
});

// Вход - возвращаем полные данные
app.post('/api/auth/login', (req, res) => {
  console.log('Вход:', req.body);

  const { email, password } = req.body;

  res.json({
    success: true,
    message: 'Вход выполнен',
    token: 'auth-token-' + Date.now(),
    user: {
      id: 1,
      email: email,
      username: email.split('@')[0],
      name: email.split('@')[0] || 'Пользователь',
      role: 'USER'
    }
  });
});

// ФИЛЬМЫ
app.get('/api/movies', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT
        id,
        title,
        description,
        duration_minutes as "durationMinutes",
        duration_minutes as duration,
        release_year as "releaseYear",
        genre,
        poster_url as "posterUrl",
        poster_url as "poster_url"
      FROM movies
      ORDER BY id
    `);

    res.json({
      success: true,
      movies: result.rows
    });
  } catch (error) {
    console.error('Ошибка БД при получении фильмов:', error);
    res.status(500).json({ error: error.message });
  }
});

// СЕАНСЫ (валюта в BYN)
app.get('/api/sessions', async (req, res) => {
  try {
    console.log('Запрос сеансов из БД...');

    const result = await pool.query(`
      SELECT
        s.id,
        s.movie_id as "movieId",
        m.title as "movieTitle",
        s.start_time as "startTime",
        s.hall_number as "hallNumber",
        'Зал ' || s.hall_number as "hallName",
        ROUND(s.price / 300, 2) as price, -- Конвертация RUB → BYN
        s.available_seats as "availableSeats",
        'Киномакс' as "cinemaName",
        '2D' as format
      FROM sessions s
      JOIN movies m ON s.movie_id = m.id
      WHERE s.start_time > NOW()
      ORDER BY s.start_time
    `);

    console.log(`Найдено сеансов: ${result.rows.length}`);

    if (result.rows.length === 0) {
      console.log('Сеансов нет в БД, создаем тестовый...');

      await pool.query(`
        INSERT INTO sessions (movie_id, start_time, hall_number, price, available_seats)
        SELECT id, NOW() + INTERVAL '2 hours', 1, 105.00, 100
        FROM movies
        LIMIT 1
        ON CONFLICT DO NOTHING;
      `);

      const newResult = await pool.query(`
        SELECT
          s.id,
          s.movie_id as "movieId",
          m.title as "movieTitle",
          s.start_time as "startTime",
          s.hall_number as "hallNumber",
          'Зал ' || s.hall_number as "hallName",
          ROUND(s.price / 300, 2) as price,
          s.available_seats as "availableSeats",
          'Киномакс' as "cinemaName",
          '2D' as format
        FROM sessions s
        JOIN movies m ON s.movie_id = m.id
        ORDER BY s.start_time
      `);

      res.json({
        success: true,
        sessions: newResult.rows
      });

    } else {
      res.json({
        success: true,
        sessions: result.rows
      });
    }

  } catch (error) {
    console.error('Ошибка при получении сеансов:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Предстоящие сеансы
app.get('/api/sessions/upcoming', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT
        s.id,
        s.movie_id as "movieId",
        m.title as "movieTitle",
        s.start_time as "startTime",
        s.hall_number as "hallNumber",
        'Зал ' || s.hall_number as "hallName",
        ROUND(s.price / 300, 2) as price,
        s.available_seats as "availableSeats",
        'Киномакс' as "cinemaName",
        '2D' as format
      FROM sessions s
      JOIN movies m ON s.movie_id = m.id
      WHERE s.start_time > NOW()
      ORDER BY s.start_time
      LIMIT 10
    `);

    res.json({
      success: true,
      sessions: result.rows
    });

  } catch (error) {
    console.error('Ошибка:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

// Получить сеанс по ID
app.get('/api/sessions/:id', async (req, res) => {
  try {
    const { id } = req.params;

    const result = await pool.query(`
      SELECT
        s.id,
        s.movie_id as "movieId",
        m.title as "movieTitle",
        s.start_time as "startTime",
        s.hall_number as "hallNumber",
        'Зал ' || s.hall_number as "hallName",
        ROUND(s.price / 300, 2) as price,
        s.available_seats as "availableSeats",
        'Киномакс' as "cinemaName"
      FROM sessions s
      JOIN movies m ON s.movie_id = m.id
      WHERE s.id = $1
    `, [id]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Сеанс не найден'
      });
    }

    res.json({
      success: true,
      session: result.rows[0]
    });

  } catch (error) {
    console.error('Ошибка:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

// СЕАНСЫ по фильму
app.get('/api/sessions/movie/:movieId', async (req, res) => {
  try {
    const { movieId } = req.params;

    const result = await pool.query(`
      SELECT
        s.id,
        s.movie_id as "movieId",
        m.title as "movieTitle",
        s.start_time as "startTime",
        s.hall_number as "hallNumber",
        'Зал ' || s.hall_number as "hallName",
        ROUND(s.price / 300, 2) as price,
        s.available_seats as "availableSeats",
        'Киномакс' as "cinemaName",
        '2D' as format
      FROM sessions s
      JOIN movies m ON s.movie_id = m.id
      WHERE s.movie_id = $1 AND s.start_time > NOW()
      ORDER BY s.start_time
    `, [movieId]);

    res.json({
      success: true,
      sessions: result.rows
    });

  } catch (error) {
    console.error('Ошибка:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

// СОЗДАНИЕ ЗАКАЗА (исправленный)
app.post('/api/orders', async (req, res) => {
  try {
    console.log('📦 Создание заказа:', req.body);

    const { userId = 1, sessionId, seats, totalPrice } = req.body;

    if (!sessionId || !seats || seats.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'Недостаточно данных: нужны sessionId и seats'
      });
    }

    // Преобразуем seats в массив если это строка
    const seatsArray = Array.isArray(seats) ? seats : [seats];

    const result = await pool.query(`
      INSERT INTO orders (user_id, session_id, seats, total_price, status)
      VALUES ($1, $2, $3, $4, 'confirmed')
      RETURNING id, session_id as "sessionId", seats, total_price as "totalPrice", status, created_at as "createdAt";
    `, [userId, sessionId, seatsArray, totalPrice || 0]);

    console.log('✅ Заказ создан:', result.rows[0]);

    res.json({
      success: true,
      message: 'Заказ успешно создан',
      order: result.rows[0]
    });

  } catch (error) {
    console.error('❌ Ошибка создания заказа:', error);

    // Если ошибка с seats, покажем подсказку
    if (error.message.includes('seats') || error.message.includes('столбец')) {
      console.log('⚠️  Вероятно, таблица orders не имеет столбца seats');
      console.log('   Выполните: ALTER TABLE orders ADD COLUMN seats TEXT[] DEFAULT \'{}\';');
    }

    res.status(500).json({
      success: false,
      error: error.message,
      message: 'Ошибка при создании заказа. Проверьте структуру таблицы orders.'
    });
  }
});

// ПОЛУЧЕНИЕ ЗАКАЗОВ ПОЛЬЗОВАТЕЛЯ (исправленный)
app.get('/api/orders/user/:userId', async (req, res) => {
  try {
    const { userId } = req.params;

    console.log(`📋 Запрос заказов пользователя ${userId}`);

    // Сначала проверяем структуру таблицы
    try {
      // Проверяем существование столбца seats
      const checkResult = await pool.query(`
        SELECT column_name
        FROM information_schema.columns
        WHERE table_name = 'orders' AND column_name = 'seats'
      `);

      const hasSeatsColumn = checkResult.rows.length > 0;

      if (!hasSeatsColumn) {
        console.log('⚠️  Столбец seats не существует в таблице orders');

        // Временно возвращаем без seats
        const result = await pool.query(`
          SELECT
            o.id,
            o.session_id as "sessionId",
            o.total_price as "totalPrice",
            o.status,
            o.created_at as "createdAt",
            s.start_time as "sessionTime",
            m.title as "movieTitle",
            m.poster_url as "posterUrl"
          FROM orders o
          LEFT JOIN sessions s ON o.session_id = s.id
          LEFT JOIN movies m ON s.movie_id = m.id
          WHERE o.user_id = $1
          ORDER BY o.created_at DESC
          LIMIT 20
        `, [userId || 1]);

        // Добавляем пустой массив seats к результату
        const ordersWithSeats = result.rows.map(order => ({
          ...order,
          seats: []
        }));

        return res.json({
          success: true,
          orders: ordersWithSeats,
          warning: 'Столбец seats отсутствует в таблице orders'
        });
      }

      // Если столбец существует, запрашиваем нормально
      const result = await pool.query(`
        SELECT
          o.id,
          o.session_id as "sessionId",
          o.seats,
          o.total_price as "totalPrice",
          o.status,
          o.created_at as "createdAt",
          s.start_time as "sessionTime",
          m.title as "movieTitle",
          m.poster_url as "posterUrl"
        FROM orders o
        LEFT JOIN sessions s ON o.session_id = s.id
        LEFT JOIN movies m ON s.movie_id = m.id
        WHERE o.user_id = $1
        ORDER BY o.created_at DESC
        LIMIT 20
      `, [userId || 1]);

      console.log(`📊 Найдено заказов: ${result.rows.length}`);

      res.json({
        success: true,
        orders: result.rows
      });

    } catch (dbError) {
      console.error('❌ Ошибка БД:', dbError.message);

      // Возвращаем тестовые данные
      res.json({
        success: true,
        orders: [
          {
            id: 1,
            sessionId: 1,
            seats: ['R1S1', 'R1S2'],
            totalPrice: 7.00,
            status: 'confirmed',
            createdAt: new Date().toISOString(),
            sessionTime: new Date(Date.now() + 2*60*60*1000).toISOString(),
            movieTitle: 'Интерстеллар',
            posterUrl: 'https://m.media-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_FMjpg_UX1000_.jpg'
          }
        ],
        warning: 'Используются тестовые данные из-за ошибки БД'
      });
    }

  } catch (error) {
    console.error('❌ Общая ошибка:', error);
    res.json({
      success: true,
      orders: [],
      error: error.message
    });
  }
});

// ПОЛУЧЕНИЕ ЗАКАЗА ПО ID
app.get('/api/orders/:orderId', async (req, res) => {
  try {
    const { orderId } = req.params;

    const result = await pool.query(`
      SELECT
        o.id,
        o.session_id as "sessionId",
        o.seats,
        o.total_price as "totalPrice",
        o.status,
        o.created_at as "createdAt",
        s.start_time as "sessionTime",
        m.title as "movieTitle",
        m.poster_url as "posterUrl"
      FROM orders o
      JOIN sessions s ON o.session_id = s.id
      JOIN movies m ON s.movie_id = m.id
      WHERE o.id = $1
    `, [orderId]);

    if (result.rows.length === 0) {
      return res.status(404).json({
        success: false,
        message: 'Заказ не найден'
      });
    }

    res.json({
      success: true,
      order: result.rows[0]
    });

  } catch (error) {
    console.error('Ошибка получения заказа:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// ТЕСТОВЫЙ ЭНДПОИНТ (можно удалить позже)
app.post('/api/orders/test', (req, res) => {
  console.log('🧪 Тестовый запрос на создание заказа:', req.body);

  res.json({
    success: true,
    message: 'Тестовый заказ создан (без БД)',
    order: {
      id: Math.floor(Math.random() * 1000),
      sessionId: req.body.sessionId,
      seats: req.body.seats,
      totalPrice: req.body.totalPrice,
      status: 'confirmed',
      createdAt: new Date().toISOString(),
      movieTitle: 'Тестовый фильм',
      sessionTime: new Date(Date.now() + 2*60*60*1000).toISOString()
    }
  });
});

// ПОЛУЧЕНИЕ ЗАКАЗОВ ПОЛЬЗОВАТЕЛЯ
app.get('/api/orders/user/:userId', async (req, res) => {
  try {
    const { userId } = req.params;

    const result = await pool.query(`
      SELECT
        o.id,
        o.session_id as "sessionId",
        o.seats,
        o.total_price as "totalPrice",
        o.status,
        o.created_at as "createdAt",
        s.start_time as "sessionTime",
        m.title as "movieTitle",
        m.poster_url as "posterUrl"
      FROM orders o
      JOIN sessions s ON o.session_id = s.id
      JOIN movies m ON s.movie_id = m.id
      WHERE o.user_id = $1
      ORDER BY o.created_at DESC
      LIMIT 20
    `, [userId || 1]);

    res.json({
      success: true,
      orders: result.rows
    });

  } catch (error) {
    console.error('Ошибка получения заказов:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// ПОЛУЧЕНИЕ ЗАКАЗОВ ПОЛЬЗОВАТЕЛЯ
app.get('/api/orders/user/:userId', async (req, res) => {
  try {
    const { userId } = req.params;

    const result = await pool.query(`
      SELECT
        o.id,
        o.session_id as "sessionId",
        o.seats,
        o.total_price as "totalPrice",
        o.status,
        o.created_at as "createdAt",
        s.start_time as "sessionTime",
        m.title as "movieTitle",
        m.poster_url as "posterUrl"
      FROM orders o
      JOIN sessions s ON o.session_id = s.id
      JOIN movies m ON s.movie_id = m.id
      WHERE o.user_id = $1
      ORDER BY o.created_at DESC
      LIMIT 20
    `, [userId || 1]);

    res.json({
      success: true,
      orders: result.rows
    });

  } catch (error) {
    console.error('Ошибка получения заказов:', error);
    res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Остальные эндпоинты
app.get('/api/tickets', (req, res) => {
  res.json({ success: true, tickets: [] });
});

app.get('/api/cinemas', (req, res) => {
  res.json({ success: true, cinemas: [] });
});

app.get('/api/halls', (req, res) => {
  res.json({ success: true, halls: [] });
});

app.listen(PORT, () => {
  console.log('🎬 Cinema Backend запущен на http://localhost:' + PORT);
  console.log('📡 API: http://localhost:' + PORT + '/api');
  console.log('🇧🇾 Валюта: BYN');
  console.log('📋 Доступные эндпоинты:');
  console.log('  - GET  /api/health');
  console.log('  - POST /api/auth/register');
  console.log('  - POST /api/auth/login');
  console.log('  - GET  /api/movies');
  console.log('  - GET  /api/sessions');
  console.log('  - GET  /api/sessions/upcoming');
  console.log('  - GET  /api/sessions/:id');
  console.log('  - POST /api/orders');
  console.log('  - GET  /api/orders/user/:userId');
});