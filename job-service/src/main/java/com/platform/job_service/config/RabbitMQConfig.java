package com.platform.job_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.resume.name:resume_processing_queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.resume.name:resume_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.resume.key:resume_routing_key}")
    private String routingKey;

    @Bean
    public Queue resumeQueue() {
        // 'true' makes the queue durable (survives broker restarts)
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange resumeExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue resumeQueue, TopicExchange resumeExchange) {
        return BindingBuilder.bind(resumeQueue).to(resumeExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}