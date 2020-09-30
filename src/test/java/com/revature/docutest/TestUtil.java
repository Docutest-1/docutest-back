package com.revature.docutest;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class TestUtil {
    
    public static Swagger todos;
    public static Swagger blank;
    public static Swagger malformed;
    
    public static Swagger get;
    public static Swagger post;
    public static Swagger delete;
    
    {
        initFields();
    }
    
    public static void initFields() {  
        todos = new SwaggerParser().read("src/test/resources/example.json");
        blank = new SwaggerParser().read("src/test/resources/blank.json");
        malformed = new SwaggerParser().read("src/test/resources/malformed.json");
        
        // these are based off a specific person's project 1
        // might not be useful
        get = new SwaggerParser().read("src/test/resources/get.json");
        post = new SwaggerParser().read("src/test/resources/post.json");
        delete = new SwaggerParser().read("src/test/resources/delete.json");
    }

}
