package com.revature.services;

import com.revature.models.ResultSummary;
import com.revature.repositories.ResultSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResultSummaryService {
    
    @Autowired
    private ResultSummaryRepository repository;
    
    public ResultSummary insert(ResultSummary rs) {
        
        return repository.save(rs);
        
    }

}
