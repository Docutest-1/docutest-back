package com.revature.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.models.SwaggerSummary;
import com.revature.models.SwaggerUploadResponse;
import com.revature.services.JMeterService;
import com.revature.services.ResultSummaryService;
import com.revature.services.SwaggerSummaryService;
import com.revature.templates.LoadTestConfig;
import io.swagger.models.Swagger;
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
    
}
