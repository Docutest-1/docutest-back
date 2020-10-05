package com.revature.docutest;

import java.util.ArrayList;
import java.util.List;

import com.revature.models.Endpoint;
import com.revature.models.Request;

import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

// container class for test data
public class TestUtil {

    public static Swagger todos;
    public static Swagger blank;
    public static Swagger malformed;
    public static Swagger get;
    public static Swagger post;
    
    public static Swagger multi;
    public static Swagger petstore;
    public static Swagger yaml;
    
    public static List<Request> getReq;
    public static List<Request> postReq;
    public static List<Request> getReqMulti;
    
    public static final String TODO_JSON = "{" +
            "\"completed\" : \"false\"," +
            "\"createdOn\" : \"\"," +
            "\"id\" : \"0\"," +
            "\"title\" : \"\"," +
            "}";
    public static final String PET_JSON = "{" +
            "\"id\" : \"0\"," +
            "\"category\" : {" +
                "\"id\" : \"0\"," +
                "\"name\" : \"\"," +
                "}," +
            "\"name\" : \"\"," +
            "\"photoUrls\": []," +
            "\"tags\": [" + 
                "{" +
                    "\"id\":\"0\"," +
                    "\"name\":\"\"," +
                "}" +
            "]," + 
            "\"status\" : \"\"," +
            "}";
    
    public static final String POST_OBJ_JSON = "{" +
            "\"id\" : \"0\"," +
            "\"field1\" : \"\",}";

    static {
        initFields();
    }

    public static void initFields() {
        // create swagger
        todos = new SwaggerParser().read("src/test/resources/example.json");
        blank = new SwaggerParser().read("src/test/resources/blank.json");
        malformed = new SwaggerParser().read("src/test/resources/malformed.json");
        get = new SwaggerParser().read("src/test/resources/get.json");
        multi = new SwaggerParser().read("src/test/resources/multi.json");
        petstore = new SwaggerParser().read("src/test/resources/petstore.json");
        yaml = new SwaggerParser().read("src/test/resources/petstore.yaml");
        post = new SwaggerParser().read("src/test/resources/post.json");
        
        initRequests();
    }
    
    private static void initRequests() {
        // for oasservice test
        // for get.json
        Endpoint endpoint = new Endpoint();
        endpoint.setBasePath("/");
        endpoint.setPath("/");
        endpoint.setBaseUrl("blazedemo.com");
        endpoint.setPort(80);
        Request req = new Request();
        req.setEndpoint(endpoint);
        req.setVerb(HttpMethod.GET);
        getReq = new ArrayList<>();
        getReq.add(req);
        
        getReqMulti = new ArrayList<>(getReq);
        // add second endpoint
        Endpoint endpoint2 = new Endpoint();
        endpoint2.setBasePath("/");
        endpoint2.setPath("/login");
        endpoint2.setBaseUrl("blazedemo.com");
        endpoint2.setPort(80);
        Request req2 = new Request();
        req2.setEndpoint(endpoint2);
        req2.setVerb(HttpMethod.GET);
        getReqMulti.add(req2);
                
        // same endpoint
        req = new Request();
        req.setEndpoint(endpoint);
        req.setVerb(HttpMethod.POST);
        req.setBody(POST_OBJ_JSON);
        postReq = new ArrayList<>();
        postReq.add(req);
    }

}
