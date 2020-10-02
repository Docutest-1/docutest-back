package com.revature.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.services.JMeterServices;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;



@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class SwaggerfileController {

    @Autowired
    private JMeterServices jms;
    
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadSwaggerFile(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("Filename: " + file.getOriginalFilename() + "\n");
        
        InputStream jsonStream = file.getInputStream();
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStream);
        
        Swagger swag = new SwaggerParser().read(node);
        
        this.jms.createHTTPSampler(swag);
        
        return ResponseEntity.ok().build();
    }
    
    // ----------------------- Troubleshooting/test/debug methods ------------------------------
    // TODO remove when no longer needed
    private static void printOperations(Swagger swag, Map<HttpMethod, Operation> operationMap) {
        for (Map.Entry<HttpMethod, Operation> op : operationMap.entrySet()) {
            System.out.println("HTTP Method: " + op.getKey() + " - " + op.getValue().getOperationId());
            
            System.out.println("Parameters: ");
            printParameters(swag, op.getValue().getParameters());
        }
    }
    
    private static void printParameters(Swagger swag, List<Parameter> parameters) {
        for (Parameter p : parameters) {
            System.out.println("- " + p.getName() + " (in " + p.getIn() + ")");
            if (p instanceof PathParameter) {
                System.out.println("    Type: " + ((PathParameter) p).getType());
            } else if (p instanceof HeaderParameter) {
                System.out.println("    Type: " + ((HeaderParameter) p).getType());
                System.out.println("    Default value: " + ((HeaderParameter) p).getDefaultValue());
            }
            
            if (p instanceof BodyParameter) {
                BodyParameter bp = (BodyParameter) p;
                Model schema = bp.getSchema();
                
                String reference = schema.getReference();
                System.out.println("    Schema full reference: " + reference);
                
                String simpleRef = reference.replaceFirst(".*/", "");
                System.out.println("    Simple ref: " + simpleRef);
                
                Map<String, Model> definitions = swag.getDefinitions();
                Model definition = definitions.get(simpleRef);
                Map<String, Property> bodyProperties = definition.getProperties();
                
                for (Map.Entry<String, Property> entry : bodyProperties.entrySet()) {
                    System.out.println("    Property: ");
                    System.out.println("        " + "body property: " + entry.getKey());
                    System.out.println("        " + "body type: " + entry.getValue().getType());
                }
                
            }
            
        }
        System.out.println();
    }
    // --------------------------------------------------------------------------------------
    
}
