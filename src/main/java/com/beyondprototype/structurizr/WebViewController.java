package com.beyondprototype.structurizr;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Slf4j
@Controller
public class WebViewController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String indexView(ModelMap modelMap, Principal user, HttpSession session) {

        log.info("indexView User :" + (user == null ? "Unknown" : user.getName()));
        log.info("indexView HttpSession :" + (session == null ? "Unknown" : session.getId()));
        //TODO: externalize brokerURL to configuration
        modelMap.put("stompBrokerURL", "ws://localhost:8090/gs-guide-websocket");
        //TODO: user authentication
        //modelMap.put("user", user);
        modelMap.put("user", new Principal() {
            @Override
            public String getName() {
                if(session == null) {
                    return "Unknown";
                }
                return session.getId();
            }
        });

        return "index";
    }
}
