package com.beyondprototype.structurizr.stomp;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Configuration
public class WebSocketConnectHandlerConfig<S extends ApplicationEvent> {

    @Bean
    public WebSocketConnectHandler<S> webSocketConnectHandler(SimpMessagingTemplate simpMessagingTemplate) {
        return new WebSocketConnectHandler<>(simpMessagingTemplate);
    }

    @Bean
    public WebSocketDisconnectHandler<S> webSocketDisconnectHandler(SimpMessagingTemplate simpMessagingTemplate) {
        return new WebSocketDisconnectHandler<>(simpMessagingTemplate);
    }
}
