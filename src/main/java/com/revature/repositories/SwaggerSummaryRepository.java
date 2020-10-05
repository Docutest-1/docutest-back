package com.revature.repositories;

import com.revature.models.SwaggerSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwaggerSummaryRepository extends JpaRepository<SwaggerSummary, Integer> {
    
    SwaggerSummary findById(int id);
    
}
