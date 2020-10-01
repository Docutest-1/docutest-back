package com.revature.classtest;

import javax.annotation.PostConstruct;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class to implement transactional integration tests using the root application configuration.
 * <p>
 * Created by patrik.mihalcin on 3.4.2017.
 */
@RunWith(SpringRunner.class)
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    public static ApplicationContext context;

    @PostConstruct
    public static void dataSourceInfo() {
        DataSource ds = context.getBean(DataSource.class);
        PoolConfiguration poolProperties = ds.getPoolProperties();
        String url = poolProperties.getUrl();
        String driverClassName = poolProperties.getDriverClassName();
        String username = poolProperties.getUsername();
        String password = poolProperties.getPassword();
        System.out.println("DataSource info -> URL: " + url + ", driver: " + driverClassName + ", username: " + username + ", password: " + password);
    }

}