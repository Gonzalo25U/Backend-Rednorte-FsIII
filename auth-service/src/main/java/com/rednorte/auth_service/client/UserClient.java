package com.rednorte.auth_service.client;

import com.rednorte.auth_service.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient webClient;

    public UserResponse getUserByRut(String rut) {
        try {
            return webClient.get()
                    .uri("http://user-service:8081/users/rut/" + rut)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .block(); // bloqueante para mantener compatibilidad con el servicio servlet
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            System.out.println("💥 Error conectando con user-service: " + e.getMessage());
            throw new RuntimeException("Error conectando con user-service");
        }
    }
}