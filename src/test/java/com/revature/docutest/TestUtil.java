package com.revature.docutest;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class TestUtil {
    
    public static Swagger todos;
    public static Swagger blank;
    public static Swagger malformed;
    public static Swagger get;
    public static Swagger multi;
    
    {
        initFields();
    }
    
    public static void initFields() {  
        todos = new SwaggerParser().read("src/test/resources/example.json");
        blank = new SwaggerParser().read("src/test/resources/blank.json");
        malformed = new SwaggerParser().read("src/test/resources/malformed.json");
        get = new SwaggerParser().read("src/test/resources/get.json");
        multi = new SwaggerParser().read("src/test/resources/multi.json");
    }

}
