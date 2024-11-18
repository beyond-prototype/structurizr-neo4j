package com.beyondprototype.structurizr.stomp;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic/","/queue/");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setPreservePublishOrder(true);
        //registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket")
                .setAllowedOrigins("*")
//                .setHandshakeHandler(new DefaultHandshakeHandler(){
//                    @Override
//                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//                        if (request instanceof ServletServerHttpRequest){
//                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
//                            HttpSession session = servletRequest.getServletRequest().getSession();
//                            log.info("HttpSession session : %s".formatted(session.getId()));
//
//                            return new Principal() {
//                                @Override
//                                public String getName() {
//                                    return session.getId();
//                                }
//                            };
//                        }
//
//                        return null;
//                    }
//                })
        ;

        //registry.setPreserveReceiveOrder(true);
    }

    //TODO: user authentication
    //@Autowired
    //private AuthChannelInterceptorAdapter authChannelInterceptorAdapter;
    //@Override
    //public void configureClientInboundChannel(ChannelRegistration registration) {
    //    registration.interceptors(authChannelInterceptorAdapter);
    //}

    @Configuration
    @EnableScheduling
    public class StompScheduler {
        @Autowired
        private SimpMessagingTemplate template;

        @Scheduled(fixedRate = 5000)
        public void broadcast(){
            //log.info("send greeting from server");
            template.convertAndSend("/topic/greetings",Map.of("content","Hello from server"));
        }
    }
}
