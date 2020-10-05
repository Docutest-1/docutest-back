package com.revature.responsecollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

public class JMeterResponseCollector extends ResultCollector { 
    
    private static final long serialVersionUID = -4468726154521336732L;
    private List<Long> responseTimes = new ArrayList<>();
    private long responseMax = 0;
    
    private long firstSampleStartTime = 0;
    private long sampleStartTime;
    
    // count of number of nXX status codes
    private int[] statusCodeCount = new int[5];
    
    public JMeterResponseCollector(Summariser summer) {
        super(summer);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e);
        SampleResult r = e.getResult();
        if (firstSampleStartTime == 0) {
            firstSampleStartTime = r.getStartTime();
        }
        sampleStartTime = r.getStartTime();
        long responseTime = r.getEndTime() - r.getStartTime();
        responseTimes.add(responseTime);
        if (responseTime > responseMax) {
            responseMax = responseTime;
        }
        
        char temp = r.getResponseCode().charAt(0);
        int respType = Character.getNumericValue(temp);
        // -1 since indices start at 0
        statusCodeCount[respType - 1]++;
    }
    
    public double getsuccessFailPercentage() {
        double ratio = 0;
        if (statusCodeCount[1] + statusCodeCount[3] + statusCodeCount[4] != 0) {
            ratio = ((double) statusCodeCount[1]) / (statusCodeCount[1] + statusCodeCount[3] + statusCodeCount[4]);
        }
        return ratio;
    }
    
    public long getResponseAvg() {
        long length = responseTimes.size();
        long sum = 0;
        for (long lat : responseTimes) {
            sum += lat;
        }
        return sum / length;
    }
    
    public long getResponse50Percentile() {
        Collections.sort(responseTimes);

        int middle = (int) Math.round(responseTimes.size() * 0.5);
        return responseTimes.get(middle - 1);
    }
    
    public long getResponse25Percentile() {
        Collections.sort(responseTimes);

        int split = (int) Math.round(responseTimes.size() * 0.25);
        return responseTimes.get(split - 1);
    }
    
    public long getResponse75Percentile() {
        Collections.sort(responseTimes);
        int split = (int) Math.round(responseTimes.size() * 0.75);
        return responseTimes.get(split - 1);
    }
    
    public double getReqPerSec() {
        long duration = sampleStartTime - firstSampleStartTime;
        double reqPerSec = 0;
        if (duration != 0) {
            reqPerSec = 1000 * ((double) responseTimes.size()) / duration;
        }
        return reqPerSec;
    }

    public List<Long> getResponseTimes() {
        return responseTimes;
    }

    public long getResponseMax() {
        return responseMax;
    }

    public long getStartTime() {
        return firstSampleStartTime;
    }

    public long getCurrentTime() {
        return sampleStartTime;
    }


    public int getNum1XX() {
        return statusCodeCount[0];
    }

    public int getNum2XX() {
        return statusCodeCount[1];
    }

    public int getNum3XX() {
        return statusCodeCount[2];
    }

    public int getNum4XX() {
        return statusCodeCount[3];
    }

    public int getNum5XX() {
        return statusCodeCount[4];
    }

    public void setResponseTimes(List<Long> latencyTimes) {
        this.responseTimes = latencyTimes;
    }

    public void setResponseMax(long responseMax) {
        this.responseMax = responseMax;
    }

    public void setStartTime(long startTime) {
        this.firstSampleStartTime = startTime;
    }

    public void setCurrentTime(long currentTime) {
        this.sampleStartTime = currentTime;
    }
}
