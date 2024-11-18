package com.beyondprototype.structurizr.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
public class WebSocketDisconnectHandler<S> implements ApplicationListener<SessionDisconnectEvent> {
    private SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketDisconnectHandler(SimpMessagingTemplate simpMessagingTemplate) {
        super();
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        log.info("*** SessionDisconnectEvent ***");
        String sessionId = event.getSessionId();
        if(sessionId == null) {
            return;
        }
        log.info("SessionDisconnectEvent session : %s".formatted(sessionId));
        //TODO: remove user session from repository
    }
}
