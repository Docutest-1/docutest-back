package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

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

import com.revature.responsecollector.JMeterResponseCollector;

class JMeterResponseCollectorTest {
    private JMeterResponseCollector jmrc;
    private StandardJMeterEngine engine;
    private static final String JMeterPropPath = "src/main/resources/jmeter.properties";
    
    private static List<SampleEvent> sampleEvents = new ArrayList<>();
    
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        SampleEvent event1;
        SampleEvent event2;
        SampleEvent event3;
        SampleEvent event4;
        SampleResult event1Result;
        SampleResult event2Result;
        SampleResult event3Result;
        SampleResult event4Result;
        SampleResult event5Result;
        
        event1Result = SampleResult.createTestSample(100,200);
        event1Result.setLatency(100);
        event1Result.setResponseCode("200");
        event1 = new SampleEvent(event1Result, "Sample Thread Group");
        
        event2Result = SampleResult.createTestSample(200, 400);
        event2Result.setLatency(200);
        event2Result.setResponseCode("400");
        event2 = new SampleEvent(event2Result, "Sample Thread Group");
        
        event3Result = SampleResult.createTestSample(400, 800);
        event3Result.setLatency(400);
        event3Result.setResponseCode("400");
        event3 = new SampleEvent(event3Result, "Sample Thread Group");
        
        event4Result = SampleResult.createTestSample(800, 1100);
        event4Result.setLatency(300);
        event4Result.setResponseCode("400");
        event4 = new SampleEvent(event4Result, "Sample Thread Group");
        
        sampleEvents.add(event1);
        sampleEvents.add(event2);
        sampleEvents.add(event3);
        sampleEvents.add(event4);
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
    }

    @AfterEach
    void tearDown() throws Exception {
        jmrc.setCurrentTime(0);
        jmrc.setStartTime(0);
        jmrc.setResponseMax(0);
        jmrc.setResponseTimes(null);
    }
    
    @Test
    void testgetsuccessFailPercentageNoEvents() {
        double expected = 0.0;
        assertEquals(expected, jmrc.getsuccessFailPercentage());
    }
    
    @Test
    void test200ResponseNoEvents() {
        int expected = 0;
        assertEquals(expected, jmrc.getNum2XX());
    }
    
    @Test
    void test400ResponseNoEvents() {
        int expected = 0;
        assertEquals(expected, jmrc.getNum4XX());
    }
    
    @Test
    void test200ResponseEvent() {
        int expected = jmrc.getNum2XX() + 1;
        jmrc.sampleOccurred(sampleEvents.get(0));
        assertEquals(expected, jmrc.getNum2XX());
    }
    
    @Test
    void test400ResponseEvent() {
        int expected = jmrc.getNum4XX() + 1;
        jmrc.sampleOccurred(sampleEvents.get(1));
        assertEquals(expected, jmrc.getNum4XX());
    }
    
    @Test
    void testAddLatency() {
        ArrayList<Long> expected = new ArrayList<Long>();
        expected.add((long) 100);
        jmrc.sampleOccurred(sampleEvents.get(0));
        assertEquals(expected, jmrc.getResponseTimes());
    }
    
    @Test
    void testFirstStartTime() {
        long startTime = 100;
        jmrc.sampleOccurred(sampleEvents.get(0));
        assertEquals(startTime, jmrc.getStartTime());
    }
    @Test
    void testStartTimeAfterSet() {
        long startTime = 100;
        jmrc.sampleOccurred(sampleEvents.get(0));
        jmrc.sampleOccurred(sampleEvents.get(1));
        assertEquals(startTime, jmrc.getStartTime());
    }
    @Test
    void testCurrentTime() {
        long currentTime = 200;
        jmrc.sampleOccurred(sampleEvents.get(1));
        assertEquals(currentTime, jmrc.getCurrentTime());
    }
    
    @Test
    void testMaxLatency() {
        long responseMax = 200;
        jmrc.sampleOccurred(sampleEvents.get(1));
        assertEquals(responseMax, jmrc.getResponseMax());
    }
    
    @Test
    void testgetsuccessFailPercentage() {
        jmrc.sampleOccurred(sampleEvents.get(0));
        jmrc.sampleOccurred(sampleEvents.get(1));
        float expected = 50f;
        assertEquals(expected, jmrc.getsuccessFailPercentage());
    }
    
    @Test
    void testgetResponseAvg() {
        long expected = 150;
        jmrc.sampleOccurred(sampleEvents.get(0));
        jmrc.sampleOccurred(sampleEvents.get(1));
        assertEquals(expected, jmrc.getResponseAvg());
    }
    
    @Test
    void testgetReqPerSec() {
        long expected = 20;
        jmrc.sampleOccurred(sampleEvents.get(0));
        jmrc.sampleOccurred(sampleEvents.get(1));
        assertEquals(expected, jmrc.getReqPerSec());
    }
    
    @Test
    void testgetResponse25percentile() {
        long expected = 100;
        for (SampleEvent e : sampleEvents) {
            jmrc.sampleOccurred(e);
        }
        assertEquals(expected, jmrc.getResponse25Percentile());
    }
    
    @Test
    void testgetResponse50percentile() {
        long expected = 200;
        for (SampleEvent e : sampleEvents) {
            jmrc.sampleOccurred(e);
        }
        assertEquals(expected, jmrc.getResponse50Percentile());
    }
    
    @Test
    void testgetResponse75percentile() {
        long expected = 300;
        for (SampleEvent e : sampleEvents) {
            jmrc.sampleOccurred(e);
        }
        assertEquals(expected, jmrc.getResponse75Percentile());
    }
}
