package com.revature.docutest;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.protocol.http.control.Header;

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
    public static Swagger pathParam;
    public static Swagger todos_deployed;

    public static Swagger multi;
    public static Swagger petstore;
    public static Swagger yaml;

    public static List<Request> getReq;
    public static List<Request> postReq;
    public static List<Request> getReqMulti;

    // technically not JSON directly, but regex to account for auto id
    public static final String TODO_JSON_REGEX = "\\{" + "\"completed\" : \"false\","
            + "\"createdOn\" : \"autogeneratedstring\"," + "\"id\" : \".*\"," + "\"title\" : \"autogeneratedstring\""
            + "\\}";
    public static final String PET_JSON_REGEX = "\\{" + "\"id\" : \".*\"," + "\"category\" : \\{" + "\"id\" : \".*\","
            + "\"name\" : \"autogeneratedstring\"" + "\\}," + "\"name\" : \"autogeneratedstring\","
            + "\"photoUrls\": \\[\"autogeneratedstring\"\\]," + "\"tags\": \\[" + "\\{" + "\"id\":\".*\","
            + "\"name\":\"autogeneratedstring\"" + "\\}" + "\\]," + "\"status\" : \"autogeneratedstring\"" + "\\}";

    public static final String POST_OBJ_JSON_REGEX = "\\{" + "\"id\" : \".*\","
            + "\"field1\" : \"autogeneratedstring\"\\}";

    public static final String POST_OBJ_JSON = "{" + "\"id\" : \"1\"," + "\"field1\" : \"autogeneratedstring\","
            + "\"field2\" : [1]" + "}";

    public static String[] pathOrder;
    public static HttpMethod[] verbOrder;

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
        pathParam = new SwaggerParser().read("src/test/resources/path_param.json");
        todos_deployed = new SwaggerParser().read("src/test/resources/todos_deployed.json");
        
        initRequests();
        initRequestOrders();
    }

    private static void initRequests() {
        // for oasservice test
        // for get.json
        Endpoint endpoint = new Endpoint();
        endpoint.setBasePath("/");
        endpoint.setPath("/");
        endpoint.setBaseUrl("google.com");
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
        endpoint2.setBaseUrl("google.com");
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
        req.getHeaderParams().add(new Header("Content-Type", "application/json"));
        postReq = new ArrayList<>();
        postReq.add(req);
    }

    private static void initRequestOrders() {
        // Expected Order: todos -> todos/id -> todos/truncate
        // Verb Order: POST -> GET -> PUT, GET -> PATCH -> DELETE, DELETE
        pathOrder = new String[] { "/todos", "/todos", "/todos", "/todos/1", "/todos/1", "/todos/1",
                "/todos/truncate" };
        verbOrder = new HttpMethod[] { HttpMethod.POST, HttpMethod.GET, HttpMethod.PUT, HttpMethod.GET,
                HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.DELETE };
    }

}
