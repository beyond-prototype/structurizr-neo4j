package com.beyondprototype.structurizr.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class SseController {

    private AtomicInteger counter = new AtomicInteger();

//    @RequestMapping(value="/stream",method= RequestMethod.GET)
//    public ResponseBodyEmitter handle() {
//        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
//        // Pass the emitter to another component.
//        return emitter;
//    }

//    @GetMapping(value = "/sse/text", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Object text(){
//        log.info("**** /sse/text is triggered ...");
//        return Map.of("status", "Complete");
//    }

    @GetMapping(value = "/answer/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(value = "query") String query) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        log.info("/answer/stream is triggered... query: %s".formatted(query));
        new Thread(() -> {
            try {
                while (true) {
                    String message = "sse message-%s : %s".formatted(counter.incrementAndGet(),query);
//                    emitter.send(SseEmitter.event().data(Map.of("answer", message))
//                            .name("sse-event")
//                            .id("x0001")
//                            .build());

                    emitter.send(Map.of("answer", message));

                    Thread.sleep(5000);
                }
            } catch (AsyncRequestNotUsableException e) {
                //Response not usable after async request completion.
            } catch (Exception e) {
                //java.io.I0Exception: Broken pipe
                log.error("error", e);
            }
        }).start();

        emitter.onCompletion(() -> {
            log.info("*** SseEmitter.complete ***");
        });

        emitter.onError((t) -> {
            log.info("*** SseEmitter error ***", t);
        });

        return emitter;
    }

//    @GetMapping(value ="/sse/dsl", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter dsl(@RequestParam(value = "query") String query, @RequestParam(value = "workspace") String workspace) {
//        log.info("query = %s".formatted(query));
//        log.info("workspace = %s".formatted(workspace));
//        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
//        new Thread(() -> {
//            try {
//            //qaChain.emitter(emitter).invoke(query);
//            } catch (Exception e) {
//                log.error("error", e);
//            }
//        }).start();
//        emitter.onCompletion(() -> {
//            log.info("*** SseEmitter complete ***");
//        });
//        emitter.onError((t) -> {
//            log.info("*** SseEmitter error ***", t);
//        });
//        return emitter;
//    }
}

