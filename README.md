# AppDynamics Apache Log Monitoring Extension Plus

## What is New?

Upgrade the log4j to log4j2

Upgrade some other dependency libraries

New Metrics:

Apache
 * Total Pages Views
 * Total Total Success Page View *
 * Total Total Non Success Page View *
 * Normal Average Response Time (micro) **
 * Average Response Time (micro)
 * Normal Average Response Time (mili) **
 * Average Response Time (mili)

Per Page
 * Total Pages Views
 * Total Total Success Page View *
 * Total Total Non Success Page View *
 * Normal Average Response Time (micro) **
 * Average Response Time (micro)
 * Normal Average Response Time (mili) **
 * Average Response Time (mili)


\* Success is return code 2XX
\*\* Normal Response Time is the average of 2XX calls only (others calls with return code 300 or higher will not count) 

## What changed?

Extensions run in continous mode now to collect all the logs in the minute, so you will have more accurated results.

More Debugs in case of wrong configurations or incompatibilities.




## Use Case

<p>Monitors Apache access log file and reports metrics such as successful hits, bandwidth and page access count of visitors, spiders, browsers and operating systems. 

Has the ability to display individual metrics per visitor, spider, browser, operating system, response code and page request.

This extension works only with the standalone machine agent.

**Note: By default, the Machine agent can only send a fixed number of metrics to the controller. This extension can potentially report thousands of metrics, so to change this limit, please follow the instructions mentioned [here](https://docs.appdynamics.com/display/PRO40/Metrics+Limits).** 

## Installation

1. Run 'mvn clean install' from apache-log-monitoring-extension directory
2. Copy and unzip ApacheLogMonitor-\<version\>.zip from 'target' directory into \<machine_agent_dir\>/monitors/
3. Edit config.yaml file in ApacheLogMonitor/conf and provide the required configuration (see Configuration section)
4. Restart the Machine Agent.

## Configuration

### config.yaml

**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

| Param | Description | Default Value | Example |
| ----- | ----- | ----- | ----- |
| displayName | The alias name of this log, used in metric path. |  | "Staging Apache" |
| logDirectory | The full directory path access log |  | "/var/log/apache2" |
| logName | The access log filename. Supports wildcard (\*) for dynamic filename |  | **Static name:**<br/>"access.log" <br/><br/>**Dynamic name:**<br/>"access\*.log" |
| logPattern | The grok pattern used for parsing the log. See examples for pre-defined pattern you can use.<br/><br/>If you're using a custom log format, you can create your own grok pattern to match this, see Grok Expressions section.  |  | **"%{COMMONAPACHELOG}"** - for common log format<br/><br/>**"%{COMBINEDAPACHELOG}"** - for combined log format |
| hitResponseCodes | The response codes used to determine a successful hit. Leave null to use default values. | 200, 304 | 200, 201, 304, 305 |
| nonPageExtensions | The URL extensions used to determine if request is for non-page access, e.g. image. Leave null to use default values | "ico", "css", "js",<br/>"class","gif","jpg",<br/>"jpeg","png","bmp",<br/>"rss","xml","swf" | "pdf","jpg" |
| **metricsFilterForCalculation** | **Filters unwanted metrics** | ----- | ----- |
| excludeVisitors | The list of visitor hosts to exclude. Note, this supports regex, so period must be escaped. |  | **Specific  Host:**<br/>"10\\\\.10\\\\.10\\\\.5",<br/>"127\\\\.1\\\\.1\\\\.0"<br/><br/>**Host Regex:**<br/>"10.\*", "127.\*" |
| excludeSpiders* | The list of spider names to exclude. Note, this supports regex. |  | **Specific Spider:**<br/>"GoogleBot",<br/>"Yahoo"<br/><br/>**Spider Regex:**<br/>"Google.\*" |
| excludeUrls | The list of request URLs (any files) to exclude. Note, this supports regex. |  | **Specific URL:**<br/>"/test.html",<br/>"/test/admin.html"<br/><br/>**URL Regex:**<br/>"/test/.\*" |
| excludeBrowsers* | The list of browser names to exclude. Note, this supports regex. |  | **Specific URL:**<br/>"Chrome",<br/>"Safari"<br/><br/>**URL Regex:**<br/>"Chro.\*" |
| excludeOs* | The list of OS names to exclude. Note, this supports regex. |  | **Specific OS:**<br/>"MAC OS X"<br/><br/>**OS Regex:**<br/>"MAC.\*" |
| **individualMetricsToDisplay** | **Displays individual metrics** | ----- | ----- |
| includeVisitors | The list of visitor hosts to display. Note, this supports regex, so period must be escaped. |  | **Specific  Host:**<br/>"10\\\\.10\\\\.10\\\\.5",<br/>"127\\\\.1\\\\.1\\\\.0"<br/><br/>**Host Regex:**<br/>"10.\*", "127.\*" |
| includeSpiders* | The list of spider names to display. Note, this supports regex. |  | **Specific Spider:**<br/>"GoogleBot",<br/>"Yahoo"<br/><br/>**Spider Regex:**<br/>"Google.\*" |
| includePages | The list of pages to display. Note, this supports regex. |  | **Specific Page:**<br/>"/test.html",<br/>"/test/admin.html"<br/><br/>**Page Regex:**<br/>"/test/.\*" |
| includeBrowsers* | The list of browser names to display. Note, this supports regex. |  | **Specific URL:**<br/>"Chrome",<br/>"Safari"<br/><br/>**URL Regex:**<br/>"Chro.\*" |
| includeOs* | The list of OS names to display. Note, this supports regex. |  | **Specific OS:**<br/>"MAC OS X"<br/><br/>**OS Regex:**<br/>"MAC.\*" |
| includeResponseCodes* | The list of response codes to display. |  | 200, 304, 404 500 |
| ----- | ----- | ----- | ----- |
| noOfThreads | The no of threads used to process multiple apache logs concurrently | 3 | 3 |
| metricPrefix | The path prefix for viewing metrics in the metric browser. | "Custom Metrics\|Apache Log Monitor\|" | "Custom Metrics\|Apache Log Monitor2\|" |


**\*Requires user-agent details in the log, e.g. use combined log pattern in apache + specify logPattern as "%{COMBINEDAPACHELOG}" in this config.yaml.**

### sample config.yaml with static filename and dynamic filename

~~~
apacheLogs:
  - name: "StaticName"
    logDirectory: "/var/log/apache2"
    logName: "access.log"
    logPattern: "%{COMMONAPACHELOG}"
    hitResponseCodes: [ ] #leave null to use default values
    nonPageExtensions: [ ] #leave null to use default values
    
    metricsFilterForCalculation:
       excludeVisitors: [ ]
       excludeSpiders: [ ]
       excludeUrls: [ ]
       excludeBrowsers: [ ]
       excludeOs: [ ]
      
    individualMetricsToDisplay:
       includeVisitors: ["10\\.10.*" ]
       includeSpiders: ["Google.*" ]
       includePages: ["/test/.*" ]
       includeBrowsers: ["Chrome.*" ]
       includeOs: ["MAC.*" ]
       includeResponseCodes: [200, 305, 304, 400, 401, 500 ]
       
  - name: "DynamicLog"
    logDirectory: "/usr/log/apache2"
    logName: "access*.log"
    logPattern: "%{COMBINEDAPACHELOG}"
    hitResponseCodes: [ ] #leave null to use default values
    nonPageExtensions: [ ] #leave null to use default values
    
    metricsFilterForCalculation:
       excludeVisitors: [ ]
       excludeSpiders: [ ]
       excludeUrls: [ ]
       excludeBrowsers: [ ]
       excludeOs: [ ]
      
    individualMetricsToDisplay:
       includeVisitors: [ ]
       includeSpiders: [ ]
       includePages: [ ]
       includeBrowsers: [ ]
       includeOs: [ ]
       includeResponseCodes: [ ]
        
noOfThreads: 3        

metricPrefix: "Custom Metrics|Apache Log Monitor|"
~~~

### Grok Expressions
Grok is a way to define and use complex, nested regular expressions in an easy to read and use format. Regexes defining discrete elements in a log file are mapped to grok-pattern names, which can also be used to create more complex patterns.

Grok file is located in **ApacheLogMonitor/conf/patterns/grok-patterns.grok**.

To add your own custom grok expression, simply edit the file above. Note, you must ensure that mandatory fields are captured:

- clientip
- response
- bytes
- request
- microseg
- miliseg

Optional field used to determine the browser, os and spider details is as follow:

- agent

For example:

~~~
COMMONAPACHELOG %{IPORHOST:clientip} %{USER:ident} %{USER:auth} \[%{HTTPDATE:timestamp}\] "(?:%{WORD:verb} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})" %{NUMBER:response} (?:%{NUMBER:bytes}|-)

MYCUSTOMAPACHELOG %{COMMONAPACHELOG} %{QS:referrer} %{QS:agent}

For example 2:

Log format IBM apache (Custom CSV Format):

LogFormat "%h,%{%Y-%m-%d-%H.%M.%S.000000}t,%p,%H,%m,%l,%U,%q,%>s,(Res.Status),%b,(Bytes Trans),%D,(micro-seg),%T,(seg),%{WAS}e,%X" customLogcsv
~~~

Extension grok Example

DATETIME_ESPECIFIC %{YEAR}[./-]%{MONTHNUM}[./-]%{MONTHDAY}[./-]%{HOUR}[./-]%{MINUTE}[./-]%{SECOND}[./-]%{MILISECOND}

CUSTOMAPACHELOG %{IPORHOST:clientip},%{DATETIME_ESPECIFIC:timestamp},%{NUMBER:portnumber},(?:HTTP/%{NUMBER:httpversion})?,(?:%{WORD:verb}),(?:%{NUMBER:logid}|-),%{DATA:request},(?:%{DATA:querystring})?,%{NUMBER:response},\(Res.Status\),(?:%{NUMBER:bytes}|-),\(Bytes Trans\),(?:%{NUMBER:microseg}|-),\(micro-seg\),(?:%{NUMBER:seg}|-),\(seg\),(?:%{HOSTPORT:hostport}|-),(?<trash>.*)
~~~

~~~

You can use [Grok Debugger](https://grokdebug.herokuapp.com/) to validate your expression.

Then, define your custom expression in logPattern field in config.yaml, e.g.

~~~
...
logPattern: "%{MYCUSTOMAPACHELOG}"
...
~~~

## Metrics

### Definition
| Metric | Description |
| ----- | ----- |
| Hits | No of any file requests where response code matches the defined hitResponseCode |
| Bandwidth (bytes) | File size in bytes |
| Pages | The no of page requests, excluding files where extensions are defined in nonPageExtensions. |

Typical Metric Path: **Application Infrastructure Performance|\<Tier\>|Custom Metrics|Apache Log Monitor|\<Log Name\>|** followed by the individual categories/metrics below:

| Metric | Description |
| ----- | ----- |
| Total Hits | Overall Total Hits (Visitor Hits + Spider Hits) |
| Total Bandwidth (bytes) | Overall Total Bandwidth (Visitor Bandwidth + Spider Bandwidth) |
| Total Pages Views | Overall Total Pages (Visitor Pages + Spider Pages) |
| Total Total Success Page View | Overall Total (2XX return) Pages (Visitor Pages + Spider Pages) |
| Total Total Non Success Page View | Overall Total (Non 2XX return) Pages (Visitor Pages + Spider Pages) |
| Normal Average Response Time (micro) | 2XX return Average Response time |
| Average Response Time (micro) | Average Response time |
| Normal Average Response Time (mili) | 2XX return Average Response time |
| Average Response Time (mili) | Average Response time |



### Visitor, Spider, OS and Browser

| Metric | Description |
| ----- | ----- |
| Total Hits | No of hits |
| Total Bandwidth (bytes) | Bandwidth size |
| Total Pages | No of Pages |

### Page

| Metric | Description |
| ----- | ----- |
| Total Hits | No of hits |
| Total Pages Views | Overall Total Pages (Visitor Pages + Spider Pages) |
| Total Total Success Page View | Overall Total (2XX return) Pages (Visitor Pages + Spider Pages) |
| Total Total Non Success Page View | Overall Total (Non 2XX return) Pages (Visitor Pages + Spider Pages) |
| Normal Average Response Time (micro) | 2XX return Average Response time |
| Average Response Time (micro) | Average Response time |
| Normal Average Response Time (mili) | 2XX return Average Response time |
| Average Response Time (mili) | Average Response time |
| Total Bandwidth (bytes) | Overall Total Bandwidth (Visitor Bandwidth + Spider Bandwidth) |



### Response Code

| Metric | Description |
| ----- | ----- |
| Hits | No of times this response code is returned for any file request|
| Bandwidth (bytes) | Bandwidth size |
| Pages | No of times this response code is returned for any page request |

## Custom Dashboard Example
![image](https://github.com/diegopereiraeng/apache-log-monitoring-extension/blob/master/sample_custom_dashboard.png)
![image](https://github.com/diegopereiraeng/apache-log-monitoring-extension/blob/master/sample_custom_dashboard2.png)
## Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/diegopereiraeng/apache-log-monitoring-extension).


## Support

For any questions or feature request, please create a issue here or a pull request =]

