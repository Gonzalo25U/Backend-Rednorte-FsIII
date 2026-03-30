package com.rednorte.appointment_service.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

@Service
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public boolean existsUser(String rut) {
        try {
            String url = "http://localhost:8081/users/" + rut;
            restTemplate.getForObject(url, Object.class);
            return true;

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false; // 👈 usuario no existe (OK)
            }

            // otro error HTTP
            throw new RuntimeException("Error en user-service: " + e.getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException("Error conectando con user-service");
        }
    }
}