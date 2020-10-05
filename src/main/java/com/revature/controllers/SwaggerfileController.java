package com.revature.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.revature.models.ResultSummary;
import com.revature.models.SwaggerSummary;
import com.revature.services.JMeterService;
import com.revature.services.ResultSummaryService;
import com.revature.services.SwaggerSummaryService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<Void> uploadSwaggerFile(@RequestParam("file") MultipartFile file) throws IOException, URISyntaxException {
        // TESTING PURPOSES
        SwaggerSummary s = swaggerSummaryService.insert();
        
        swaggerSummaryService.update(s);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                test(s.getId());
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/get")
    public ResponseEntity<Void> getSwaggerSummary(@RequestParam int id) throws IOException, URISyntaxException {
        
        SwaggerSummary s = swaggerSummaryService.getById(id);
        if (s == null) {
            return ResponseEntity.notFound().build();
        }
        Set<ResultSummary> rs = s.getResultsummaries();
        
        for (ResultSummary r : rs) {
            System.out.println(r);
        }
        
        return ResponseEntity.ok().build();
    }
    
    private void test(int swaggerSummaryId) throws URISyntaxException {
        
        SwaggerSummary ss1;
        
        ResultSummary rs = new ResultSummary(new URI("/test"), "POST", 90, 23, 78, 130, 167, 10, 90, 20, "s3.aws.com/dfdfdfs4.csv");
        ResultSummary rs2 = new ResultSummary(new URI("/test"), "GET", 100, 25, 80, 126, 175, 10, 90, 20, "s3.aws.com/dfdfdfs5.csv");
        List<ResultSummary> resultSummaries = new ArrayList<>();
        resultSummaries.add(rs);
        resultSummaries.add(rs2);
        
        for (ResultSummary r : resultSummaries) {
            ss1 = swaggerSummaryService.getById(swaggerSummaryId);
            
            System.out.println("PROCESSING");
            for (int i = 0; i < 1000000000; i++) {}
            System.out.println("DONE");
            
            ss1.getResultsummaries().add(r);
            swaggerSummaryService.update(ss1);
        }
        
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
