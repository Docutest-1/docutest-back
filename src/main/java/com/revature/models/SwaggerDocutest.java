package com.revature.models;

import java.util.List;

import lombok.Data;

@Data
public class SwaggerDocutest implements Docutest {
    int id; // id of swaggersummary object
    private List<Request> requests;
    
    public SwaggerDocutest(List<Request> requests) {
        this.requests = requests;
    }
}
