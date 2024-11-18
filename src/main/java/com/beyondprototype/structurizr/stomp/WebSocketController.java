package com.beyondprototype.structurizr.stomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
public class WebSocketController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greeting(String message) throws Exception {
        Thread.sleep(1000); //simulate delay
        return new String("Hello %s".formatted(HtmlUtils.htmlEscape(message)));
    }

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/saac")
    @SendToUser(value="/queue/saac", broadcast = false)
    public Object saac(@Payload Object input, Principal user, @Header("simpSessionId") String sessionId) {

        SimpMessageSender sender = new SimpMessageSender(template, sessionId, user.getName(),"/queue/saac");

        sender.sendToUser(Map.of("status","Only for session :%s".formatted(sessionId)));

        //TODO: Add Streaming QA Chain
        //streamingQaChain.invoke(input.getQuery(), sender);

        return Map.of("status", "Processing");
    }
}
