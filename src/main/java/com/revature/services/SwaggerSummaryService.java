package com.revature.services;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import com.revature.models.SwaggerSummary;
import com.revature.models.SwaggerUploadResponse;
import com.revature.repositories.SwaggerSummaryRepository;
import com.revature.templates.LoadTestConfig;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
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

    public SwaggerSummary getById(int id) {
        return repository.findById(id);
    }

    public SwaggerUploadResponse uploadSwaggerfile(Swagger swag, LoadTestConfig ltc) {
        SwaggerUploadResponse sur = new SwaggerUploadResponse();
        SwaggerSummary s = this.insert();

        s.setDuration(ltc.getDuration());
        s.setFollowRedirects(ltc.isFollowRedirects());
        s.setLoops(ltc.getLoops());
        s.setRampUp(ltc.getRampUp());
        s.setTestPlanName(ltc.getTestPlanName());
        s.setThreads(ltc.getThreads());

        sur.setSwaggerSummaryId(s.getId());
        sur.setResultRef("Docutest/swaggersummary/" + s.getId());
        int endPointCount = 0;
        Map<String, Path> endpoints = swag.getPaths();
        for (Map.Entry<String, Path> entry : endpoints.entrySet()) {
            Path pathOperations = entry.getValue();
            Map<HttpMethod, Operation> verbs = pathOperations.getOperationMap();
            endPointCount = verbs.keySet().size();
        }

        if (ltc.getLoops() > 0) {
            long singleTest = (ltc.getRampUp() + ltc.getLoops() * 500 + 5000);
            sur.setEta(System.currentTimeMillis() + (singleTest * endPointCount));
        }
        if (ltc.getDuration() != 0) {
            long singleTest = (ltc.getDuration() * 1000 + 5000);
            sur.setEta(System.currentTimeMillis() + (singleTest * endPointCount));
        }
        return sur;

    }

}
