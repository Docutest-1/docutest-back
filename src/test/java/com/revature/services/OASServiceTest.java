package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.http.control.Header;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;
import com.revature.models.Request;
import com.revature.ordering.RequestComparator;

class OASServiceTest {
    
    private List<Request> reqList;
    
    // not autowired since it's a unit test, 
    // don't want to have Spring set up all of our beans/dependencies
    private OASService oasService = new OASService(new JSONStringCreator());

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
    
    // ---------------------- PARAM TESTS --------------------------
    
    @Test
    void testPathParam() {
        reqList = oasService.getRequests(TestUtil.pathParam);
        Map<String, String> pathParams = reqList.get(0).getPathParams();
        assertEquals(1, pathParams.size());
        // check header added to map properly
        assertEquals("1", pathParams.get("testparam"));
        // check path modified properly
        assertEquals("/1", reqList.get(0).getEndpoint().getPath());
    }
    
    @Test
    void testHeaderParam() {
        reqList = oasService.getRequests(TestUtil.todos);
        // arraylists preserve insertion order, but if we add/remove requests
        // from example.json, this will need to change
        List<Header> headerParams = reqList.get(0).getHeaderParams();
        assertEquals(1, headerParams.size());
        assertEquals(new Header("Accept", "application/json"), headerParams.get(0));
    }
    
    @Test
    void testNullInput() {
        reqList = oasService.getRequests(null);
        assertEquals(0, reqList.size());
    }
    
    @Test
    void testRequestOrder() {
        reqList = oasService.getRequests(TestUtil.todos);
        reqList.sort(new RequestComparator());
        for (int i = 0; i < reqList.size(); i++) {
            assertEquals(reqList.get(i).getVerb(), TestUtil.verbOrder[i]);
            assertEquals(reqList.get(i).getEndpoint().getPath(), TestUtil.pathOrder[i]);
        }
    }

}
