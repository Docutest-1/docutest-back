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
        loadConfig.duration = 10;
        loadConfig.loops = 2;
        loadConfig.rampUp = 2;
        loadConfig.threads = 20;
        loadConfig.testPlanName = "JMeterServicesTest";

        jm = new JMeterServices();
        TestUtil.initFields();

        engine = new StandardJMeterEngine();
        JMeterUtils.loadJMeterProperties(JMeterPropPath);
        // JMeterUtils.initLogging();
        JMeterUtils.initLocale();
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testRun() {
        HTTPSampler element = new HTTPSampler();

        element.setProtocol(HTTPSampler.PROTOCOL_HTTP);
        element.setDomain("google.com");
        element.setPort(80);
        element.setPath("/");
        // http verb
        element.setMethod("GET");
        element.setUseKeepAlive(true);

        LoopController lc = new LoopController();
        lc.setLoops(5);
        lc.addTestElement(element);
        lc.setFirst(true);
        lc.initialize();

        SetupThreadGroup tg = jm.createLoad(lc, 50, 2, 2);

        HashTree config = new HashTree();
        config.add("testPlan", new TestPlan("test"));
        config.add("loopController", lc);
        config.add("setupThreadGroup", tg);
        config.add("httpSampler", element);
        Summariser summer = null;

        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }
        String logFile = "/temp/temp/file.jtl";
        JMeterResponseCollector logger = new JMeterResponseCollector(summer);
        logger.setFilename(logFile);
        config.add(config.getArray()[0], logger);

        engine.configure(config);
        engine.run();

//        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);
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
        LoopController testLC = (LoopController) jm.createLoopController(samplerSet, loadConfig.loops);
        assertTrue(loadConfig.loops == testLC.getLoops());
        // way to check loadconfig elements?
    }

    @Test
    void testCreateLoopControllerNull() {
        assertTrue(null == jm.createLoopController(null, loadConfig.loops));
    }

    @Test
    void testCreateLoopControllerEmptySet() {
        assertTrue(null == jm.createLoopController(new HashSet<HTTPSampler>(), loadConfig.loops));
    }

    // ------------------ EXPLORATORY TESTS ------------------

    @Test
    void testRunNullLoopController() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        SetupThreadGroup testThreadGroup = jm.createLoad(null, loadConfig.threads, loadConfig.rampUp,
                loadConfig.duration);
        HashTree testHashTree = jm.createTestConfig(loadConfig.testPlanName, null, testThreadGroup);
        assertThrows(NullPointerException.class, () -> {
            engine.configure(testHashTree);
        });
        engine.run();

    }

    @Test
    void testRunNullThreadGroup() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        LoopController testLC = (LoopController) jm.createLoopController(samplerSet, loadConfig.loops);

        SetupThreadGroup testThreadGroup = jm.createLoad(testLC, loadConfig.threads, loadConfig.rampUp,
                loadConfig.duration);
        HashTree testHashTree = jm.createTestConfig(loadConfig.testPlanName, testLC, null);

        assertThrows(NullPointerException.class, () -> {
            engine.configure(testHashTree);
        });
        engine.run();

    }

    @Test
    void testRunMissingHTTPSamplers() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        LoopController testLC = (LoopController) jm.createLoopController(samplerSet, loadConfig.loops);

        SetupThreadGroup testThreadGroup = jm.createLoad(testLC, loadConfig.threads, loadConfig.rampUp,
                loadConfig.duration);
        HashTree testHashTree = jm.createTestConfig(loadConfig.testPlanName, testLC, testThreadGroup);
        engine.configure(testHashTree);
        engine.run();
    }

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
