package com.revature.templates;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @EqualsAndHashCode
public class LoadTestConfig {
    public String testPlanName = "";
    public int loops;
    public int duration;
    public int threads;
    public int rampUp;
}
