package com.revature.services;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.revature.templates.LoadTestConfig;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class JMeterServices {
    
    // TODO REMOVE TEMP
    public static final int TEMP_DURATION = 10;
    
    private HashTree hashTree;

    public void loadTesting(Swagger swag, LoadTestConfig ltg) {
        StandardJMeterEngine jm = new StandardJMeterEngine();

        // WHERE IS THIS FILE?
        //JMeterUtils.loadJMeterProperties("../jmeter.properties");
//        JMeterUtils.initLogging();
//        JMeterUtils.initLocale();

        Set<HTTPSampler> httpSampler = this.createHTTPSampler(swag);

        TestElement loopCtrl = null;
        if (ltg.loops == 0) {
            // TODO implement time duration
        } else {
            loopCtrl = this.createLoopController(httpSampler, ltg.loops);

        }

        SetupThreadGroup threadGroup = this.createLoad((LoopController) loopCtrl, ltg.threads, ltg.rampUp, TEMP_DURATION);

        TestPlan testPlan = new TestPlan("MY TEST PLAN");
        
        hashTree.add("testPlan", testPlan);
        hashTree.add("loopCtrl", loopCtrl);
        hashTree.add("threadGroup", threadGroup);

        jm.configure(hashTree);

        // ResultCollector class tracks results

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
                }
                // path
                String fullPath = basePath + path;
                // TODO parse path for path var
                element.setPath(basePath + path);
                // http verb
                element.setMethod(verb.toString());
                
                this.hashTree.add(element);
                httpSamplers.add(element);
            }
        }
        

        return httpSamplers;
    }

    /**
     * Adds each element in the HTTPSampler set as a test element to the loop controller
     * @param httpSamplers
     * @param n Number of iterations
     * @return Array of LoopController objects based on the httpSamplers
     *         http://svn.apache.org/repos/asf/jmeter/tags/v2_3_2/docs/api/org/apache/jmeter/control/LoopController.html
     */
    public TestElement createLoopController(Set<HTTPSampler> httpSampler, int n) {
        // TODO implement
        // TODO null checks

        TestElement loopCtrl = new LoopController();
        ((LoopController) loopCtrl).setFirst(true);
        ((LoopController) loopCtrl).setLoops(n);

        for (HTTPSampler element : httpSampler) {
            loopCtrl.addTestElement(element);
        }
        
        return loopCtrl;
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
        // TODO implement
        SetupThreadGroup ret = new SetupThreadGroup();

        ret.setRampUp(rampUp);
        ret.setDuration(duration);
        ret.setSamplerController(loopController);

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
    public HashTree createTestConfig(String testPlanName, HTTPSampler httpSampler, LoopController loopController,
            ThreadGroup threadGroup) {
        // init hashtree
        HashTree jmConfig = new HashTree();
        TestPlan testPlan = new TestPlan("testPlanName");

        jmConfig.add("TestPlan", testPlan);
        jmConfig.add("LoopController", loopController);
        jmConfig.add("ThreadGroup", threadGroup);
        jmConfig.add("HTTPSampler", httpSampler);

        return jmConfig;

    }

    public void runTest(HashTree testConfig) {
        StandardJMeterEngine jmRunner = new StandardJMeterEngine();
        jmRunner.configure(testConfig);

        String summaryName = "";
        Summariser summary = new Summariser(summaryName);

    }

}
