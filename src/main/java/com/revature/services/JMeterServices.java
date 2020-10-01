package com.revature.services;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;

@Service
public class JMeterServices {

    // TODO REMOVE TEMP
    public static final int TEMP_DURATION = 10;

    private HashTree hashTree = new HashTree();

    /**
     * Runs the JMeter test using a Swagger object
     * @param swag Input Swagger object
     * @param testConfig LoadTestConfig object with test settings
     * @param propertiesPath Filepath to the properties JMeter Properties file
     * @return True if test runs successfully, false if exception is thrown during the test.
     */
    public boolean loadTesting(Swagger swag, LoadTestConfig testConfig, String propertiesPath) {
        StandardJMeterEngine jm = new StandardJMeterEngine();

        JMeterUtils.loadJMeterProperties(propertiesPath);
        JMeterUtils.initLogging();
        JMeterUtils.initLocale();

        Set<HTTPSampler> httpSampler = this.createHTTPSampler(swag);

        TestElement loopCtrl = null;
        if (testConfig.loops == 0) {
            // TODO implement time duration
        } else {
            loopCtrl = this.createLoopController(httpSampler, testConfig.loops);

        }

        SetupThreadGroup threadGroup = this.createLoad((LoopController) loopCtrl, testConfig.threads, testConfig.rampUp,
                testConfig.duration);

        TestPlan testPlan = new TestPlan(testConfig.testPlanName);

        hashTree.add("testPlan", testPlan);
        hashTree.add("loopCtrl", loopCtrl);
        hashTree.add("threadGroup", threadGroup);

        jm.configure(hashTree);

        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        String logFile = "/temp/temp/file.jtl";
        JMeterResponseCollector logger = new JMeterResponseCollector(summer);
        logger.setFilename(logFile);
        hashTree.add(hashTree.getArray()[0], logger);

        jm.run();
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

                    this.hashTree.add("httpSampler", element);
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
     * @param fullPath
     * @param verbs : map containing HttpMethod and Operation pairs
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
     * Adds each element in the HTTPSampler set as a test element to the loop
     * controller. Returns null if httpSampler is null or has no elements.
     * @param httpSamplers Set of httpsamplers to iterate add to the loop controller.
     * @param n Number of iterations
     * @return Array of LoopController objects based on the httpSamplers
     */
    public TestElement createLoopController(Set<HTTPSampler> httpSampler, int n) {
        TestElement loopCtrl = new LoopController();

        if (httpSampler != null && httpSampler.size() > 0) {
            ((LoopController) loopCtrl).setFirst(true);
            ((LoopController) loopCtrl).setLoops(n);

            for (HTTPSampler element : httpSampler) {
                loopCtrl.addTestElement(element);
            }

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
    public SetupThreadGroup createLoad(LoopController loopController, int threads, int rampUp, int duration) {
        if (loopController == null) {
            return null;
        }

        SetupThreadGroup ret = new SetupThreadGroup();

        ret.setNumThreads(threads);
        ret.setRampUp(rampUp);
        ret.setDuration(duration);
        ret.setSamplerController(loopController); // needs to not be null

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
    public HashTree createTestConfig(String testPlanName, LoopController loopController,
            SetupThreadGroup threadGroup) {
        // init hashtree
        HashTree jmConfig = new HashTree();
        TestPlan testPlan = new TestPlan("testPlanName");

        jmConfig.add("TestPlan", testPlan);
        jmConfig.add("LoopController", loopController);
        jmConfig.add("ThreadGroup", threadGroup);

        return jmConfig;

    }

}
