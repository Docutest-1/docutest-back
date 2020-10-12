package com.revature.ordering;

import java.util.Comparator;

import com.revature.models.Request;

public class RequestComparator implements Comparator<Request> {

    @Override
    public int compare(Request o1, Request o2) {
        // order by endpoint, then by verb
        
        if (o1.getEndpoint().equals(o2.getEndpoint())) {
            // if endpoints are the same
            // order of Http method enum is
            // POST -> GET -> PATCH -> PUT -> DELETE -> HEAD -> OPTIONS
            // we can just sort using indices
            
            return (o1.getVerb().ordinal() - o2.getVerb().ordinal());
            
        }
        
        return o1.getEndpoint().compareTo(o2.getEndpoint());

    }

}
