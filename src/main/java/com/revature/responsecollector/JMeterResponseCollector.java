package com.revature.responsecollector;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

public class JMeterResponseCollector extends ResultCollector { 
    
    private static final long serialVersionUID = 1L;
    private int failCount = 0;
    private int okResponse = 0;
    private ArrayList<Long> latencyTimes = new ArrayList<Long>();
    private long responseMax = 0;
    
    public JMeterResponseCollector(Summariser summer) {
        super(summer);
    }
    
    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e);
        SampleResult r = e.getResult();
        long latency = r.getLatency();
        latencyTimes.add(latency);
        if (latency > responseMax) {
            responseMax = latency;
        }
        if (r.getResponseCode().charAt(0) == 4 || r.getResponseCode().charAt(0) == 5) {
            failCount++;
            System.out.println("4XX/5XX");
        }
        if (r.getResponseCode().charAt(0) == 2) {
            okResponse++;
            System.out.println("2XX");
        }
        
    }
    
    public float getsuccessFailPercentage() {
        float ratio = 100;
        if (failCount != 0) {
            ratio = okResponse / failCount;
        } 
        return ratio;
    }
    
    public long getResponseAvg() {
        long length = latencyTimes.size();
        long sum = 0;
        for (long lat : latencyTimes) {
            sum += lat;
        }
        long avg = sum / length;
        return avg;
    }
    
    public long getResponse50Percentile() {
        Collections.sort(latencyTimes);

        int middle = latencyTimes.size() / 2;
        return latencyTimes.get(middle);
    }
    
    public long getResponse25Percentile() {
        Collections.sort(latencyTimes);

        int split = latencyTimes.size() / 4;
        return latencyTimes.get(split);
    }
    
    public long getResponse75Percentile() {
        Collections.sort(latencyTimes);

        int split = latencyTimes.size() * 3 / 4;
        return latencyTimes.get(split);
    }
}
