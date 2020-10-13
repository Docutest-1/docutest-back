package com.revature.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.annotation.PostConstruct;

import com.opencsv.CSVWriter;
import com.revature.models.Docutest;
import com.revature.models.Request;
import com.revature.models.ResultSummary;
import com.revature.models.ResultSummaryCsv;
import com.revature.models.SwaggerSummary;
import com.revature.ordering.RequestComparator;
import com.revature.responsecollector.JMeterResponseCollector;
import com.revature.templates.LoadTestConfig;
import io.swagger.models.HttpMethod;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JMeterService {
    private static Logger log = LogManager.getLogger(JMeterService.class);

    // Object representing the config for the JMeter test
    // At the very least, requires a TestPlan, HTTPSamplerProxy, and ThreadGroup
    // Test Elements can be nested within each other
    private HashTree hashTree = new HashTree();

    // replace user with username later
    public static final String BASE_FILE_PATH = "./datafiles/user_";

    public static final String PROPERTIES_PATH = "/jmeter.properties";
    
    public static final String S3_DOMAIN = "https://docutestbucket.s3.us-east-2.amazonaws.com";

    private LoadTestConfig testConfig = new LoadTestConfig();

    @Autowired
    private SwaggerSummaryService sss;

    @Autowired
    private ResultSummaryCsvService rscs;
    
    @Autowired
    private ResultSummaryService rss;
    
    @Autowired
    private S3Service s3Service;
    
    @PostConstruct
    private void loadProperties() {
        InputStream inputStream = getClass().getResourceAsStream(PROPERTIES_PATH);
        Properties p = new Properties();
        
        try {
            Field field = JMeterUtils.class.getDeclaredField("appProperties");
            field.setAccessible(true);
            p.load(inputStream);
            field.set(this, p);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException ex) {
            log.error("EXCEPTION THROWN WHILE ATTEMPTING TO SET JMETER PROPERTIES");
            log.trace("STACK TRACE: ", ex);
        }
        
        JMeterUtils.initLocale();
    }
    
    /**
     * Runs the JMeter test using a Docutest object, test configuration, and JMeter
     * properties path. If both duration and number of loops are set, duration takes
     * precedence. If the requests field of the Docutest object is empty or null,
     * the threads still start up and run, but won't make any requests.
     * 
     * @param input          Input Docutest object
     * @param testConfig     LoadTestConfig object with test settings
     * @param propertiesPath File path to the properties JMeter Properties file
     */
    @Transactional
    public void loadTesting(Docutest input, LoadTestConfig testConfig, int swaggerSummaryId) {

        this.testConfig = testConfig;
        StandardJMeterEngine jm = new StandardJMeterEngine();

        // create list of all HTTP requests as defined in swagger
        // then sort so we get the proper order of requests (i.e. post before get before delete)
        
        input.getRequests().sort(new RequestComparator());
        List<HTTPSamplerProxy> httpSampler = this.createHTTPSamplerProxy(input);

        int reqNumber = 0;

        // run a separate load test for each req since we want individual CSV/summaries
        // for each
        for (HTTPSamplerProxy element : httpSampler) {
            // use TestElement since we may not always want LoopController
            TestElement logicController = createLoopController(element, testConfig.getLoops());

            SetupThreadGroup threadGroup = this.createLoad((LoopController) logicController, testConfig.getThreads(),
                    testConfig.getRampUp());

            TestPlan testPlan = new TestPlan(testConfig.getTestPlanName());
            testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());

            // set content type
            HeaderManager manager = new HeaderManager();
            manager.add(new Header("Content-Type", "application/json"));
            manager.setName(JMeterUtils.getResString("header_manager_title"));
            manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
            manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());

            // add headers
            element.setHeaderManager(manager);

            hashTree.add(testPlan);
            HashTree samplerTree = new HashTree();
            samplerTree.add(element, manager);

            HashTree threadGroupTree = new HashTree();
            threadGroupTree = hashTree.add(testPlan, threadGroup);
            threadGroupTree.add(samplerTree);

            jm.configure(hashTree);

            // recording results of load test
            Summariser summer = null;
            String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
            if (summariserName.length() > 0) {
                summer = new Summariser(summariserName);
            }

            // Temporary file to be uploaded to S3
            // Will need to change the filename if we want subdirectories for each user
            // Definitely need to change if we want multiple users to run multiple tests at
            // once
            String logFile = BASE_FILE_PATH + reqNumber + ".csv";
            reqNumber++;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            CSVWriter writer = rscs.createWriter(stream);

            ResultSummaryCsv resultSummaryCsv = rscs.createCSV(writer);

            JMeterResponseCollector logger;
            logger = new JMeterResponseCollector(summer, writer);
            logger.setFilename(logFile);

            hashTree.add(hashTree.getArray()[0], logger);

            try {
                log.info("RUNNING LOAD TEST FOR ENDPOINT: {}:{}{}, VERB: {}, ARGS: {}", 
                        element.getDomain(), element.getPort(), element.getPath(),
                        element.getMethod(),
                        element.getArguments());
                jm.run();
                hashTree.clear();

                // Save CSV to database in ResultSummaryCsv
                writer.close();
                resultSummaryCsv.setByteCsv(stream.toByteArray());
                
                // Insert resultsummary and csv to S3 bucket
                ResultSummary resultSummary = new ResultSummary(element.getUrl().toURI(), element.getMethod(), logger,
                        resultSummaryCsv);
                resultSummary = rss.insert(resultSummary);
                InputStream is = new ByteArrayInputStream(stream.toByteArray());
                String filename = "resultsummary_csv_" + resultSummary.getId() + ".csv";
                s3Service.putObjectInBucket(filename, is);
                resultSummary.setDataReference(S3_DOMAIN + "/" + filename);
                
                // Retrieve swaggersummary and add resultsummaries to swaggersummary
                Optional<SwaggerSummary> optSwaggerSummary = sss.getById(swaggerSummaryId);
                if (optSwaggerSummary.isPresent()) {
                    SwaggerSummary swaggerSummary = optSwaggerSummary.get();
                    swaggerSummary.getResultsummaries().add(resultSummary);
                    sss.update(swaggerSummary);
                }
                
            } catch (Exception e) {
                log.error("EXCEPTION FOR ENDPOINT {} WITH METHOD {}", element.getPath(), element.getMethod());
                log.trace("STACK TRACE: ", e);
            }
        }

    }

    /**
     * For OAS 2.0. Parses HTTP request conditions from Docutest input and generates
     * an array of HTTPSamplerProxy objects based the requests field.
     * 
     * @param input Docutest object
     * @return Set of HTTPSamplerProxy objects. Returns an empty set if there are no
     *         endpoints. Returns null if there is a problem with the Swagger input.
     */
    public List<HTTPSamplerProxy> createHTTPSamplerProxy(Docutest input) {
        List<HTTPSamplerProxy> httpSamplers = new ArrayList<>();
        if (input != null) {
            List<Request> requests = input.getRequests();
            // each verb/operation
            for (Request req : requests) {
                HTTPSamplerProxy element = new HTTPSamplerProxy();

                element.setDomain(req.getEndpoint().getBaseUrl());
                element.setPort(req.getEndpoint().getPort());
                String path = removeEmptyPath(req.getEndpoint().getBasePath(), req.getEndpoint().getPath());
                element.setPath(path);
                element.setMethod(req.getVerb().toString());
                element.setFollowRedirects(true);

                if (requiresBody(req.getVerb())) {
                    element.setPostBodyRaw(true);
                    element.addNonEncodedArgument("", req.getBody(), "");
                }
                httpSamplers.add(element);
            }
        }
        return httpSamplers;
    }

    /**
     * Configures a LoopController with the given loop count and httpSampler.
     * Returns null if httpSampler is null.
     * 
     * @param httpSampler HTTPSamplerProxy object representing a request
     * @param loops       Number of iterations
     * @return Covariant LoopController object.
     */
    public TestElement createLoopController(HTTPSamplerProxy httpSampler, int loops) {
        if (httpSampler != null) {
            TestElement loopCtrl = new LoopController();
            ((LoopController) loopCtrl).setLoops(loops);
            loopCtrl.addTestElement(httpSampler);
            return loopCtrl;
        }
        return null;
    }

    /**
     * Creates a thread group (specifically a SetupThreadGroup object) with the
     * given parameters.
     * 
     * @param loopControllers for thread group
     * @param nThreads        Number of threads.
     * @param rampUp          Ramp up time in seconds.
     * @param duration        in seconds
     * @return Configured thread group for ramp up test
     */
    public SetupThreadGroup createLoad(LoopController controller, int threads, int rampUp) {
        if (controller == null) {
            return null;
        }

        SetupThreadGroup ret = new SetupThreadGroup();

        if (testConfig.getDuration() > 0) {
            ret.setScheduler(true);
            ret.setDuration(testConfig.getDuration());
            controller.setLoops(-1);
        }
        
        ret.setNumThreads(threads);
        ret.setRampUp(rampUp);
        ret.setSamplerController(controller); // needs to not be null

        return ret;
    }

    // --------------------- HELPER METHODS -------------------------

    /**
     * Formats path to prevent example.com//. If both basepath and path are "/",
     * returns an empty string
     * 
     * @param basePath
     * @param path
     * @return Adjusted path
     */
    private String removeEmptyPath(String basePath, String path) {
        if (basePath.equals("/")) {
            basePath = "";
        } else if (path.equals("/")) {
            path = "";
        }
        return basePath + path;
    }

    private boolean requiresBody(HttpMethod verb) {
        switch (verb.toString()) {
        case "POST":
            return true;
        case "PUT":
            return true;
        case "PATCH":
            return true;
        default:
            return false;
        }
    }

}
