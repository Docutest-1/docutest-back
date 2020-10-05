package com.revature.repositories;

import com.revature.models.ResultSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultSummaryRepository extends JpaRepository<ResultSummary, Integer> {
    
    ResultSummary findById(int id);
    
}
