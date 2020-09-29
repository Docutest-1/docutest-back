package com.revature.docutest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.revature")
@SpringBootApplication
public class DocutestApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocutestApplication.class, args);
    }

}
