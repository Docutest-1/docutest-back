package com.revature.models;

import java.util.List;

/**
 * Model representing a single specification file upload. Data for load testing is embedded in each Request object.
 */
public interface Docutest {
    
    public int getId();
    
    public void setId(int id);
    
    public List<Request> getRequests();

}
