import { Pool } from 'pg';
import dotenv from 'dotenv';

dotenv.config();

async function setupDatabase() {
  console.log('🔄 Начинаем настройку базы данных...');
  
  const pool = new Pool({
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT),
    database: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
  });

  try {
    await pool.query(`
      CREATE TABLE IF NOT EXISTS users (
        id SERIAL PRIMARY KEY,
        email VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        name VARCHAR(100) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    console.log('✅ Таблица "users" создана');

    await pool.query(`
      CREATE TABLE IF NOT EXISTS movies (
        id SERIAL PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        description TEXT,
        duration_minutes INTEGER NOT NULL,
        release_year INTEGER,
        genre VARCHAR(100),
        poster_url VARCHAR(500),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    console.log('✅ Таблица "movies" создана');

    await pool.query(`
      CREATE TABLE IF NOT EXISTS sessions (
        id SERIAL PRIMARY KEY,
        movie_id INTEGER REFERENCES movies(id) ON DELETE CASCADE,
        start_time TIMESTAMP NOT NULL,
        hall_number INTEGER NOT NULL,
        price DECIMAL(10, 2) NOT NULL,
        available_seats INTEGER NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    console.log('✅ Таблица "sessions" создана');

    await pool.query(`
      CREATE TABLE IF NOT EXISTS orders (
        id SERIAL PRIMARY KEY,
        user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
        session_id INTEGER REFERENCES sessions(id) ON DELETE CASCADE,
        seats_count INTEGER NOT NULL,
        total_price DECIMAL(10, 2) NOT NULL,
        status VARCHAR(50) DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    console.log('✅ Таблица "orders" создана');

    await pool.query(`
      INSERT INTO movies (title, description, duration_minutes, release_year, genre, poster_url)
      VALUES ('Интерстеллар', 'Фантастика о путешествии сквозь червоточину', 169, 2014, 'Фантастика', 'https://example.com/poster1.jpg')
      ON CONFLICT DO NOTHING;
    `);
    console.log('✅ Тестовый фильм добавлен');

    console.log('\n🎉 База данных успешно настроена!');
    console.log('📊 Созданы таблицы: users, movies, sessions, orders');

  } catch (error: any) {
    console.error('❌ Ошибка:', error.message);
  } finally {
    await pool.end();
  }
}

setupDatabase();