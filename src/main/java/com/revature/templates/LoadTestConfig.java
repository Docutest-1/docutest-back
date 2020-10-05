package com.revature.templates;

import lombok.Data;

@Data
public class LoadTestConfig {
    private String testPlanName = "";
    private int loops;
    private int duration;
    private int threads;
    private int rampUp;
    private boolean followRedirects = true;
}
