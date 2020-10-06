package com.revature.services;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import com.revature.models.SwaggerSummary;
import com.revature.repositories.SwaggerSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class SwaggerSummaryService {

    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private SwaggerSummaryRepository repository;
    
    public SwaggerSummary insert() {
        
        SwaggerSummary s = new SwaggerSummary();
        
        return repository.save(s);
        
    }

    public void update(SwaggerSummary s) {
        
        repository.save(s);
        
    }
    
    public SwaggerSummary getById(int id) throws EntityNotFoundException {
        SwaggerSummary s = repository.findById(id);
        
        return s;
    }
    
}
