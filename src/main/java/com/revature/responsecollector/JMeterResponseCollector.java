package com.revature.responsecollector;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

public class JMeterResponseCollector extends ResultCollector { 
    
    private static final long serialVersionUID = 1L;
    
    public JMeterResponseCollector(Summariser summer) {
        super(summer);
    }
    
    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e);
        SampleResult r = e.getResult();
        System.out.println("event found");
        if (r.isSuccessful()) {
            System.out.println("Response time in milliseconds: " + r.getTime());
        }
    }

}
