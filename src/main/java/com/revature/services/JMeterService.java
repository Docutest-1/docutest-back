package com.revature.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revature.models.Docutest;
import com.revature.models.Request;
import com.revature.models.ResultSummary;
import com.revature.responsecollector.JMeterResponseCollector;
import com.revature.templates.LoadTestConfig;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;



@Service
public class JMeterService {

    // Object representing the config for the JMeter test
    // At the very least, requires a TestPlan, HTTPSampler, and ThreadGroup
    // Test Elements can be nested within each other
    private HashTree hashTree = new HashTree();

    // replace user with username later
    public static final String BASE_FILE_PATH = "./datafiles/user_";

    private LoadTestConfig testConfig = new LoadTestConfig();

    private Set<ResultSummary> resultSummaries = new HashSet<>();

    /**
     * Runs the JMeter test using a Docutest object, test configuration, and JMeter
     * properties path. If both duration and number of loops are set, duration takes
     * precedence. If the requests field of the Docutest object is empty or null, the
     * threads still start up and run, but won't make any requests.
     * 
     * @param input          Input Docutest object
     * @param testConfig     LoadTestConfig object with test settings
     * @param propertiesPath File path to the properties JMeter Properties file
     */
    public void loadTesting(Docutest input, LoadTestConfig testConfig, String propertiesPath) {
        this.testConfig = testConfig;
        StandardJMeterEngine jm = new StandardJMeterEngine();

        JMeterUtils.loadJMeterProperties(propertiesPath);
        JMeterUtils.initLocale();

        // create set of all unique HTTP requests as defined in swagger
        Set<HTTPSampler> httpSampler = this.createHTTPSampler(input);

        int reqNumber = 0;

        // run a separate load test for each req since we want individual CSV/summaries
        // for each
        for (HTTPSampler element : httpSampler) {
            // use TestElement since we may not always want LoopController
            TestElement logicController = createLoopController(element, testConfig.getLoops());

            SetupThreadGroup threadGroup = this.createLoad((LoopController) logicController, testConfig.getThreads(),
                    testConfig.getRampUp());

            TestPlan testPlan = new TestPlan(testConfig.getTestPlanName());
            testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());

            hashTree.add("testPlan", testPlan);
            hashTree.add("setupThreadGroup", threadGroup);
            hashTree.add("httpSampler", element);

            jm.configure(hashTree);

            // recording results of load test
            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }

            // Temporary file to be uploaded to S3
            // Will need to change the filename if we want subdirectories for each user
            // Definitely need to change if we want multiple users to run multiple tests at
            // once
            String logFile = BASE_FILE_PATH + reqNumber + ".csv";
            reqNumber++;

            JMeterResponseCollector logger;
            logger = new JMeterResponseCollector(summer);
            logger.setFilename(logFile);

            hashTree.add(hashTree.getArray()[0], logger);

            try {
                jm.run();
                hashTree.clear();

                ResultSummary resultSummary = new ResultSummary(logger);
                resultSummary.setHttpMethod(element.getMethod());
                resultSummary.setUri(element.getUrl().toURI());
                // TODO file upload to S3 here
                resultSummaries.add(resultSummary);
            } catch (Exception e) {
                // TODO log
                e.printStackTrace();
            }
        }
//        TODO save resultsummaries to Hibernate
    }

    /**
     * For OAS 2.0. Parses HTTP request conditions from Docutest input and generates
     * an array of HTTPSampler objects based the requests field.
     * @param input Docutest object
     * @return Set of HTTPSampler objects. Returns an empty set if there are no
     *         endpoints. Returns null if there is a problem with the Swagger input.
     */
    public Set<HTTPSampler> createHTTPSampler(Docutest input) {
        Set<HTTPSampler> httpSamplers = new HashSet<>();
        if (input != null) {
            List<Request> requests = input.getRequests();
            // each verb/operation
            for (Request req : requests) {
                HTTPSampler element = new HTTPSampler();

                // domain
                element.setDomain(req.getEndpoint().getBaseUrl());
                // port
                element.setPort(req.getEndpoint().getPort());
                String path = removeEmptyPath(req.getEndpoint().getBasePath(), req.getEndpoint().getPath());
                // may want to use enum, also might need to be a setting later
                element.setProtocol("http");
                element.setPath(path);
                element.setMethod(req.getVerb().toString());
                element.setFollowRedirects(true);

                httpSamplers.add(element);
            }
        }
        return httpSamplers;
    }

    /**
     * Configures a LoopController with the given loop count and httpSampler.
     * Returns null if httpSampler is null.
     * 
     * @param httpSampler HTTPSampler object representing a request
     * @param loops       Number of iterations
     * @return Covariant LoopController object.
     */
    public TestElement createLoopController(HTTPSampler httpSampler, int loops) {
        if (httpSampler != null) {
            TestElement loopCtrl = new LoopController();
            ((LoopController) loopCtrl).setLoops(loops);
            loopCtrl.addTestElement(httpSampler);
            return loopCtrl;
        }
        return null;
    }

    /**
     * Creates a thread group (specifically a SetupThreadGroup object) with the
     * given parameters.
     * 
     * @param loopControllers for thread group
     * @param nThreads        Number of threads.
     * @param rampUp          Ramp up time in seconds.
     * @param duration        in seconds
     * @return Configured thread group for ramp up test
     */
    public SetupThreadGroup createLoad(LoopController controller, int threads, int rampUp) {
        if (controller == null) {
            return null;
        }

        SetupThreadGroup ret = new SetupThreadGroup();

        if (testConfig.getDuration() > 0) {
            ret.setScheduler(true);
            ret.setDuration(testConfig.getDuration());
        }
        ret.setNumThreads(threads);
        ret.setRampUp(rampUp);
        ret.setSamplerController(controller); // needs to not be null

        return ret;
    }

    // --------------------- HELPER METHODS -------------------------

    /**
     * Formats path to prevent example.com//. If both basepath and path are "/", returns
     * an empty string
     * @param basePath
     * @param path
     * @return Adjusted path
     */
    private String removeEmptyPath(String basePath, String path) {
        if (basePath.equals("/")) {
            basePath = "";
        } else if (path.equals("/")) {
            path = "";
        }
        return basePath + path;
    }
}
