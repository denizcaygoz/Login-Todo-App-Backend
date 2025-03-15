package com.login_todo_app_backend.security.jwt;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    private final Dotenv dotenv;

    public EnvConfig() {
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public Dotenv dotenv() {
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        return dotenv;
    }

    // Provide JWT Secret
    @Bean
    public String jwtSecret() {
        return dotenv.get("JWT_SECRET", "default-secret-key");
    }

    // Provide JWT Expiration Time
    @Bean
    public long jwtExpirationMs() {
        return Long.parseLong(dotenv.get("JWT_EXPIRATION_MS", "86400000")); // Default 24 hours
    }
}
