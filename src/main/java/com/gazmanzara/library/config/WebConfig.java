package com.gazmanzara.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Only allow CORS for our API endpoints
                .allowedOrigins(
                        "https://library-management-alpha-self.vercel.app/",
                        "http://localhost:3000", // Spring Boot default port
                        "http://localhost:8080", // Spring Boot default port
                        "http://localhost:5173" // Vite default port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")
                .allowCredentials(true)
                .maxAge(3600); // 1 hour
    }
}