import { Pool } from 'pg';
import dotenv from 'dotenv';

dotenv.config();

async function testConnection() {
  console.log('🔄 Тестируем подключение к PostgreSQL...');
  console.log('Настройки из .env:');
  console.log('- Хост:', process.env.DB_HOST);
  console.log('- Порт:', process.env.DB_PORT);
  console.log('- База:', process.env.DB_NAME);
  console.log('- Пользователь:', process.env.DB_USER);
  console.log('- Пароль:', process.env.DB_PASSWORD ? '***' : 'не указан');

  const pool = new Pool({
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT),
    database: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
  });

  try {
    const client = await pool.connect();
    console.log('\n✅ УСПЕХ: Подключение к PostgreSQL установлено!');
    
    const result = await client.query('SELECT NOW() as current_time');
    console.log('🕒 Время на сервере БД:', result.rows[0].current_time);
    
    client.release();
    await pool.end();
    
    return true;
  } catch (error: any) {
    console.error('\n❌ ОШИБКА подключения к БД:');
    console.error('Сообщение:', error.message);
    console.error('\nПроверьте:');
    console.log('1. Запущен ли PostgreSQL? (net start postgresql-x64-18)');
    console.log('2. Верный ли пароль в .env?');
    console.log('3. Существует ли база cinema_db?');
    return false;
  }
}

testConnection();