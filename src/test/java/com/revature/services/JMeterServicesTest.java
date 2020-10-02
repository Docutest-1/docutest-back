package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class JMeterServicesTest {

    private JMeterServices jm;
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
        loadConfig.loops = 1;
        loadConfig.rampUp = 2;
        loadConfig.threads = 20;
        loadConfig.duration = -1;
        loadConfig.testPlanName = "JMeterServicesTest";

        jm = new JMeterServices();
        TestUtil.initFields();

//        File logFile = new File(CSV_FILE_PATH);
//        logFile.delete();

        File directory = new File(DIRECTORY_PATH);
        deleteFolder(directory);
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testLoadTestingLoop() {
        loadConfig.loops = 2;
        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            int counter = 0;
            int expectedReq = (loadConfig.loops * loadConfig.threads);

            while (reader.readLine() != null) {
                counter++;
            }
            counter--; // decrement for header line
            System.out.println("Expected Request Count: " + expectedReq);
            System.out.println("Actual Request Count: " + counter);
            assertTrue(counter == expectedReq);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testLoadTestingLoopMultiReq() {
        loadConfig.loops = 2;
        jm.loadTesting(TestUtil.multi, loadConfig, JMeterPropPath);
        String multi_base_path = jm.BASE_FILE_PATH;
        for (int i = 0; i < 2; i++) {
            String filename = multi_base_path + i + ".csv";
            System.out.println(filename);
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                int counter = 0;
                // number of distinct req
                // may need to revisit once S3 is implemented
                int expectedReq = (loadConfig.loops * loadConfig.threads); 

                while (reader.readLine() != null) {
                    counter++;
                }
                counter--; // decrement for header line
                System.out.println("Expected Request Count: " + expectedReq);
                System.out.println("Actual Request Count: " + counter);
                assertTrue(counter == expectedReq);
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        }
        
    }

    @Test
    void testLoadTestingDuration() throws IOException {
        loadConfig.duration = 3;
        loadConfig.loops = -1;

        jm.loadTesting(TestUtil.get, loadConfig, JMeterPropPath);

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
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
            long diff = Long.parseLong(row[0]) - startTime;
            // 1000 base + 10% of duration in ms
            System.out.println("Difference between expected and actual duration (ms): "
                    + Math.abs((loadConfig.duration * 1000) - diff));
            assertTrue(Math.abs((loadConfig.duration * 1000) - diff) < (1000 + (loadConfig.duration*1000/10)));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void testLoadTestingDurationMulti() {
        loadConfig.duration = 10;
        loadConfig.loops = -1;

        jm.loadTesting(TestUtil.multi, loadConfig, JMeterPropPath);
        
        for (int i = 0; i < 2; i++) {
            String filename = JMeterServices.BASE_FILE_PATH + i + ".csv";
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
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
                long diff = Long.parseLong(row[0]) - startTime;
                long expectedDuration = loadConfig.duration*1000;
                System.out.println("Difference between expected and actual duration (ms): " 
                        + Math.abs(expectedDuration-diff));
                // 1000 base + 10% of duration in ms
                assertTrue(Math.abs((expectedDuration)-diff) < (1000 + (loadConfig.duration*1000/10))); 
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        }
        
    }

    @Test
    void testHttpSamplerDistinctRequestCount() {
        Set<HTTPSampler> samplers = jm.createHTTPSampler(TestUtil.get);
        assertTrue(1 == samplers.size());
        samplers = jm.createHTTPSampler(TestUtil.todos);
        assertTrue(7 == samplers.size());
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
    
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
