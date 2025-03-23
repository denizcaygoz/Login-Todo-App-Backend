package com.login_todo_app_backend.security.jwt;

import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class EnvConfig {

    static {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("JWT_EXPIRATION_MS", dotenv.get("JWT_EXPIRATION_MS"));
        System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
        System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
        System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
        System.setProperty("DB_USER", dotenv.get("DB_USER"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("POSTGRES_DB", dotenv.get("POSTGRES_DB"));
        System.setProperty("DB_PASSWORD_2", dotenv.get("DB_PASSWORD_2"));
    }

    public static Dotenv getDotenv() {
        return Dotenv.load();
    }
}