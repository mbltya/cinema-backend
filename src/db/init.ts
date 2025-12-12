import { Pool } from 'pg';
import dotenv from 'dotenv';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const pool = new Pool({
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT),
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
});

export async function initDatabase() {
  try {
    // Читаем SQL файл
    const sqlPath = path.join(__dirname, 'schema.sql');
    const sql = fs.readFileSync(sqlPath, 'utf8');
    
    // Выполняем SQL
    await pool.query(sql);
    console.log('✅ Таблицы успешно созданы');
    
    // Добавляем тестовые данные
    await seedTestData();
    
  } catch (error) {
    console.error('❌ Ошибка при создании таблиц:', error);
  } finally {
    await pool.end();
  }
}

async function seedTestData() {
  try {
    // Тестовый фильм
    await pool.query(`
      INSERT INTO movies (title, description, duration_minutes, release_year, genre, poster_url)
      VALUES ('Интерстеллар', 'Фантастика о путешествии сквозь червоточину', 169, 2014, 'Фантастика', 'https://example.com/poster1.jpg')
      ON CONFLICT DO NOTHING;
    `);
    
    console.log('✅ Тестовые данные добавлены');
  } catch (error) {
    console.log('ℹ️  Тестовые данные уже существуют');
  }
}

// Запуск если файл вызван напрямую
const isMainModule = import.meta.url === `file://${process.argv[1]}`;
if (isMainModule) {
  initDatabase();
}