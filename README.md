# Introduction

Docutest is a web application that is used to run load tests on web applications based on Swagger files 
submitted to the application. Once load tests are complete, Docutest then returns a graphical representation 
of the results. Any tests results are stored in a database, and can be retrieved as soon as the load tests complete, 
as well as can be retrieved at any point in the future

## User Configuration Settings

Along with submitting a Swagger file, users must provide configuration parameters that represent the test plan for the intended load test.
- Users must specify a test plan name.
- Users must specify the number of threads that will execute the test plan in its entirety.
- Users must specify a ramp up period which defines the time that it takes for Jmeter to have all specified threads running.
- Users must specify the duration of the load test which defines the period of time for which the test will be performed.*
- Users must specify the number of loops/iterations which defines the number of times that a test case will be performed.*

\* Duration-based and loop-based testing are mutually exclusive. If a value is set for both, duration takes precedence.

Docutest backend currently runs on port 8083, and is hosted on the [AWS EC2 instance here](http://ec2-13-58-23-152.us-east-2.compute.amazonaws.com/) using a Docker image.

## Test Result API Response

The API sends back a SwaggerSummary object which contains fields for the test configuration settings plus an array containing resultsummaries objects for each request/endpoint combination. The resultsummaries object contains aggregate summary results for the test, along with a link to a CSV file which contains information for each individual thread. **All times are in milliseconds (ms).**

**Raw data for each thread (i.e. dataReference) is currently being stored directly as a byte[], but this implementation is temporary.**

### Example Structure

SwaggerSummary Structure:

```
{
	"testConfigParam1": value,
	"testConfigParam2": value,
	...
	"testConfigParamN": value,
	"resultsummaries": [
		{resultsummary object},
		{resultsummary object},
		...
	]
}
```

ResultSummary Structure:

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
	"dataReference": s3.fake-aws-website.com/yourdata.csv
}
```

SwaggerSummary Example:

```
{
    "id": 5,
    "testPlanName": "test",
    "loops": 100,
    "duration": 0,
    "threads": 10,
    "rampUp": 2,
    "followRedirects": true,
    "resultsummaries": [
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
            "dataReference": null
        },
        {
            "id": 12,
            "uri": "http://blazedemo.com/register",
            "httpMethod": "GET",
            "responseAvg": 344,
            "response25Percentile": 259,
            "response50Percentile": 295,
            "response75Percentile": 387,
            "responseMax": 1229,
            "failCount": 0,
            "successFailPercentage": 100,
            "reqPerSec": 27.156939956005758,
            "dataReference": null
        },
        {
            "id": 13,
            "uri": "http://blazedemo.com/",
            "httpMethod": "GET",
            "responseAvg": 343,
            "response25Percentile": 239,
            "response50Percentile": 286,
            "response75Percentile": 384,
            "responseMax": 1376,
            "failCount": 0,
            "successFailPercentage": 100,
            "reqPerSec": 27.37550986887131,
            "dataReference": null
        }
    ]
}
```

For dataReference, each row represents a single request by a single thread and contains the following: timestamp, latency, status code. A header row is included. For example:
|timestamp|elapsed|responseCode|failureMessage|
|----------|---|---|---|
|1601578125|16|200||
|1601578127|15|200||
|1601578130|16|200||
|1601578131|12|400|Error Message|
...

## External Links:
- [Test Case Design](https://drive.google.com/file/d/1Jh1iYjdp2j4YR8yoAjYcUrd1arveYbNI/view?usp=sharing-)
- [Requirements Traceability Matrix](https://drive.google.com/file/d/1ckCViMN4p7jxq4tk50QlwTb5-qGwPsBw/view?usp=sharing)
