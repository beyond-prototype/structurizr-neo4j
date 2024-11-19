package com.beyondprototype.structurizr.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {
    private Map<String, SseEmitter> emitters = new ConcurrentHashMap();

    public void send(String sessionid, Object payload) {
        try {
            get(sessionid).send(payload);
        } catch (IllegalStateException e) {
            //ResponseBodyEmitter has already completed
            log.error("SseEmitter to send", e);
        } catch (Exception e) {
            log.error("SseEmitter to send", e);
        }
    }

    public SseEmitter get(String sessionid) {
        SseEmitter emitter = emitters.get(sessionid);
        if (emitter == null) {
            emitter = create();
            emitters.put(sessionid, emitter);
        }
        return emitter;
    }

    private SseEmitter create() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> {
            log.info("*** SseEmitter complete ***");
        });

        emitter.onError((t) -> {
            log.info("*** SseEmitter error ***", t);
        });
        return emitter;
    }
}