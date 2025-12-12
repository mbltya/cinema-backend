// cinema-backend/seed-sessions.js
const { Pool } = require('pg');
require('dotenv').config();

const pool = new Pool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

async function seedSessions() {
  try {
    // 1. Сначала получим существующие фильмы
    const moviesResult = await pool.query('SELECT id, title FROM movies ORDER BY id');
    const movies = moviesResult.rows;

    if (movies.length === 0) {
      console.log('❌ Нет фильмов в БД. Сначала создайте фильмы.');
      return;
    }

    console.log('Найдены фильмы:', movies.map(m => `${m.id}: ${m.title}`));

    // 2. Используем ID существующих фильмов
    const sessions = [
      {
        movie_id: movies[0].id, // Первый фильм
        start_time: new Date(Date.now() + 2*60*60*1000), // через 2 часа
        hall_number: 1,
        price: 450,
        available_seats: 120
      },
      {
        movie_id: movies.length > 1 ? movies[1].id : movies[0].id, // Второй фильм или снова первый
        start_time: new Date(Date.now() + 4*60*60*1000), // через 4 часа
        hall_number: 2,
        price: 350,
        available_seats: 80
      },
      {
        movie_id: movies[0].id, // Первый фильм снова
        start_time: new Date(Date.now() + 24*60*60*1000), // завтра
        hall_number: 3,
        price: 400,
        available_seats: 150
      }
    ];

    console.log('Создаем сеансы...');

    for (const session of sessions) {
      await pool.query(`
        INSERT INTO sessions (movie_id, start_time, hall_number, price, available_seats)
        VALUES ($1, $2, $3, $4, $5)
        ON CONFLICT DO NOTHING;
      `, [session.movie_id, session.start_time, session.hall_number, session.price, session.available_seats]);

      console.log(`✅ Сеанс для фильма ID ${session.movie_id} создан`);
    }

    console.log('✅ Все сеансы созданы');

    // 3. Покажем что создалось
    const result = await pool.query(`
      SELECT s.*, m.title as movie_title
      FROM sessions s
      JOIN movies m ON s.movie_id = m.id
      ORDER BY s.start_time
    `);

    console.log('\nВсего сеансов:', result.rows.length);
    console.log('Список сеансов:');
    result.rows.forEach(row => {
      console.log(`  - ${row.movie_title}: ${new Date(row.start_time).toLocaleString()} (Зал ${row.hall_number})`);
    });

  } catch (error) {
    console.error('❌ Ошибка:', error.message);
  } finally {
    await pool.end();
  }
}

seedSessions();