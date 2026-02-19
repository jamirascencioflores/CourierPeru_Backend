package com.courierperu.notifications.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue emailQueue() {
        return new Queue("cola.correos", true);
    }
}