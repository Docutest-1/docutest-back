package com.revature.services;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class JMeterServices {
    StandardJMeterEngine jm = new StandardJMeterEngine();

    TestPlan testPlan = new TestPlan("MY TEST PLAN");

    public HTTPSampler getHttpSampler(String domain, int port, String path, String method) {
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain(domain);
        httpSampler.setPort(port);
        httpSampler.setPath(path);
        httpSampler.setMethod(method);
        return httpSampler;
    }

    HashTree hashTree = new HashTree();

    public TestElement getTestElement(int loops, HTTPSampler httpSampler) {
        TestElement loopCtrl = new LoopController();
        ((LoopController) loopCtrl).setLoops(loops);
        ((LoopController) loopCtrl).addTestElement(httpSampler);
        ((LoopController) loopCtrl).setFirst(true);
        return loopCtrl;
    }

    public SetupThreadGroup getThreadGroup(int threads, int rampUp, TestElement loopCtrl) {
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(threads);
        threadGroup.setRampUp(rampUp);
        threadGroup.setSamplerController((LoopController) loopCtrl);// this or time duration
        return threadGroup;
    }

}
