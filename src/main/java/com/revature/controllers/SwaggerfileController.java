package com.revature.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.models.ResultSummary;
import com.revature.models.SwaggerSummary;
import com.revature.models.SwaggerUploadResponse;
import com.revature.services.JMeterService;
import com.revature.services.ResultSummaryService;
import com.revature.services.SwaggerSummaryService;
import com.revature.templates.LoadTestConfig;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
public class SwaggerfileController {

    @Autowired
    private JMeterService jms;
    @Autowired
    private SwaggerSummaryService swaggerSummaryService;
    @Autowired
    private ResultSummaryService resultSummaryService;
    @Autowired
    private EntityManager entityManager;

    @PostMapping("/upload")
    public ResponseEntity<SwaggerUploadResponse> uploadSwaggerFile(@RequestParam("file") MultipartFile file, @RequestParam("LoadTestConfig") String ltcString) throws IOException {
        InputStream jsonStream = file.getInputStream();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonStream);
        Swagger swag = new SwaggerParser().read(node);
          
        LoadTestConfig ltc = mapper.readValue(ltcString, LoadTestConfig.class);
        SwaggerUploadResponse swagResponse = swaggerSummaryService.uploadSwaggerfile(swag, ltc);
          
        Executors.newSingleThreadExecutor().execute(() -> 
            jms.loadTesting(swag, ltc, swagResponse.getSwaggerSummaryId())
        );
            
        return ResponseEntity.ok(swagResponse);
    }

    @GetMapping("/swaggersummary/{id}")
    public ResponseEntity<SwaggerSummary> getSwaggerSummary(@PathVariable("id") int id) {

        SwaggerSummary s = swaggerSummaryService.getById(id);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(s);
    }

    // ----------------------- Troubleshooting/test/debug methods
    // ------------------------------
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
