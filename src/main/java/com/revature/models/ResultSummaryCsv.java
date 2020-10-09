package com.revature.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class ResultSummaryCsv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private byte[] byteCsv;
    
    @Override
    public String toString() {
        return "ResultSummaryCsv [id=" + id + "]";
    }
    
}
