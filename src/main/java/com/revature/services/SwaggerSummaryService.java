package com.revature.services;

import com.revature.models.SwaggerSummary;
import com.revature.repositories.SwaggerSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SwaggerSummaryService {

    @Autowired
    private SwaggerSummaryRepository repository;
    
    public SwaggerSummary insert() {
        
        SwaggerSummary s = new SwaggerSummary();
        
        return repository.save(s);
        
    }

    public void update(SwaggerSummary s) {
        
        repository.save(s);
        
    }
    
}
