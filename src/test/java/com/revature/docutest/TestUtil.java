package com.revature.docutest;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class TestUtil {
    
    public static Swagger swag1;
    
    {
        initFields();
    }
    
    public static void initFields() {  
        swag1 = new SwaggerParser().read("src/test/resources/example.json");
    }

}
