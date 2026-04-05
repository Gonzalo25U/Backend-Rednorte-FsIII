package com.rednorte.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rednorte.user_service.enums.UserRole;
import com.rednorte.user_service.model.User;
import com.rednorte.user_service.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository repository) {
        return args -> {
            if (!repository.existsByRole(UserRole.ADMIN)) {
                User admin = new User();
                admin.setRut("11111111-1");
                admin.setName("Admin");
                admin.setRole(UserRole.ADMIN);
                admin.setPassword("$2a$12$eRb5MbDUzVdkXrIXs1kqROGuFEiwrMMa/oy1w2qV9geJJ3XgwssJO"); // admin123
                admin.setActive(true);
                repository.save(admin);
                System.out.println("🔥 ADMIN creado: 11111111-1 / admin1234");
            }
        };
    }
}