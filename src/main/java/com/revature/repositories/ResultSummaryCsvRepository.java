package com.revature.repositories;

import com.revature.models.ResultSummaryCsv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultSummaryCsvRepository extends JpaRepository<ResultSummaryCsv, Integer> {
    
    ResultSummaryCsv findById(int id);
    
}
