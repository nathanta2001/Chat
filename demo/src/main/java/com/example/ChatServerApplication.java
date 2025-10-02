package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.example.Chat.server")
@EnableJpaRepositories("com.example.Chat.server")
@EntityScan("com.example.Chat.common")
public class ChatServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatServerApplication.class, args);
    }
}