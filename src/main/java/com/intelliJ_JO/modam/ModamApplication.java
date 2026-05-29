package com.intelliJ_JO.modam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class ModamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModamApplication.class, args);
    }
}
