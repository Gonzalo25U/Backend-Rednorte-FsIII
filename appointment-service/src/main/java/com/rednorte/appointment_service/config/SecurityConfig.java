package com.rednorte.appointment_service.config;

import com.rednorte.appointment_service.security.JwtFilter;
import com.rednorte.appointment_service.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/appointments")
                    .hasAnyRole("PACIENTE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/appointments")
                    .hasAnyRole("ADMIN", "DOCTOR", "PACIENTE")
                .requestMatchers(HttpMethod.PUT, "/appointments/*/cancel")
                    .hasAnyRole("PACIENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/appointments/*/status")
                    .hasAnyRole("DOCTOR", "ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}