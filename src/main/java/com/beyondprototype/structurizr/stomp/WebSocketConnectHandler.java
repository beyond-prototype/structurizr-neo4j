package com.beyondprototype.structurizr.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
public class WebSocketConnectHandler<S> implements ApplicationListener<SessionConnectEvent> {
    private SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketConnectHandler(SimpMessagingTemplate simpMessagingTemplate) {
        super();
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        log.info("*** SessionConnectEvent ***");
        MessageHeaders headers = event.getMessage().getHeaders();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        Map<String, List<String>> multiValueMap = headers.get(StompHeaderAccessor.NATIVE_HEADERS, Map.class);
//        multiValueMap.entrySet().stream().forEach(head ->{
//            log.info(head.getKey() +":"+ head.getValue());
//        });

        Principal user = StompHeaderAccessor.getUser(headers);
        if (user == null) {
            return;
        }
        String sessionId = SimpMessageHeaderAccessor.getSessionId(headers);
        log.info("*** SessionConnectEvent *** user;%s sessionId:%s".formatted(user.getName(), sessionId));
        //TODO: save user session to repository
    }
}
