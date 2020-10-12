package com.revature.services;

import com.revature.models.ResultSummary;
import com.revature.repositories.ResultSummaryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for interfacing with the ResultSummaryRepository
 */
@Service
@Transactional
public class ResultSummaryService {
    private static final Logger log = LogManager.getLogger(ResultSummaryService.class);

    @Autowired
    private ResultSummaryRepository repository;

    public ResultSummaryService(ResultSummaryRepository mockedDao) {
        this.repository = mockedDao;
    }

    public ResultSummary insert(ResultSummary rs) {
        log.info("insert() CALLED FOR ResultSummary ID: {}", rs.getId());
        return repository.save(rs);
    }

    public boolean update(ResultSummary s) {
        ResultSummary saved = repository.save(s);
        return saved.equals(s);
    }

    public boolean delete(ResultSummary s) {
        repository.delete(s);
        return !repository.existsById(s.getId());

    }

    public ResultSummary getById(int id) {
        return repository.findById(id);
    }

}
