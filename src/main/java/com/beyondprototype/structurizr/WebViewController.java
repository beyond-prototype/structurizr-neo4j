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

    //@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String indexView(ModelMap modelMap, Principal user, HttpSession session) {

        log.info("indexView User :" + (user == null ? "Unknown" : user.getName()));
        log.info("indexView HttpSession :" + (session == null ? "Unknown" : session.getId()));

        //TODO: user authentication
        //modelMap.put("user", user);
        modelMap.put("user", new Principal() {
            @Override
            public String getName() {
                if (session == null) {
                    return "Unknown";
                }
                return session.getId();
            }
        });

        return "index";
    }

    //http://localhost:8090/stomp
    @GetMapping(value = "/stomp", produces = MediaType.TEXT_HTML_VALUE)
    public String indexViewStomp(ModelMap modelMap, Principal user, HttpSession session) {
        modelMap.put("client", "stomp");
        //TODO: externalize serverUrl to configuration
        modelMap.put("serverUrl", "ws://localhost:8090/websocket");
        return indexView(modelMap, user, session);
    }

    //http://localhost:8090/
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String indexViewSse1(ModelMap modelMap, Principal user, HttpSession session) {
        return indexViewSse(modelMap, user, session);
    }

    //http://localhost:8090/sse
    @GetMapping(value = "/sse", produces = MediaType.TEXT_HTML_VALUE)
    public String indexViewSse(ModelMap modelMap, Principal user, HttpSession session) {
        modelMap.put("client", "sse");
        //TODO: externalize serverUrl to configuration
        modelMap.put("serverUrl", "http://localhost:8090/answer/stream");
        return indexView(modelMap, user, session);
    }
}
