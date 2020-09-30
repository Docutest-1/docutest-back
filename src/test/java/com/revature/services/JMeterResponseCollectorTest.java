package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;
import com.revature.responsecollector.JMeterResponseCollector;
import com.revature.templates.LoadTestConfig;

public class JMeterResponseCollectorTest {
    private JMeterResponseCollector jmrc;
    private StandardJMeterEngine engine;
    private static final String JMeterPropPath = "src/test/resources/test.properties";
    private SampleEvent event1;
    private SampleEvent event2;
    private SampleEvent event3;
    private SampleEvent event4;
    private SampleResult event1Result;
    private SampleResult event2Result;
    private SampleResult event3Result;
    private SampleResult event4Result;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {

        engine = new StandardJMeterEngine();
        JMeterUtils.loadJMeterProperties(JMeterPropPath);
        Summariser summer = null;
     
        
        jmrc = new JMeterResponseCollector(summer);
        

        event1Result = SampleResult.createTestSample(100,200);
        event1Result.setLatency(100);
        event1Result.setResponseCode("200");
        event1 = new SampleEvent(event1Result, "Sample Thread Group");
        
        event2Result = SampleResult.createTestSample(200, 400);
        event2Result.setLatency(200);
        event2Result.setResponseCode("400");
        event2 = new SampleEvent(event2Result, "Sample Thread Group");
        
        event3Result = SampleResult.createTestSample(400, 600);
        event3Result.setLatency(200);
        event3Result.setResponseCode("400");
        event3 = new SampleEvent(event3Result, "Sample Thread Group");
        
        event4Result = SampleResult.createTestSample(600, 900);
        event4Result.setLatency(300);
        event4Result.setResponseCode("400");
        event4 = new SampleEvent(event4Result, "Sample Thread Group");
        

    }

    @AfterEach
    void tearDown() throws Exception {
        jmrc.setFailCount(0);
        jmrc.setCurrentTime(0);
        jmrc.setStartTime(0);
        jmrc.setResponseMax(0);
        jmrc.setLatencyTimes(null);
        jmrc.setOkResponse(0);
    }
    
    @Test
    void testgetsuccessFailPercentageNoEvents() {
        long expected = (long) 0;
        assertEquals(expected, jmrc.getsuccessFailPercentage());
    }
    
    @Test
    void test200ResponseNoEvents() {
        int expected = 0;
        jmrc.sampleOccurred(event1);
        assertEquals(expected, jmrc.getOkResponse());
    }
    
    @Test
    void test400ResponseNoEvents() {
        int expected = 0;
        jmrc.sampleOccurred(event2);
        assertEquals(expected, jmrc.getFailCount());
    }
    
    @Test
    void test200ResponseEvent() {
        int expected = jmrc.getOkResponse() + 1;
        jmrc.sampleOccurred(event1);
        assertEquals(expected, jmrc.getOkResponse());
    }
    
    @Test
    void test400ResponseEvent() {
        int expected = jmrc.getFailCount() + 1;
        jmrc.sampleOccurred(event2);
        assertEquals(expected, jmrc.getFailCount());
    }
    
    @Test
    void testAddLatency() {
        ArrayList<Long> expected = new ArrayList<Long>();
        expected.add((long) 100);
        jmrc.sampleOccurred(event1);
        assertEquals(expected, jmrc.getLatencyTimes());
    }
    
    @Test
    void testFirstStartTime() {
        long startTime = 100;
        jmrc.sampleOccurred(event1);
        assertEquals(startTime, jmrc.getStartTime());
    }
    @Test
    void testStartTimeAfterSet() {
        long startTime = 100;
        jmrc.sampleOccurred(event1);
        jmrc.sampleOccurred(event2);
        assertEquals(startTime, jmrc.getStartTime());
    }
    @Test
    void testCurrentTime() {
        long currentTime = 200;
        jmrc.sampleOccurred(event2);
        assertEquals(currentTime, jmrc.getCurrentTime());
    }
    
    @Test
    void testMaxLatency() {
        long responseMax = 200;
        jmrc.sampleOccurred(event2);
        assertEquals(responseMax, jmrc.getResponseMax());
    }
    
    @Test
    void testgetsuccessFailPercentage() {
        jmrc.sampleOccurred(event1);
        jmrc.sampleOccurred(event2);
        long expected = (long) 0.5;
        assertEquals(expected, jmrc.getsuccessFailPercentage());
    }
    
    @Test
    void testgetResponseAvg() {
        long expected = 150;
        jmrc.sampleOccurred(event1);
        jmrc.sampleOccurred(event2);
        assertEquals(expected, jmrc.getResponseAvg());
    }
    
    @Test
    void testgetReqPerSec() {
        long expected = 20;
        jmrc.sampleOccurred(event1);
        jmrc.sampleOccurred(event2);
        System.out.println(jmrc.getStartTime());
        System.out.println(jmrc.getCurrentTime());
        assertEquals(expected, jmrc.getReqPerSec());
    }
}
