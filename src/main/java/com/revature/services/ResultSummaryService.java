package com.revature.services;

import com.revature.models.ResultSummary;
import com.revature.models.SwaggerSummary;
import com.revature.repositories.ResultSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResultSummaryService {

    @Autowired
    private ResultSummaryRepository repository;

    public ResultSummaryService(ResultSummaryRepository mockedDao) {
        this.repository = mockedDao;
    }

    public ResultSummary insert(ResultSummary rs) {
        return repository.save(rs);
    }

    public boolean update(ResultSummary s) {
        ResultSummary saved = repository.save(s);
        if (saved != null && saved.equals(s)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean delete(ResultSummary s) {
        repository.delete(s);
        if (repository.existsById(s.getId()) == false) {
            return true;
        } else {
            return false;
        }
    }

    public ResultSummary getById(int id) {
        return repository.findById(id);
    }

}
