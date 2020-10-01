package com.revature.classtest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.revature.docutest.DocutestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DocutestApplication.class)
public class SpringBootJPAIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private GenericEntityRepository genericEntityRepository;
    
    @Test
    public void givenGenericEntityRepository_whenSaveAndRetreiveEntity_thenOK() {
        GenericEntity genericEntity = genericEntityRepository.save(new GenericEntity("test"));
        GenericEntity foundEntity = genericEntityRepository.findOne(genericEntity.getId());
 
        assertNotNull(foundEntity);
        assertEquals(genericEntity.getValue(), foundEntity.getValue());
    }

}
