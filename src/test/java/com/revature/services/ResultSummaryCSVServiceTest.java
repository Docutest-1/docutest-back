package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.opencsv.CSVWriter;
import com.revature.docutest.DocutestApplication;
import com.revature.models.ResultSummaryCsv;
import com.revature.repositories.ResultSummaryCsvRepository;

@SpringBootTest(classes = DocutestApplication.class)
@ContextConfiguration(classes = ResultSummaryCsvService.class)
class ResultSummaryCSVServiceTest {

    private ResultSummaryCsvService testInstance;

    @Mock
    private ResultSummaryCsvRepository MockedDao;

    private ResultSummaryCsv testCsv;
    private ResultSummaryCsv testCsvFail;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        testCsv = new ResultSummaryCsv();
        testCsv.setId(1);

        testCsvFail = new ResultSummaryCsv();
        testCsvFail.setId(2);

        testInstance = new ResultSummaryCsvService(MockedDao);
        when(MockedDao.findById(1)).thenReturn(Optional.of(testCsv));
        when(MockedDao.save(any())).thenReturn(testCsv);
        when(MockedDao.save(testCsv)).thenReturn(testCsv);
        when(MockedDao.save(testCsvFail)).thenReturn(testCsv);

    }

    @Test
    void testCreateCsv() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        CSVWriter writer = new CSVWriter(streamWriter);
        assertEquals(testCsv, testInstance.createCSV(writer));
    }

    @Test
    void testGetById() {
        ResultSummaryCsv expected = testCsv;
        assertEquals(expected, testInstance.getById(1).get());
    }

    @Test
    void testUpdateSuccess() {
        assertTrue(testInstance.update(testCsv));
    }

    @Test
    void testUpdateFail() {
        assertFalse(testInstance.update(testCsvFail));
    }

}
