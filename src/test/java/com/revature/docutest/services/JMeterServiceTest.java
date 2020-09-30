package com.revature.docutest.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;

class JMeterServiceTest {
    
    private JMeterService jmService;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        jmService = new JMeterService();
        TestUtil.initFields();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testCreateHttpSampler() {
        Set<HTTPSampler> ret = jmService.createHTTPSampler(TestUtil.swag1);
        
    }

}
