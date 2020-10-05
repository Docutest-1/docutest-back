package com.revature.repositories;

import com.revature.models.ResultSummary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultSummaryRepository extends CrudRepository<ResultSummary, Integer> {
    
    ResultSummary findById(int id);
    
}
