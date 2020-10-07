package com.revature.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.docutest.DocutestApplication;

@SpringBootTest(classes = DocutestApplication.class)
@ExtendWith(SpringExtension.class)
public class SwaggerfileControllerTest {
    
    @Autowired
    private ObjectMapper om;
    
    @Test
    public void test() {
        System.out.println(om);
    }
    
}
