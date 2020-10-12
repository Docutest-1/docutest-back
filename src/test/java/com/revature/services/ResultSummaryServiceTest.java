package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.revature.docutest.DocutestApplication;
import com.revature.models.ResultSummary;
import com.revature.repositories.ResultSummaryRepository;


@SpringBootTest(classes = DocutestApplication.class)
@ContextConfiguration(classes = ResultSummaryService.class)
class ResultSummaryServiceTest {

    private ResultSummaryService testInstance;
    
    @Mock
    private ResultSummaryRepository MockedDao;
    
    private ResultSummary resultSummary;
    private ResultSummary resultSummaryfail;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        testInstance = new ResultSummaryService(MockedDao);
        resultSummary = new ResultSummary();
        resultSummary.setId(1);
        
        resultSummaryfail = new ResultSummary();
        resultSummaryfail.setId(2);
        when(MockedDao.save(resultSummary)).thenReturn(resultSummary);
        when(MockedDao.existsById(1)).thenReturn(false);
        when(MockedDao.findById(1)).thenReturn(Optional.of(resultSummary));
        
        when(MockedDao.save(resultSummaryfail)).thenReturn(resultSummary);
        when(MockedDao.existsById(2)).thenReturn(true);
        when(MockedDao.findById(2)).thenReturn(null);


    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    void testInsertResultSummary() {
        assertEquals(resultSummary, testInstance.insert(resultSummary));
    }

    @Test
    void testUpdateSuccess() {
        assertTrue(testInstance.update(resultSummary));
    }
    
    @Test
    void testUpdateFail() {
        assertFalse(testInstance.update(resultSummaryfail));
    }
    
    @Test
    void testDeleteSuccess() {
        assertTrue(testInstance.delete(resultSummary));
    }
    
    @Test
    void testDeleteFail() {
        assertFalse(testInstance.delete(resultSummaryfail));
    }
    
    @Test
    void testGetById() {
        assertEquals(resultSummary, testInstance.getById(1).get());
    }
    
    @Test
    void testGetByInvalidId() {
        assertEquals(null, testInstance.getById(2));
    }
}
