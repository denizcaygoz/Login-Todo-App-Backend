package com.login_todo_app_backend.security.jwt;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
//Loads environment variables from a .env file.
@Configuration
public class EnvConfig {
    @Bean
    public Dotenv dotenv() {
        Dotenv dotenv = Dotenv.configure().directory(Paths.get(System.getProperty("user.dir")).toString()).ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        return dotenv;
    }
    @Bean
    public String jwtSecret(Environment environment) {
        return environment.getProperty("jwt.secret", "default-secret-key");
    }
    @Bean
    public long jwtExpirationMs(Environment environment) {
        return Long.parseLong(environment.getProperty("jwt.expirationMs", "86400000"));
    }
}
