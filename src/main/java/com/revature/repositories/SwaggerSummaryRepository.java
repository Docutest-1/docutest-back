package com.revature.repositories;

import com.revature.models.SwaggerSummary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwaggerSummaryRepository extends CrudRepository<SwaggerSummary, Integer> {
    
    SwaggerSummary findById(int id);
    
}
