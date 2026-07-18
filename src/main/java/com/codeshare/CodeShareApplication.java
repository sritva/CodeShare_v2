package com.codeshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeShareApplication.class, args);
    }
}
