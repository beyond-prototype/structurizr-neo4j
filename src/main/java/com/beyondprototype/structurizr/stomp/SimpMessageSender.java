package com.beyondprototype.structurizr.stomp;

import com.beyondprototype.structurizr.MessageSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@AllArgsConstructor
public class SimpMessageSender implements MessageSender {

    private SimpMessagingTemplate simpMessagingTemplate;
    private String sessionId;
    private String user;
    private String destination;

    @Override
    public void sendToUser(Object payload) {
        //Assert.requireNonEmpty(user);
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        //send the message to the specific session only (broadcast = false)
        //rather than all sessions associated with the user
        if (sessionId != null) {
            accessor.setSessionId(sessionId);
        }
        accessor.setLeaveMutable(true);
        MessageHeaders headers = accessor.getMessageHeaders();

        simpMessagingTemplate.convertAndSendToUser(user, destination, payload, headers);
    }
}
