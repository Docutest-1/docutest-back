package com.revature.responsecollector;

import java.security.Timestamp;
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
    
    private long startTime = 0;
    private long currentTime;
    
    public JMeterResponseCollector(Summariser summer) {
        super(summer);
    }
    

    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e);
        SampleResult r = e.getResult();
        if (startTime == 0) {
            startTime = r.getStartTime();
        }
        currentTime = r.getStartTime();
        long latency = r.getLatency();
        latencyTimes.add(latency);
        if (latency > responseMax) {
            responseMax = latency;
        }
        if (r.getResponseCode().charAt(0) == '4' || r.getResponseCode().charAt(0) == '5') {
            failCount += 1;
        }
        if (r.getResponseCode().charAt(0) == '2') {
            okResponse += 1;
        }
        
    }
    
    public float getsuccessFailPercentage() {
        float ratio = 0;
        if (latencyTimes.size() != 0) {
            ratio = okResponse / latencyTimes.size();
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
    
    public long getReqPerSec() {
        long duration = (currentTime - startTime) / 1000;
        long reqPerSec = 0;
        if (duration != 0) {
            reqPerSec = latencyTimes.size() / duration;
        }
        return reqPerSec;
    }



    public int getFailCount() {
        return failCount;
    }



    public int getOkResponse() {
        return okResponse;
    }



    public ArrayList<Long> getLatencyTimes() {
        return latencyTimes;
    }



    public long getResponseMax() {
        return responseMax;
    }



    public long getStartTime() {
        return startTime;
    }



    public long getCurrentTime() {
        return currentTime;
    }


    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }


    public void setOkResponse(int okResponse) {
        this.okResponse = okResponse;
    }


    public void setLatencyTimes(ArrayList<Long> latencyTimes) {
        this.latencyTimes = latencyTimes;
    }


    public void setResponseMax(long responseMax) {
        this.responseMax = responseMax;
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
    
    
}
