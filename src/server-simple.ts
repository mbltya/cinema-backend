import express from 'express';
import cors from 'cors';

const app = express();
const PORT = 5000;

app.use(cors({
  origin: 'http://localhost:3000',
  credentials: true
}));
app.use(express.json());

app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'Cinema API работает (без БД)',
    time: new Date().toISOString()
  });
});

app.post('/api/auth/register', (req, res) => {
  res.json({ 
    success: true, 
    message: 'Регистрация (тест без БД)',
    user: req.body 
  });
});

app.get('/api/movies', (req, res) => {
  res.json({
    success: true,
    movies: [
      { id: 1, title: 'Тестовый фильм 1', duration: 120 },
      { id: 2, title: 'Тестовый фильм 2', duration: 90 }
    ]
  });
});

app.listen(PORT, () => {
  console.log(`🎬 Cinema Backend (упрощенный) запущен на http://localhost:${PORT}`);
  console.log(`📡 API: http://localhost:${PORT}/api`);
  console.log(`🩺 Проверка: http://localhost:${PORT}/api/health`);
});

process.on('SIGINT', () => {
  console.log('Завершение работы...');
  process.exit(0);
});