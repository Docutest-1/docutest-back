package com.revature.services;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revature.responsecollector.JMeterResponseCollector;
import com.revature.templates.LoadTestConfig;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.RunTime;
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
public class JMeterServices {

    private HashTree hashTree = new HashTree();

    /**
     * Runs the JMeter test using a Swagger object, test configuration, and JMeter
     * properties path.
     * @param swag           Input Swagger object
     * @param testConfig     LoadTestConfig object with test settings
     * @param propertiesPath File path to the properties JMeter Properties file
     */
    public void loadTesting(Swagger swag, LoadTestConfig testConfig, String propertiesPath) {
        StandardJMeterEngine jm = new StandardJMeterEngine();

        JMeterUtils.loadJMeterProperties(propertiesPath);
        // JMeterUtils.initLogging();
        JMeterUtils.initLocale();

        Set<HTTPSampler> httpSampler = this.createHTTPSampler(swag);

        // TODO replace
        int temp = 0;
        for (HTTPSampler element : httpSampler) {
            TestElement logicController = null;

            logicController = createLoopController(element, testConfig.loops);

            SetupThreadGroup threadGroup = this.createLoad((LoopController) logicController, testConfig.threads,
                    testConfig.rampUp);

            TestPlan testPlan = new TestPlan(testConfig.testPlanName);
            testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());

            hashTree.add("testPlan", testPlan);
            hashTree.add("setupThreadGroup", threadGroup);
            hashTree.add("httpSampler", element);

            jm.configure(hashTree);

            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }

            String logFile = "/temp/temp/file" + temp + ".csv";
            temp++;
            JMeterResponseCollector logger;
            if (testConfig.duration > 0) {
                logger = new JMeterResponseCollector(summer, jm, testConfig.duration);
            } else {
                logger = new JMeterResponseCollector(summer);
            }
            logger.setFilename(logFile);
            hashTree.add(hashTree.getArray()[0], logger);

            try {
                jm.run();

            } catch (Exception e) {
                // TODO log
                e.printStackTrace();
            }
        }
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
        // TODO test

        Set<HTTPSampler> httpSamplers = new HashSet<>();

        try {
            String host = input.getHost();

            // trim, remove "
            host = host.trim();
            host = host.replaceAll("\"", "");

            String[] splitHost = host.split(":");
            String basePath = input.getBasePath();
            Map<String, Path> endpoints = input.getPaths();

            // each path
            for (String path : endpoints.keySet()) {
                Path pathOperations = endpoints.get(path);
                Map<HttpMethod, Operation> verbs = pathOperations.getOperationMap();

                // each verb/operation
                for (HttpMethod verb : verbs.keySet()) {
                    HTTPSampler element = new HTTPSampler();

                    // domain
                    element.setDomain(splitHost[0]);
                    try {
                        // port
                        element.setPort(Integer.parseInt(splitHost[1]));
                    } catch (NumberFormatException e) {
                        return null;
                    } catch (IndexOutOfBoundsException e) {
                        return null;
                    }

                    // path
                    if (basePath.equals("/")) {
                        basePath = "";
                    }
                    String fullPath = basePath + path;
                    System.out.println("fullPath: " + fullPath);

                    String parsedURL = this.parseURL(fullPath, verbs);
                    System.out.println(parsedURL);

                    element.setPath(basePath + path);
                    // http verb
                    element.setMethod(verb.toString());

                    httpSamplers.add(element);
                }
            }
        } catch (NullPointerException e) {
            // return empty set in case of missing params
            // TODO log

            return new HashSet<HTTPSampler>();
        }

        return httpSamplers;
    }

    /**
     * Parses URL and inserts path parameters if exists
     *
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
                        fullPath = fullPath.replace("{" + pathParam.getName() + "}", "1");
                    }
                }
            }
        }

        return fullPath;
    }

    /**
     * Configures a LoopController with the given loop count and httpSampler.
     * Alternative to createRunTimeController. Returns null if httpSampler is null.
     *
     * @param httpSampler HTTPSampler object representing a request
     * @param loops       Number of iterations
     * @return Covariant LoopController object.
     */
    public TestElement createLoopController(HTTPSampler httpSampler, int loops) {
        if (httpSampler != null) {
            TestElement loopCtrl = new LoopController();
            // ((LoopController) loopCtrl).setFirst(true);
            ((LoopController) loopCtrl).setLoops(loops);
            loopCtrl.addTestElement(httpSampler);
            return loopCtrl;
        }
        return null;
    }

    /**
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

        ret.setNumThreads(threads);
        ret.setRampUp(rampUp);
        ret.setSamplerController(controller); // needs to not be null

        return ret;
    }

    // May want a separate method for setting up spike tests?

    // might get rid of this
    /**
     *
     * @param testPlanName
     * @param httpSampler
     * @param loopController
     * @param threadGroup
     * @return hashtree for use with StandardJMeterEngine
     */
    public HashTree createTestConfig(String testPlanName, LoopController loopController, SetupThreadGroup threadGroup) {
        // init hashtree
        HashTree jmConfig = new HashTree();
        TestPlan testPlan = new TestPlan("testPlanName");

        jmConfig.add("TestPlan", testPlan);
        jmConfig.add("LoopController", loopController);
        jmConfig.add("ThreadGroup", threadGroup);

        return jmConfig;

    }

}
