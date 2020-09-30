package com.revature.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;
import com.revature.templates.LoadTestConfig;

class JMeterServicesTest {
    
    private JMeterServices jm;
    private LoadTestConfig loadConfig = new LoadTestConfig();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        loadConfig.loops = 2;
        loadConfig.rampUp = 2;
        loadConfig.threads = 20;
        
        jm = new JMeterServices();
        TestUtil.initFields();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testTemp() {
        jm.loadTesting(TestUtil.todos, loadConfig);
    }
    
    @Test
    void testHttpSampler() {
        Set<HTTPSampler> samplers = jm.createHTTPSampler(TestUtil.get);
        assertTrue(1 == samplers.size());
        samplers = jm.createHTTPSampler(TestUtil.todos);
        assertTrue(7 == samplers.size());
    }
    
    @Test
    void testHttpSamplerNull() {
        assertTrue(0 == jm.createHTTPSampler(null).size());
    }

}
