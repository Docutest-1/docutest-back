package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.revature.templates.SwaggerSummaryDTO;

@SpringBootTest(classes = DocutestApplication.class)
@ContextConfiguration(classes = SwaggerSummaryService.class)
class SwaggerSummaryServiceTest {
    private SwaggerSummaryService testInstance;
    
    @Mock
    private SwaggerSummaryRepository MockedDao;
    
    private LoadTestConfig ltcLoops;
    private LoadTestConfig ltcDuration;
    
    private SwaggerSummary swaggerSummary;
    private SwaggerSummary swaggerSummaryfail;
    
    List<SwaggerSummary> swaggerSummaries;
    List<SwaggerSummaryDTO> swaggerSummariesDto;

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
        
        swaggerSummaries = new ArrayList<>();
        swaggerSummaries.add(swaggerSummary);
        swaggerSummaries.add(swaggerSummaryfail);
        
        swaggerSummariesDto = new ArrayList<>();
        swaggerSummariesDto.add(new SwaggerSummaryDTO(swaggerSummary));
        swaggerSummariesDto.add(new SwaggerSummaryDTO(swaggerSummaryfail));
        
        ltcLoops = new LoadTestConfig();
        ltcLoops.setLoops(10);
        ltcLoops.setRampUp(2);
        ltcLoops.setThreads(10);
        ltcLoops.setDuration(-1);
        ltcLoops.setTestPlanName("Loop Test");
        
        ltcDuration = new LoadTestConfig();
        ltcDuration.setLoops(-1);
        ltcDuration.setRampUp(2);
        ltcDuration.setThreads(10);
        ltcDuration.setDuration(10);
        ltcDuration.setTestPlanName("Duration Test");
        
        when(MockedDao.save(any(SwaggerSummary.class))).thenReturn(swaggerSummary);
        when(MockedDao.existsById(1)).thenReturn(false);
        when(MockedDao.findById(1)).thenReturn(Optional.of(swaggerSummary));
        
        when(MockedDao.save(swaggerSummaryfail)).thenReturn(swaggerSummary);
        when(MockedDao.existsById(2)).thenReturn(true);
        when(MockedDao.findById(2)).thenReturn(null);
        
        when(MockedDao.findAll()).thenReturn(swaggerSummaries);

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
        assertEquals(testInstance.getById(1).get(), swaggerSummary);
    }
    
    @Test
    void uploadSwaggerfileResultRef() {
        SwaggerUploadResponse result = testInstance.uploadSwaggerfile(TestUtil.multi, ltcLoops);
        assertEquals("Docutest/swaggersummary/1", result.getResultRef());
    }
        
    @Test
    void uploadSwaggerfileSwaggerSummaryId() {
        SwaggerUploadResponse result = testInstance.uploadSwaggerfile(TestUtil.multi, ltcDuration);
        assertEquals("Docutest/swaggersummary/1", result.getResultRef());
        assertEquals(1,result.getSwaggerSummaryId());
    }
    
    @Test
    void testGetAllSwaggerSummaries() {
        List<SwaggerSummary> results = testInstance.getAllSwaggerSummaries();
        assertEquals(swaggerSummaries, results);
    }
    
    @Test
    void testConvertSwaggerSummary() {
        List<SwaggerSummaryDTO> results = testInstance.convertSwaggerSummary(swaggerSummaries);
        assertEquals(swaggerSummariesDto, results);
    }
}
