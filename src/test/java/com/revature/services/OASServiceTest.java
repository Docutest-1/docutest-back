package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.revature.docutest.DocutestApplication;
import com.revature.docutest.TestUtil;
import com.revature.models.Request;

@SpringBootTest(classes = DocutestApplication.class)
@ContextConfiguration(classes = {OASService.class})
class OASServiceTest {
    
    private List<Request> reqList;
    
    @Autowired
    private OASService oasService;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testGetRequests() {
        reqList = oasService.getRequests(TestUtil.get);
        assertEquals(TestUtil.getReq, reqList); 
    }
    
    @Test
    void testPostRequests() {
        reqList = oasService.getRequests(TestUtil.post);
        assertEquals(TestUtil.postReq, reqList);
    }
    
    @Test
    void testGetRequestsMulti() {
        reqList = oasService.getRequests(TestUtil.multi);
        assertEquals(TestUtil.getReqMulti, reqList); 
    }
    
    @Test
    void testNullInput() {
        reqList = oasService.getRequests(null);
        assertEquals(0, reqList.size());
    }

}
