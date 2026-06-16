package com.rednorte.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "appointment.exchange";
    public static final String QUEUE_APPOINTMENT_CREATED = "appointment.created";
    public static final String QUEUE_APPOINTMENT_APPROVED = "appointment.approved";
    public static final String QUEUE_APPOINTMENT_CANCELLED = "appointment.cancelled";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue queueCreated() {
        return new Queue(QUEUE_APPOINTMENT_CREATED, true);
    }

    @Bean
    public Queue queueApproved() {
        return new Queue(QUEUE_APPOINTMENT_APPROVED, true);
    }

    @Bean
    public Queue queueCancelled() {
        return new Queue(QUEUE_APPOINTMENT_CANCELLED, true);
    }

    @Bean
    public Binding bindingCreated() {
        return BindingBuilder.bind(queueCreated()).to(exchange()).with("appointment.created");
    }

    @Bean
    public Binding bindingApproved() {
        return BindingBuilder.bind(queueApproved()).to(exchange()).with("appointment.approved");
    }

    @Bean
    public Binding bindingCancelled() {
        return BindingBuilder.bind(queueCancelled()).to(exchange()).with("appointment.cancelled");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}