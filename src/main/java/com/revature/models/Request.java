package com.revature.models;

import java.util.HashMap;
import java.util.Map;

import io.swagger.models.HttpMethod;
import lombok.Data;

@Data
public class Request {
    
    private String body = "";
    private Map<String, String> bodyParams = new HashMap<>();
    private Map<String, String> pathParams = new HashMap<>();
    private Endpoint endpoint;
    private HttpMethod verb;

}
