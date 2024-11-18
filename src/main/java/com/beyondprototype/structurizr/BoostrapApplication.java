package com.beyondprototype.structurizr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BoostrapApplication implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication.run(BoostrapApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("CommandLineRunner runs ...");
    }
}
