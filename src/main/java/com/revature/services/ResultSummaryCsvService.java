package com.revature.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

import com.opencsv.CSVWriter;
import com.revature.models.ResultSummaryCsv;
import com.revature.repositories.ResultSummaryCsvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResultSummaryCsvService {

    @Autowired
    private ResultSummaryCsvRepository repository;

    public ResultSummaryCsvService() {
    }
    
    public ResultSummaryCsvService(ResultSummaryCsvRepository mockedDao) {
        this.repository = mockedDao;
    }

    public CSVWriter createWriter(ByteArrayOutputStream stream) {
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        
        return new CSVWriter(streamWriter);
    }
    
    public Optional<ResultSummaryCsv> getById(int id) {
        return repository.findById(id);
    }

    public ResultSummaryCsv createCSV(CSVWriter writer) {
        String[] header = { "startTime (epoch time [ms])", "responseTime [ms]", "responseCode" }; 
        writer.writeNext(header); 
        
        ResultSummaryCsv s = new ResultSummaryCsv();
        
        return repository.save(s);
    }
    
    public boolean update(ResultSummaryCsv s) {
        ResultSummaryCsv saved = repository.save(s);
        return saved.equals(s);
    }
}
