package com.cinema.cinema_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.cinema.cinema_backend")
public class CinemaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaBackendApplication.class, args);
    }
}
