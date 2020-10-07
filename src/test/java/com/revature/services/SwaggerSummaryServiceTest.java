package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.revature.docutest.DocutestApplication;
import com.revature.docutest.TestUtil;
import com.revature.models.SwaggerSummary;
import com.revature.models.SwaggerUploadResponse;
import com.revature.repositories.SwaggerSummaryRepository;
import com.revature.templates.LoadTestConfig;

import io.swagger.models.Swagger;

@SpringBootTest(classes = DocutestApplication.class)
@ContextConfiguration(classes = SwaggerSummaryService.class)
public class SwaggerSummaryServiceTest {
    private SwaggerSummaryService testInstance;
    
    @Mock
    private SwaggerSummaryRepository MockedDao;
    
    private LoadTestConfig ltcLoops;
    private LoadTestConfig ltcDuration;
    
    private SwaggerSummary swaggerSummary;
    private SwaggerSummary swaggerSummaryfail;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        TestUtil.initFields();

        testInstance = new SwaggerSummaryService(MockedDao);
        swaggerSummary = new SwaggerSummary();
        swaggerSummary.setId(1);
        
        swaggerSummaryfail = new SwaggerSummary();
        swaggerSummaryfail.setId(2);
        
        ltcLoops = new LoadTestConfig();
        ltcLoops.setLoops(10);
        ltcLoops.setRampUp(2);
        ltcLoops.setThreads(10);
        ltcLoops.setDuration(-1);
        ltcLoops.setTestPlanName("Loop Test");
        
        ltcDuration = new LoadTestConfig();
        ltcLoops.setLoops(-1);
        ltcLoops.setRampUp(2);
        ltcLoops.setThreads(10);
        ltcLoops.setDuration(10);
        ltcLoops.setTestPlanName("Duration Test");
        
        when(MockedDao.save(any(SwaggerSummary.class))).thenReturn(swaggerSummary);
        when(MockedDao.existsById(1)).thenReturn(false);
        when(MockedDao.findById(1)).thenReturn(swaggerSummary);
        
        when(MockedDao.save(swaggerSummaryfail)).thenReturn(null);
        when(MockedDao.existsById(2)).thenReturn(true);
        when(MockedDao.findById(2)).thenReturn(null);

    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    void testNewSwaggerSummary() {
        assertEquals(testInstance.newSummary(), swaggerSummary);
    }

    @Test
    void testUpdateSuccess() {
        assertTrue(testInstance.update(swaggerSummary));
    }
    
    @Test
    void testUpdateFail() {
        assertFalse(testInstance.update(swaggerSummaryfail));
    }
    
    @Test
    void testDeleteSuccess() {
        assertTrue(testInstance.delete(swaggerSummary));
    }
    
    @Test
    void testDeletefail() {
        assertFalse(testInstance.delete(swaggerSummaryfail));
    }
    
    @Test
    void testGetById() {
        assertEquals(testInstance.getById(1), swaggerSummary);
    }
    
    @Test
    void uploadSwaggerfileResultRef() {
        SwaggerUploadResponse result = testInstance.uploadSwaggerfile(TestUtil.multi, ltcLoops);
        assertEquals("Docutest/swaggersummary/1", result.getResultRef());
    }
    
    @Test
    void uploadSwaggerfileSwaggerSummaryId() {
        SwaggerUploadResponse result = testInstance.uploadSwaggerfile(TestUtil.multi, ltcLoops);
        assertEquals("Docutest/swaggersummary/1", result.getResultRef());
        assertEquals(1,result.getSwaggerSummaryId());
    }
}
