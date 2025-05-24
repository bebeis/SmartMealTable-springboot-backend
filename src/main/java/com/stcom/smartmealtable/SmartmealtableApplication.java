package com.stcom.smartmealtable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SmartmealtableApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartmealtableApplication.class, args);
    }

}
