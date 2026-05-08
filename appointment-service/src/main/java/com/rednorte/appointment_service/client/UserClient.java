package com.rednorte.appointment_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient webClient;

    public boolean existsUser(String rut) {
        try {
            webClient.get()
                    .uri("http://user-service:8081/users/rut/" + rut)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con user-service");
        }
    }
}