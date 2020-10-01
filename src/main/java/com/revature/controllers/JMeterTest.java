package com.revature.controllers;

import java.io.File;
import java.io.IOException;

import com.revature.responsecollector.JMeterResponseCollector;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class JMeterTest {

    public static void main(String[] args) throws IOException {
        // Engine
        StandardJMeterEngine jm = new StandardJMeterEngine();
        // jmeter.properties
        JMeterUtils.loadJMeterProperties("C:/Users/julie/scoop/apps/jmeter/5.3/bin/jmeter.properties");
        
        JMeterUtils.setJMeterHome("C:/Users/julie/scoop/apps/jmeter");
        JMeterUtils.initLocale();
        
//        SaveService.loadProperties();
//
//        SaveService.loadTree(new File("C:/Users/julie/scoop/apps/jmeter/5.3/extras/Test.jmx"));
        
        HashTree hashTree = new HashTree();     

        // HTTP Sampler
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("www.google.com");
        httpSampler.setPort(80);
        httpSampler.setPath("/");
        httpSampler.setMethod("GET");

        // Loop Controller
        TestElement loopCtrl = new LoopController();
        ((LoopController) loopCtrl).setLoops(1);
        ((LoopController) loopCtrl).addTestElement(httpSampler);
        ((LoopController) loopCtrl).setFirst(true);

        // Thread Group
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(3);
        threadGroup.setRampUp(1);
        threadGroup.setSamplerController((LoopController) loopCtrl);

        // Test plan
        TestPlan testPlan = new TestPlan("MY TEST PLAN");

        hashTree.add("testPlan", testPlan);
        hashTree.add("loopCtrl", loopCtrl);
        hashTree.add("threadGroup", threadGroup);
        hashTree.add("httpSampler", httpSampler);       

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
}
