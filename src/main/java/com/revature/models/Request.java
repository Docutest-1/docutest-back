package com.revature.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.models.HttpMethod;
import lombok.Data;
import org.apache.jmeter.protocol.http.control.Header;

@Data
public class Request {
    
    private String body = "";
    private List<Header> headerParams = new ArrayList<>();
    private Map<String, String> bodyParams = new HashMap<>(); // currently unused, mostly for future development
    private Map<String, String> pathParams = new HashMap<>();
    private Endpoint endpoint;
    private HttpMethod verb;

}
