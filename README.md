# docutest-back

Docutest is a web application that is used to run load tests on web applications based on Swagger files 
submitted to the application. Once load tests are complete, Docutest then returns a graphical representation 
of the results. Any tests results are stored in a database, and can be retrieved as soon as the load tests complete, 
as well as can be retrieved at any point in the future

# Configuration

Along with submitting a Swagger file, users must provide configuration parameters that represent the test plan for the intended load test.
- Users must specify a test plan name.
- Users must specify the number of threads that will execute the test plan in its entirety.
- Users must specify a ramp up period which defines the time that it takes for Jmeter to have all specified threads running.
- Users must specify the duration of the load test which defines the period of time for which the test will be performed. 
- Users must specify the number of loops/iterations which defines the number of times that a test case will be performed.

## Test Result API Response

The API sends back two objects for each URI/Method combination: an aggregate summary and a 2D array containing each thread that was run during the test. The aggregate object contains:

uri: URI

httpMethod: Request method

responseAvg: Average latency in ms

response25Percentile: 25th percentile latency in ms

response50Percentile: 50th percentile latency in ms

response75Percentile: 75th percentile latency in ms

responseMax: Highest response time in ms

failCount: Number of 4XX/5XX responses

successFailPercentage: Percentage of 2XX responses to 4XX/5XX responses

reqPerSec: Total Number of requests per second

dataReference: Link to the csv file with individual thread data


```
{
	uri: google.com/whateverpath,
	httpMethod: GET,
	responseAvg: 16,
	response25Percentile: 7,
	response50Percentile: 16,
	response75Percentile: 29,
	responseMax: 3000,
	failCount: 100,
	successFailPercentage: 50,
	reqPerSec: 533.6,
	dataReference: s3.fake-aws-website.com/yourdata.csv
}
```

For dataReference, each row represents a single request by a single thread and contains the following: [timestamp, latency, status code]. For example:
|timestamp|elapsed|responseCode|failureMessage|
|----------|---|---|---|
|1601578125|16|200||
|1601578127|15|200||
|1601578130|16|200||
|1601578131|12|400|Error Message|
...
