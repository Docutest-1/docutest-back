package com.revature.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;
import com.revature.responsecollector.JMeterResponseCollector;
import com.revature.templates.LoadTestConfig;

class JMeterServicesTest {

    private JMeterServices jm;
    private LoadTestConfig loadConfig = new LoadTestConfig();
    private StandardJMeterEngine engine;
    private static final String JMeterPropPath = "src/test/resources/test.properties";

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        loadConfig.loops = 1;
        loadConfig.rampUp = 2;
        loadConfig.threads = 20;
        loadConfig.testPlanName = "JMeterServicesTest";

        jm = new JMeterServices();
        TestUtil.initFields();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testLoadTestingLoop() {
        loadConfig.loops = 2;
        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);
    }
    
    @Test
    void testLoadTestingDuration() {
        loadConfig.duration = 3;
        loadConfig.loops = -1;
        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);
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

    @Test
    void testHttpSamplerNoReq() {
        assertTrue(0 == jm.createHTTPSampler(TestUtil.blank).size());
    }

    @Test
    void testHttpSamplerNoHost() {
        assertTrue(0 == jm.createHTTPSampler(TestUtil.malformed).size());
    }

    @Test
    void testCreateLoopController() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        for (HTTPSampler element : samplerSet) {
            LoopController testLC = (LoopController) jm.createLoopController(element, loadConfig.loops);
            assertTrue(loadConfig.loops == testLC.getLoops());
            // way to check loadconfig elements?
        }

    }

    @Test
    void testCreateLoopControllerNull() {
        assertTrue(null == jm.createLoopController(null, loadConfig.loops));
    }


    // ------------------ EXPLORATORY TESTS ------------------

    @Disabled
    @Test
    void testRunNullLoopController() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        SetupThreadGroup testThreadGroup = jm.createLoad(null, loadConfig.threads, loadConfig.rampUp);
        HashTree testHashTree = jm.createTestConfig(loadConfig.testPlanName, null, testThreadGroup);
        assertThrows(NullPointerException.class, () -> {
            engine.configure(testHashTree);
        });
        engine.run();

    }

    @Disabled
    @Test
    void testRunNullThreadGroup() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        for (HTTPSampler element : samplerSet) {
            LoopController testLC = (LoopController) jm.createLoopController(element, loadConfig.loops);

            SetupThreadGroup testThreadGroup = jm.createLoad(testLC, loadConfig.threads, loadConfig.rampUp);
            HashTree testHashTree = jm.createTestConfig(loadConfig.testPlanName, testLC, null);

            assertThrows(NullPointerException.class, () -> {
                engine.configure(testHashTree);
            });
            engine.run();
        }
    }

    @Disabled
    @Test
    void testRunMissingHTTPSamplers() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        for (HTTPSampler element : samplerSet) {
            LoopController testLC = (LoopController) jm.createLoopController(element, loadConfig.loops);

            SetupThreadGroup testThreadGroup = jm.createLoad(testLC, loadConfig.threads, loadConfig.rampUp);
            HashTree testHashTree = jm.createTestConfig(loadConfig.testPlanName, testLC, testThreadGroup);
            engine.configure(testHashTree);
            engine.run();
        }

    }
    
    @Disabled
    @Test
    void testRunNullHashTree() {
        assertThrows(NullPointerException.class, () -> {
            // both will throw null pointer exception
            engine.configure(null);
            engine.run();
        });
    }

    // --------------- END OF EXPLORATORY TESTS -------------

}
