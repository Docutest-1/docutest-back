# Introduction

Docutest is a web application that is used to run load tests on web applications based on Swagger files 
submitted to the application. Once load tests are complete, Docutest then returns a graphical representation 
of the results. Any tests results are stored in a database, and can be retrieved as soon as the load tests complete, 
as well as can be retrieved at any point in the future

## Table of Contents
* [Using the API](#using-the-api)
    - [User Configuration Settings](#user-configuration-settings)
    - [Supported Specifications](#supported-specifications)
    - [Response Structure](#response-structure)
    - [Endpoints](#endpoints)
        - [/upload](#upload)
        - [/swaggersummary](#swaggersummary)
        - [/swaggersummary/{id}](#swaggersummaryid)
* [Application Structure](#Application-Structure)
    - [Models](#Models)
        - Results Objects
            - [SwaggerSummary](#swagger-summary-object)
            - [ResultSummary](#ResultSummary)
            - [ResultSummaryCsv](#ResultSummaryCsv)
        - Docutest Adapter Objects
            - [Docutest](#Docutest)
            - [Request](#Request)
            - [Endpoint](#Endpoint)
        - Templates
            - [LoadTestConfig](#LoadTestConfig)
            - [SwaggerSummaryDTO](#SwaggerSummaryDTO)
    - [Controller Layer](#Controller-Layer)
        - [SwaggerfileController](#SwaggerfileController)
    - [Service Layer](#Service-Layer)
        - [JMeterService](#JmeterService)
        - [OASService](#OASService)
            - [JSONStringCreator](#JSONStringCreator)
        - [ResultSummaryService](#ResultSummaryService)
        - [SwaggerSummaryService](#SwaggerSummaryService)
    - [Repository Layer](#Repository-Layer)
        - ResultSummaryRepository
        - SwaggerSummaryRepository
* [Third Party Libraries](#Third-Party-Libraries)
    - [JMeter Java API](#JMeter-Java-API)
* [External Links](#external-links)

# Using the API

Docutest backend currently runs on port 8083, and is hosted on the [AWS EC2 instance here](http://ec2-13-58-23-152.us-east-2.compute.amazonaws.com/) using a Docker image. Calls to the API can be made through the front-end via one of the valid [endpoints](#endpoints).

The full base URL is: http://ec2-13-58-23-152.us-east-2.compute.amazonaws.com:8083/Docutest/

## User Configuration Settings

Along with submitting a Swagger file, users must provide configuration parameters that represent the test plan for the intended load test (represented as a [LoadTestConfig](#LoadTestConfig) object).
- Users must specify a test plan name.
- Users must specify the number of threads that will execute the test plan in its entirety.
- Users must specify a ramp up period which defines the time that it takes for Jmeter to have all specified threads running.
- Users must specify the duration of the load test which defines the period of time for which the test will be performed.*
- Users must specify the number of loops/iterations which defines the number of times that a test case will be performed.*

\* Duration-based and loop-based testing are mutually exclusive. If a value is set for both, duration takes precedence.

These configuration settings apply for each endpoint + verb combination in the specifications file. For example, for threads=20, rampUp=2, loops=10, each endpoint + verb will have a separate test with 20 threads that take 2 seconds to ramp up and iterate 10 times, so a Swagger file with 3 endpoints with 2 verbs each will run 6 JMeter tests in total. Currently, different test configs for different endpoints are not supported.

## Supported Specifications

Currently, only [OAS 2.0/Swagger v1](https://swagger.io/docs/specification/2-0/basic-structure/) is supported. However, the backend is designed using the adapter design pattern such that additional specifications can be included with relative ease. See [Docutest](#Docutest) model for details.

## Response Structure

The API returns 4 main objects. See [endpoints](#endpoints) for a list of endpoints/verbs.

1. SwaggerUploadResponse
    - Immediate response following Swagger + LoadTestConfig upload. Fields:
        - ETA: estimated time to completion, Epoch time
        - resultRef: uri to results once tests are completed
        - swaggerSummaryId

```
{
    "eta": 1602518485179,
    "resultRef": "Docutest/swaggersummary/108",
    "swaggerSummaryId": 108
}
```

2. SwaggerSummary
    - Summary object corresponding to **each Swagger file upload**. Contains test config settings as well as an array of ResultSummary objects. Fields:
        - id
        - testPlanName
        - loops
        - duration
        - threads
        - rampUp
        - followRedirects
        - resultsummaries (array of result summaries)

```
{
    "id": 1,
    "testPlanName": "Test",
    "loops": 10,
    "duration": 0,
    "threads": 10,
    "rampUp": 2,
    "followRedirects": true,
    "resultsummaries": [
        {
            "id": 2,
            "uri": "http://blazedemo.com/",
            "httpMethod": "GET",
            "responseAvg": 329,
            "response25Percentile": 250,
            "response50Percentile": 293,
            "response75Percentile": 362,
            "responseMax": 613,
            "failCount": 0,
            "successFailPercentage": 100.0,
            "reqPerSec": 21.537798836958864,
            "dataReference": s3.fake-aws-website.com/yourdata.csv*
        },
        {
            "id": 1,
            "uri": "http://blazedemo.com/login",
            "httpMethod": "GET",
            "responseAvg": 648,
            "response25Percentile": 274,
            "response50Percentile": 389,
            "response75Percentile": 602,
            "responseMax": 7769,
            "failCount": 0,
            "successFailPercentage": 100.0,
            "reqPerSec": 9.097525473071325,
            "dataReference": s3.fake-aws-website.com/yourdata.csv*
        }
    ]
}
```

3. ResultSummary
    - Summary object with aggregate statistics corresponding to **each individual test (i.e. endpoint+verb combination)**. All times are in ms. Fields:
        - id
        - uri
        - httpMethod
        - responseAvg
        - response25Percentile
        - response50Percentile
        - responseMax
        - failCount: count of 4XX/5XX response codes
        - successFailPercentage: percentage of 2XX / (2XX + 4XX + 5XX) response codes
        - reqPerSec
        - dataReference*

```
{
	"id": 14,
	"uri": "http://blazedemo.com/login",
	"httpMethod": "GET",
	"responseAvg": 366,
	"response25Percentile": 264,
	"response50Percentile": 300,
	"response75Percentile": 370,
	"responseMax": 2941,
	"failCount": 0,
	"successFailPercentage": 100,
	"reqPerSec": 24.568213645185857,
	"dataReference": s3.fake-aws-website.com/yourdata.csv*
}
```

4. ResultSummaryCsv*
    - Object corresponding to the CSV table with information for **each thread within a single test**. Fields:
        - id: one-to-one FK referencing the ID of the ResultSummary object
        - byteCsv: byte[] corresponding to the CSV file

Result Summary CSV Format:

Each row represents a single request by a single thread and contains the following: timestamp, response time, status code, and error message. A header row is included. For example:
|timestamp|elapsed|responseCode|failureMessage|
|----------|---|---|---|
|1601578125|232|200||
|1601578127|100|200||
|1601578130|122|200||
|1601578131|79|400|Error Message|
...

\* **THIS IS CURRENTLY IMPLEMENTED AS A SEPARATE ENTITY DUE TO PERMISSIONS ISSUES WITH IAM/S3, BUT IDEALLY WOULD BE HOSTED AS A STATIC .CSV FILE ON AN S3 BUCKET**

# Endpoints

All endpoints are relative to the [base URL](#using-the-api). For examples of response objects, refer to [Response Structure](#Response-Structure)

## /upload
```
/upload
```
**Verb**: POST

**Format**: Multipart/form-data
- Key: file
    - Valid Swagger file in JSON format, 
- Key: LoadTestConfig
    - JSON string representation of [LoadTestConfig](#LoadTestConfig)

**Status Codes:**
- 200: OK
- 400: Malformed Swagger file or LoadTestConfig

**Response**: [SwaggerUploadResponse](#SwaggerUploadResponse) object

## /swaggersummary
```
/swaggersummary
```
**Verb**: GET

**Format**: application/json

**Status Codes:**
- 200: OK

**Response**: Array of all SwaggerSummary objects. Temporary placeholder until User account/login features have been implemented

## /swaggersummary/{id}
```
/swaggersummary/12
```
**Verb**: GET

**Format**: application/json

**Status Codes:**
- 200: OK
- 204: No Content, i.e. SwaggerSummary with specified id does not exist

**Response**: [SwaggerSummary](#SwaggerSummary-Object) object which contains fields for the test configuration settings plus an array containing resultsummaries objects for each request/endpoint combination. The resultsummaries object contains aggregate summary results for the test, along with a link to a CSV file which contains information for each individual thread. All times are in milliseconds (ms). **If the specified SwaggerSummary object with {id} is not found, simply returns a blank response.**

**Raw data for each thread (i.e. dataReference) is currently being stored directly as a byte[], but this implementation is temporary. See [ResultSummaryCsv section in Response Structure](#Response-Structure)**


# Application Structure

## Models

### SwaggerSummary (Object)

- Object representation of the load test results for each file upload. Contains the results of each individual test as a Set of ResultSummary objects. See [Response Structure](#Response-Structure)

### ResultSummary

- Object representation of a single load test (endpoint + verb). Contains aggregate statistics such as average response time. See [Response Structure](#Response-Structure)

### ResultSummaryCsv

- Object representation of the CSV file for a single load test. Contains the id, which maps to the ResultSummaryId and a byte array containing the CSV file itself. Each row corresponds to a single HTTP request and contains the timestamp, response time/elapsed, status code, and message. See [Response Structure](#Response-Structure)

### Docutest

- Object representation of the specification file. Contains:
    - id: corresponds to the SwaggerSummary id
    - `List<Request>` : List of HTTP Requests parsed from the spec file

- Implementations:
    - SwaggerDocutest: Implementation for OAS 2.0/Swagger v1. Currently, Docutest/SwaggerDocutest does not really need to be an interface/implementing class, but this design allows for addition of other specifications down the line.

### Request

- Object Representation of a single HTTP request. Contains:
    - Endpoint
    - HTTP Verb
    - List<Header> for header params
    - Map<String, String> for path params
    - Strng body for request bodies

### Endpoint

- Object representation of a single endpoint. Contains:
    - baseUrl (aka domain)
    - port
    - basePath
    - path

For example, for: myec2instance.com:8080/project3/login

`baseURL=myec2instance.com`

`port=8080`

`basePath=/project3`

`path=/login`

### LoadTestConfig

- Object representation of configuration settings. This object is used to create the LoopController and SetupThreadGroup objects used to configure JMeter tests in JMeterService. Contains:
    - testPlanName
    - loops
    - duration
    - threads
    - rampUp
    - followRedirect

### SwaggerSummaryDto

- DTO for SwaggerSummary object

## Controller Layer

See [endpoints](#endpoints) for mappings

### SwaggerfileController

- Controller responsible for file upload and responses related to result summaries

## Service Layer

### JMeterService

- Service that performs the actual JMeter load tests via the loadTesting() method. Requires:
    - Docutest object
    - LoadTestConfig
    - SwaggerSummary id

In order to have summary statistics for each individual endpoint. the service iterates through the Docutest object's list of Requests, and creates a separate load test for each Request. The results of the load test are collected using JMeterResponseCollector, and written to a ResultSummary object that contains the aggregate information, along with a CSV file stored in ResultSummaryCsv that contains granular information for each thread*. See [JMeter Java API](#JMeter-Java-Api) for details.

\* Temporary implementation due to permissions issues with IAM/S3.

### OASService

- Service to create Docutest objects from Swagger/OpenAPI objects. Currently, only Swagger objects (i.e. OAS 2.0/Swagger v1) are implemented. Uses JSONStringCreator to auto generate request bodies based on Definitions found in the spec file.

### JSONStringCreator

- Recursively generates a JSON string representation of an object defined in the spec file. Values are generated based on field type:
    - String: ""
    - Number: 0
    - Integer: 1
    - Boolean: false
    - Arrays: array with a single entry of the array's specified type (e.x. [0] or [{field1:"", field2:"0"}])
    - Objects: generated via recursion until only primitive fields remain
- For fields named "id" (case insensitive), JSONStringCreator auto increments an integer starting from 1. Since JSONStringCreator is protoype scoped, auto ID restarts at 1 for each instance. Thus, a spec file with multiple POST requests, for example, will have auto ID generate start at 1 for each request.

### ResultSummaryService

- Service for interfacing with the repository layer.

### SwaggerSummaryService

- Service for interfacing with the repository layer.

## Repository Layer

Actual DAO/Repositories are implemented through Spring Data JPA. There are currently two interfaces:

- ResultSummaryRepository
- SwaggerSummaryRepository

# Third Party Libraries

## JMeter Java API

The JMeter Java API (ver 5.3) is what is used to actually send HTTP requests and perform load tests. A HashTree with the relevant configuration settings is passed into a JMeterStandardEngine, followed by a .run() call on the JMeterStandardEngine instance. See JMeterService.java for specific implementation details, but the general structure is as follows:

```
// Initialize JMeter engine
StandardJMeterEngine jm = new StandardJMeterEngine();

// Set up configurations
HashTree jmeterConfig = new HashTree();
jmeterConfig.add(relevantConfig1);
jmeterConfig.add(relevantConfig2);
...

// Configure JMeter engine with HashTree
jm.configure(jmeterConfig);

// Run load test
jm.run();
```

Just like the GUI, test elements can be nested within each other in the HashTree configuration. In other words, JMeter configurations consist of a series of nested HashTrees. At a minimum, requires a Test Plan, a Thread Group, a Logic Controller, and at least one HTTPSampler. 

The current implementation specifically uses TestPlan, SetupThreadGroup, LoopController, HTTPSamplerProxy, and HeaderManager objects, and has a configuration hierarchy as such:

- TestPlan
    - SetupThreadGroup
        - LoopController
        - HTTPSamplerProxy
            - HeaderManager

where (TestPlan, ThreadGroup) is a Key/Value pair in the overall configuration HashTree, (HTTPSampler, Header Manager) is a Key/Value pair nested in the ThreadGroup HashTree, and the Loop Controller is passed into the ThreadGroup during the initial SetupThreadGroup creation.

HTTPSampler/HTTPSamplerProxy objects contain information on, and roughly correspond to, HTTP requests. HTTPSamplerProxy objects are required when setting header parameters, and are thus required for POST/PUT/PATCH requests to include the "Content-Type: application/json" header. LoopController and SetupThreadGroup contain information on load test parameters such as number of threads, loops, duration, etc. TestPlan is a high level organizational object that corresponds to a test plan.

In theory, we could have a single HashTree configured with multiple test plans corresponding to each HTTP request, and call .run() once, however this makes the logic behind collecting and saving results more complicated. If performance issues ever arise from having to call .configure(HashTree) and .run() for each HTTP request, refactoring JMeterService to add multiple TestPlan objects to a single overall HashTree configuration could be a potential solution.

# External Links:
- [Test Case Design](https://drive.google.com/file/d/1Jh1iYjdp2j4YR8yoAjYcUrd1arveYbNI/view?usp=sharing-)
- [Requirements Traceability Matrix](https://drive.google.com/file/d/1ckCViMN4p7jxq4tk50QlwTb5-qGwPsBw/view?usp=sharing)
