package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.revature.docutest.TestUtil;
import com.revature.templates.LoadTestConfig;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

class JMeterServiceTest {

    private JMeterService jm;
    private LoadTestConfig loadConfig = new LoadTestConfig();
    private static final String JMeterPropPath = "src/test/resources/test.properties";
    private static final String CSV_FILE_PATH = "./datafiles/user_0.csv";
    public static final String DIRECTORY_PATH = "./datafiles";

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        loadConfig.setLoops(1);
        loadConfig.setRampUp(2);
        loadConfig.setThreads(10);
        loadConfig.setDuration(-1);
        loadConfig.setTestPlanName("JMeterServicesTest");

        jm = new JMeterService();
        TestUtil.initFields();

        File directory = new File(DIRECTORY_PATH);
        deleteFolder(directory);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testLoadTestingLoop() {
        loadConfig.setLoops(2);
        int expectedReq = (loadConfig.getLoops() * loadConfig.getThreads());

        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            int counter = getCounter(reader);
            System.out.println("Expected Request Count: " + expectedReq);
            System.out.println("Actual Request Count: " + counter);
            assertEquals(expectedReq, counter);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testLoadTestingLoopMultiReq() {
        loadConfig.setLoops(2);
        int expectedReq = (loadConfig.getLoops() * loadConfig.getThreads());

        jm.loadTesting(TestUtil.multi, loadConfig, JMeterPropPath);
        for (int i = 0; i < 2; i++) {
            String filename = JMeterService.BASE_FILE_PATH + i + ".csv";
            System.out.println(filename);
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                int counter = getCounter(reader);
                System.out.println("Expected Request Count: " + expectedReq);
                System.out.println("Actual Request Count: " + counter);
                assertEquals(expectedReq, counter);
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        }

    }

    @Test
    void testLoadTestingDuration() throws IOException {
        loadConfig.setDuration(3);
        loadConfig.setLoops(-1);

        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            long diff = getDiff(reader);
            // flat amount + 5% of duration in ms
            System.out.println("Difference between expected and actual duration (ms): "
                    + Math.abs((loadConfig.getDuration() * 1000) - diff));
            assertTrue(Math
                    .abs((loadConfig.getDuration() * 1000) - diff) < (2000 + (loadConfig.getDuration() * 1000 / 20)));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testLoadTestingDurationMulti() {
        loadConfig.setDuration(10);
        loadConfig.setLoops(-1);

        jm.loadTesting(TestUtil.multi, loadConfig, JMeterPropPath);

        for (int i = 0; i < 2; i++) {
            String filename = JMeterService.BASE_FILE_PATH + i + ".csv";
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                long diff = getDiff(reader);
                long expectedDuration = loadConfig.getDuration() * 1000;
                System.out.println(
                        "Difference between expected and actual duration (ms): " + Math.abs(expectedDuration - diff));
                // flat amount + 5% of duration in ms
                assertTrue(Math.abs((expectedDuration) - diff) < (2000 + (loadConfig.getDuration() * 1000 / 20)));
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        }

    }

    @Test
    void testHttpSamplerDistinctRequestCount() {
        Set<HTTPSampler> samplers = jm.createHTTPSampler(TestUtil.get);
        assertEquals(1, samplers.size());
        samplers = jm.createHTTPSampler(TestUtil.todos);
        assertEquals(7, samplers.size());
    }

    @Test
    void testHttpSamplerEndpoints() {
        // get.json
        Set<String> expected = new HashSet<>();
        expected.add("/");
        Set<HTTPSampler> samplers = jm.createHTTPSampler(TestUtil.get);
        for (HTTPSampler sampler : samplers) {
            assertTrue(expected.contains(sampler.getPath()));
        }

        // todos.json
        expected.clear();
        expected.add("/todos");
        expected.add("/todos/truncate");
        expected.add("/todos/1"); // needs to be changed once path var is implemented
        samplers = jm.createHTTPSampler(TestUtil.todos);

        for (HTTPSampler sampler : samplers) {
            assertTrue(expected.contains(sampler.getPath()));
        }
    }

    @Test
    void testHttpSamplerNull() {
        assertEquals(0, jm.createHTTPSampler(null).size());
    }

    @Test
    void testHttpSamplerNoReq() {
        assertEquals(0, jm.createHTTPSampler(TestUtil.blank).size());
    }

    @Test
    void testHttpSamplerNoHost() {
        assertEquals(0, jm.createHTTPSampler(TestUtil.malformed).size());
    }

    @Test
    void testCreateLoopController() {
        Set<HTTPSampler> samplerSet = jm.createHTTPSampler(TestUtil.todos);
        for (HTTPSampler element : samplerSet) {
            LoopController testLC = (LoopController) jm.createLoopController(element, loadConfig.getLoops());
            assertEquals(loadConfig.getLoops(), testLC.getLoops());
            // way to check loadconfig elements?
        }

    }

    @Test
    void testCreateLoopControllerNull() {
        assertNull(jm.createLoopController(null, loadConfig.getLoops()));
    }

    @Test
    void testParseURL() {
        String expected = "/todos/1";

        Swagger swag = TestUtil.todos;
        String basePath = swag.getBasePath();
        Map<String, Path> endpoints = swag.getPaths();
        for (String path : endpoints.keySet()) {
            Path pathOperations = endpoints.get(path);
            Map<HttpMethod, Operation> verbs = pathOperations.getOperationMap();
            for (HttpMethod verb : verbs.keySet()) {
                if (basePath.equals("/")) {
                    basePath = "";
                }
                String fullPath = basePath + path;

                String parsedURL = jm.parseURL(fullPath, verbs);

                // Assertion here
                if (fullPath.equals("/todos/{id}")) {
                    assertEquals(expected, parsedURL);
                }
            }
        }
    }

    // helper method to clear folder between each test
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    // helper method for duration tests to get difference between latest starttime
    // and initial starttime, in ms
    public static long getDiff(BufferedReader reader) throws NumberFormatException, IOException {
        String dat;
        int counter = 0;
        long startTime = 0;
        String[] row = new String[3];
        while ((dat = reader.readLine()) != null) {
            if (counter != 0) {
                row = dat.split(",");

                String timestamp = row[0];
                if (counter == 1) {
                    startTime = Long.parseLong(timestamp);
                }
            }
            counter++;
        }
        return Long.parseLong(row[0]) - startTime;
    }

    // helper method to get number of httprequests sent for loop-based tests
    public static int getCounter(BufferedReader reader) throws IOException {
        int counter = 0;
        // number of distinct req
        // may need to revisit once S3 is implemented

        while (reader.readLine() != null) {
            counter++;
        }
        counter--; // decrement for header line
        return counter;
    }
}
