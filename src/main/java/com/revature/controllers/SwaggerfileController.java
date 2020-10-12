package com.revature.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.models.Docutest;
import com.revature.models.Request;
import com.revature.models.SwaggerDocutest;
import com.revature.models.SwaggerSummary;
import com.revature.models.SwaggerUploadResponse;
import com.revature.services.JMeterService;
import com.revature.services.OASService;
import com.revature.services.SwaggerSummaryService;
import com.revature.templates.LoadTestConfig;
import com.revature.templates.SwaggerSummaryDTO;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for requests relating to spec file upload and results
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class SwaggerfileController {
    private static final Logger log = LogManager.getLogger(SwaggerfileController.class);

    @Autowired
    private JMeterService jms;
    @Autowired
    private SwaggerSummaryService swaggerSummaryService;
    @Autowired
    private OASService oasService;
    @Autowired
    private ObjectMapper mapper;

    @PostMapping("/upload")
    public ResponseEntity<SwaggerUploadResponse> uploadSwaggerFile(@RequestParam("file") MultipartFile file, @RequestParam("LoadTestConfig") String ltcString) throws IOException {
        InputStream jsonStream = file.getInputStream();
        
        JsonNode node = mapper.readTree(jsonStream);
        Swagger swag = new SwaggerParser().read(node);
        
        LoadTestConfig ltc = mapper.readValue(ltcString, LoadTestConfig.class);
        SwaggerUploadResponse swagResponse = swaggerSummaryService.uploadSwaggerfile(swag, ltc);
        
        String sanitizedFilename = file.getOriginalFilename();
        // log uploads
        if (sanitizedFilename != null) {
            sanitizedFilename = sanitizedFilename.replaceAll("[\n|\r|\t]", "_");
        } else {
            sanitizedFilename = "no_filename_found";
        }
        
        log.info("REQUEST FOR LOAD TEST RECEIVED FOR FILE: {} WITH SWAGGERSUMMARY ID: {}", 
                sanitizedFilename, swagResponse.getSwaggerSummaryId());
        
        List<Request> requests = oasService.getRequests(swag);
        Docutest docutest = new SwaggerDocutest(requests);
        
        log.info("RUNNING TESTS FOR SWAGGERSUMMARY ID: {}", swagResponse.getSwaggerSummaryId());
        Executors.newSingleThreadExecutor().execute(() -> 
            jms.loadTesting(docutest, ltc, swagResponse.getSwaggerSummaryId())
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
    
    @GetMapping("/swaggersummary")
    public ResponseEntity<List<SwaggerSummaryDTO>> getAllSwaggerSummaries() {
        List<SwaggerSummary> swaggerSummaries = swaggerSummaryService.getAllSwaggerSummaries();
        
        List<SwaggerSummaryDTO> swaggerSummaryDtos = swaggerSummaryService.convertSwaggerSummary(swaggerSummaries);
        
        return ResponseEntity.ok(swaggerSummaryDtos);
    }
    
}
