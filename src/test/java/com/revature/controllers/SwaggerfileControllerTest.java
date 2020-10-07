package com.revature.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.docutest.DocutestApplication;
import com.revature.models.SwaggerSummary;
import com.revature.models.SwaggerUploadResponse;
import com.revature.services.JMeterService;
import com.revature.services.OASService;
import com.revature.services.SwaggerSummaryService;
import com.revature.templates.LoadTestConfig;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

@SpringBootTest(classes = DocutestApplication.class)
@ExtendWith(SpringExtension.class)
class SwaggerfileControllerTest {
    
    @Autowired
    @InjectMocks
    private SwaggerfileController swaggerfileController;
    
    @Mock
    private SwaggerSummaryService swaggerSummaryService;
    
    @Mock
    private OASService oasService;
    
    @Mock
    private JMeterService jms;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(swaggerfileController).build();
    }
    
    @Test
    void testUploadSwaggerFile() throws Exception {
        Path path = Paths.get("src/test/resources/get.json");
        String name = "file";
        String originalFileName = "get.json";
        String contentType = "application/json";
        byte[] content = null;

        content = Files.readAllBytes(path);

        MockMultipartFile result = new MockMultipartFile(name,
                             originalFileName, contentType, content);
        
        SwaggerUploadResponse sur = new SwaggerUploadResponse();
        sur.setEta(0);
        sur.setResultRef("Docutest/swaggersummary/1");
        sur.setSwaggerSummaryId(1);
        
        String expectedJson = new ObjectMapper().writeValueAsString(sur);
        
        LoadTestConfig ltc = new LoadTestConfig();
        ltc.setDuration(0);
        ltc.setFollowRedirects(true);
        ltc.setLoops(100);
        ltc.setRampUp(2);
        ltc.setTestPlanName("test");
        ltc.setThreads(10);
        
        Swagger swagger = new SwaggerParser().read("src/test/resources/get.json");
        
        when(swaggerSummaryService.uploadSwaggerfile(swagger, ltc)).thenReturn(sur);
        
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/upload")
                .file(result)
                .param("LoadTestConfig", 
                        "{ \"testPlanName\" : \"test\", \"loops\" : 100, \"duration\" : 0, \"threads\" : 10, \"rampUp\" : 2, \"followRedirects\" : true }"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(expectedJson));
    }
    
    @Test
    void getSwaggerSummary() throws Exception {
        
        SwaggerSummary swaggerSummary = new SwaggerSummary();
        swaggerSummary.setDuration(10);
        swaggerSummary.setFollowRedirects(true);
        swaggerSummary.setId(1);
        swaggerSummary.setLoops(-1);
        swaggerSummary.setRampUp(2);
        swaggerSummary.setResultsummaries(Sets.newSet());
        swaggerSummary.setTestPlanName("test");
        swaggerSummary.setThreads(10);
        
        String jsonContent = new ObjectMapper().writeValueAsString(swaggerSummary);
        when(swaggerSummaryService.getById(Matchers.eq(1))).thenReturn(swaggerSummary);
        
        this.mockMvc.perform(MockMvcRequestBuilders.get("/swaggersummary/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(jsonContent));
    }
    
    @Test
    void getSwaggerSummary_nullSwaggerSummary() throws Exception {
        
        SwaggerSummary swaggerSummary = new SwaggerSummary();
        swaggerSummary.setDuration(10);
        swaggerSummary.setFollowRedirects(true);
        swaggerSummary.setId(1);
        swaggerSummary.setLoops(-1);
        swaggerSummary.setRampUp(2);
        swaggerSummary.setResultsummaries(Sets.newSet());
        swaggerSummary.setTestPlanName("test");
        swaggerSummary.setThreads(10);
        
        String jsonContent = new ObjectMapper().writeValueAsString(swaggerSummary);
        when(swaggerSummaryService.getById(Matchers.eq(1))).thenReturn(null);
        
        this.mockMvc.perform(MockMvcRequestBuilders.get("/swaggersummary/{id}", "1"))
                .andExpect(status().isNotFound());
    }
    
}
