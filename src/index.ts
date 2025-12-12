import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { Pool } from 'pg';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors({
  origin: 'http://localhost:3000', // Ваш фронтенд
  credentials: true
}));
app.use(express.json());

const pool = new Pool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

app.get('/api/health', async (req, res) => {
  try {
    const result = await pool.query('SELECT NOW() as time');
    res.json({ 
      status: 'OK', 
      message: 'Cinema API работает',
      databaseTime: result.rows[0].time,
      tables: ['users', 'movies', 'sessions', 'orders']
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/auth/register', async (req, res) => {
  const { email, password, name } = req.body;
  
  console.log('Регистрация:', { email, name });
  
  res.status(201).json({
    success: true,
    message: 'Регистрация успешна (тестовый режим)',
    user: { email, name, id: Date.now() }
  });
});

app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  
  console.log('Вход:', { email });
  
  res.json({
    success: true,
    message: 'Вход выполнен (тестовый режим)',
    token: 'fake-jwt-token-for-development',
    user: { email, name: 'Тестовый пользователь', id: 1 }
  });
});

app.get('/api/movies', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM movies ORDER BY id');
    res.json({
      success: true,
      movies: result.rows
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

app.listen(PORT, () => {
  console.log(`🎬 Cinema Backend запущен на http://localhost:${PORT}`);
  console.log(`📡 API доступно по адресу: http://localhost:${PORT}/api`);
  console.log(`🩺 Проверка здоровья: http://localhost:${PORT}/api/health`);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('❌ Необработанное отклонение промиса:', reason);
});

process.on('uncaughtException', (error) => {
  console.error('❌ Необработанное исключение:', error);
  process.exit(1);
});

process.on('SIGTERM', () => {
  console.log('📴 Получен SIGTERM, завершаем работу...');
  pool.end();
  process.exit(0);
});

process.on('SIGINT', () => {
  console.log('📴 Получен SIGINT, завершаем работу...');
  pool.end();
  process.exit(0);
});