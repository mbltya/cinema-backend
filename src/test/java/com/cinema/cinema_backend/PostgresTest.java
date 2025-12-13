// Создайте файл: src/test/java/com/cinema/cinema_backend/PostgresConnectionTest.java
package com.cinema.cinema_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PostgresTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testPostgresConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(2)).isTrue();
            System.out.println("✅ Подключение к PostgreSQL успешно!");

            // Проверим версию PostgreSQL
            String version = connection.getMetaData().getDatabaseProductVersion();
            System.out.println("Версия PostgreSQL: " + version);
        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения к PostgreSQL: " + e.getMessage());
            throw e;
        }
    }
}