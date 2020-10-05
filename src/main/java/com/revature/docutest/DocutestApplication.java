package com.revature.docutest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan("com.revature")
@EnableJpaRepositories("com.revature.repositories")
@EntityScan("com.revature.models")
@SpringBootApplication
public class DocutestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocutestApplication.class, args);
    }

}
