package com.revature.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revature.models.ResultSummary;
import com.revature.models.SwaggerSummary;
import com.revature.responsecollector.JMeterResponseCollector;
import com.revature.templates.LoadTestConfig;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JMeterService {

    // Object representing the config for the JMeter test
    // At the very least, requires a TestPlan, HTTPSampler, and ThreadGroup
    // Test Elements can be nested within each other
    private HashTree hashTree = new HashTree();

    // replace user with username later
    public static final String BASE_FILE_PATH = "./datafiles/user_";
    
    public static final String PROPERTIES_PATH = "src/test/resources/test.properties";

    private LoadTestConfig testConfig = new LoadTestConfig();
    
    @Autowired
    private SwaggerSummaryService sss;

    /**
     * Runs the JMeter test using a Swagger object, test configuration, and JMeter properties path.
     * If both duration and number of loops are set, duration takes precedence. If the Swagger file does not
     * have any endpoints/HTTP methods, the threads still start up and run, but don't make any requests.
     * @param swag           Input Swagger object
     * @param testConfig     LoadTestConfig object with test settings
     * @param propertiesPath File path to the properties JMeter Properties file
     */
    public void loadTesting(Swagger swag, LoadTestConfig testConfig, int swaggerSummaryId) {
        Set<ResultSummary> resultSummaries = new HashSet<>();
        
        this.testConfig = testConfig;
        StandardJMeterEngine jm = new StandardJMeterEngine();

        JMeterUtils.loadJMeterProperties(PROPERTIES_PATH);
        JMeterUtils.initLocale();

        // create set of all unique HTTP requests as defined in swagger
        Set<HTTPSampler> httpSampler = this.createHTTPSampler(swag);

        int reqNumber = 0;

        // run a separate load test for each req since we want individual CSV/summaries for each
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
            // Definitely need to change if we want multiple users to run multiple tests at once
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
        
        SwaggerSummary swaggerSummary = sss.getById(swaggerSummaryId);
        swaggerSummary.setResultsummaries(resultSummaries);
        sss.update(swaggerSummary);
        
    }

    /**
     * For OAS 2.0. Parses HTTP request conditions from swagger file and generates
     * an array of HTTPSampler objects based on host, basepath, paths, endpoints,
     * and HTTP verbs
     *
     * @param input Swagger/OpenAPIv2 file input
     * @return Set of HTTPSampler objects. Returns an empty set if there are no
     *         endpoints. Returns null if there is a problem with the Swagger input.
     */
    public Set<HTTPSampler> createHTTPSampler(Swagger input) {
        Set<HTTPSampler> httpSamplers = new HashSet<>();

        try {
            String host = input.getHost();

            // trim, remove "
            host = host.trim();
            host = host.replace("\"", "");

            String[] splitHost = host.split(":");
            String basePath = input.getBasePath();
            Map<String, Path> endpoints = input.getPaths();

            // each path
            for (Map.Entry<String, Path> entry : endpoints.entrySet()) {
                String path = entry.getKey();
                Path pathOperations = entry.getValue();
                Map<HttpMethod, Operation> verbs = pathOperations.getOperationMap();

                // each verb/operation
                for (HttpMethod verb : verbs.keySet()) {
                    HTTPSampler element = new HTTPSampler();

                    // domain
                    element.setDomain(splitHost[0]);
                    // port
                    element.setPort(Integer.parseInt(splitHost[1]));

                    // path
                    if (basePath.equals("/")) {
                        basePath = "";
                    }

                    String fullPath = basePath + path;
                    String parsedURL = this.parseURL(fullPath, verbs);
                    element.setPath(parsedURL);
                    element.setMethod(verb.toString());
                    element.setFollowRedirects(true);

                    httpSamplers.add(element);
                }
            }
        } catch (NullPointerException e) {
            // return empty set in case of missing params
            // TODO log
            e.printStackTrace();
            return new HashSet<>();
            
        // problem parsing
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
        return httpSamplers;
    }

    /**
     * Parses URL and inserts path parameters if exists
     * @param fullPath
     * @param verbs    : map containing HttpMethod and Operation pairs
     * @return a URL containing inserted parameter
     */
    public String parseURL(String fullPath, Map<HttpMethod, Operation> verbs) {
        for (Map.Entry<HttpMethod, Operation> entry : verbs.entrySet()) {
            List<Parameter> parameters = entry.getValue().getParameters();
            for (Parameter p : parameters) {
                if (p.getIn().equals("path")) {
                    PathParameter pathParam = (PathParameter) p;
                    if (pathParam.getType().equals("integer")) {
                        // TODO Full implementation
                        // currently replaced params with 1
                        fullPath = fullPath.replace("{" + pathParam.getName() + "}", "1");
                    }
                }
            }
        }

        return fullPath;
    }

    /**
     * Configures a LoopController with the given loop count and httpSampler.
     * Returns null if httpSampler is null.
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
     * Creates a thread group (specifically a SetupThreadGroup object) with the given parameters.
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
}
