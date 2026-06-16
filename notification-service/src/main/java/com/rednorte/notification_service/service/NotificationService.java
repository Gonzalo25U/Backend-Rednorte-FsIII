package com.rednorte.notification_service.service;

import com.rednorte.notification_service.dto.AppointmentEvent;
import com.rednorte.notification_service.model.Notification;
import com.rednorte.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @RabbitListener(queues = "appointment.created")
    public void handleAppointmentCreated(AppointmentEvent event) {
        Notification n = new Notification();
        n.setRecipientRut(event.getDoctorRut());
        n.setMessage("Nueva cita asignada del paciente " + event.getPatientRut() + " (ID: " + event.getAppointmentId() + ")");
        n.setType("CITA_CREADA");
        repository.save(n);
    }

    @RabbitListener(queues = "appointment.approved")
    public void handleAppointmentApproved(AppointmentEvent event) {
        Notification n = new Notification();
        n.setRecipientRut(event.getPatientRut());
        n.setMessage("Tu cita (ID: " + event.getAppointmentId() + ") ha sido aprobada por el doctor.");
        n.setType("CITA_APROBADA");
        repository.save(n);
    }

    @RabbitListener(queues = "appointment.cancelled")
    public void handleAppointmentCancelled(AppointmentEvent event) {
        Notification n = new Notification();
        n.setRecipientRut(event.getDoctorRut());
        n.setMessage("La cita (ID: " + event.getAppointmentId() + ") del paciente " + event.getPatientRut() + " ha sido cancelada.");
        n.setType("CITA_CANCELADA");
        repository.save(n);
    }

    public List<Notification> getNotifications(String recipientRut) {
        return repository.findByRecipientRutOrderByCreatedAtDesc(recipientRut);
    }

    public void markAsRead(Long id) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        n.setRead(true);
        repository.save(n);
    }
}