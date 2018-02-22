WatchAlerts plugin for Elasticsearch 5.6.1

Installation:
Download archive and install with the following command:
	bin/plugin install file://path/to/file/watchalert-x.x.x.zip

Configuration watchalert.yml:
Each task must defined by required options. All options for each task must be started with watchalert.taskX, where X is a number of task.

Common options:
	watchalert.elastichost - IP or FQDN for Elasticsearch server. Default 127.0.0.1. Not required.
	watchalert.elasticport - Elasticsearch server port. Default 9200. Not required.
	watchalert.enabledebug - enable debug output. Possible values: true or false. Default false. Not required.

TASK Required configurations for each task:	
	watchalert.taskX.indice - for example: '/paloalto_logs-2017.01/_search?pretty'.(REQUIRED)
	watchalert.taskX.period - Defining how often this task will be executing. For example 10 means every 10 seconds.(REQUIRED)
	watchalert.taskX.timeformat - Time format. Default "yyyy/MM/dd HH:mm:ss".	
	watchalert.taskX.timeZoneDiff - time zone difference in hours. Can be positive or negative. 

TASK Query 	- each task has to contains at minimum one query(maximum 20). Each next query receiving data from previously executed query. The alert will fire only if last query will return alerts.  
	watchalert.taskX.queryY.fields - search keywords in fields. The list must be separated by space.(REQUIRED)
	watchalert.taskX.queryY.querybody - JOSN query: Example:'{"query":{"term":{"GenerateTime":"2017/01/10 16:56:32"}}}'. (REQUIRED)
	watchalert.taskX.queryY.keywords - List of words separated by space which will try to find in query results. For example: fail error auth. The sing '*' meaning any words.
	watchalert.taskX.queryY.procedure - reserved for future use.
	watchalert.taskX.queryY.gt - greater than. If this option is configured then plugin will try  convert value in specific fields to double and will compare it with value in this option.   
	watchalert.taskX.queryY.lt - less than. If this option is configured then plugin will try convert value in specific fields to double and will compare it with value in this option. It received value is less than expected then an alert event will fired.
	The task must has either watchalert.taskX.keywords or watchalert.taskX.gt or watchalert.taskX.lt or watchalert.taskX.procedure option.
  
	  
Actions: each task must has at minimum one action. Supported two actions: send HTTP request and  	
ACTION MAIL: each task can have only one set of email settings. All of settings bellow must be present for each task except smtppassword.
	watchalert.taskX.action.smtpserver	- must contain SMTP server server. It can be in two formats : hostname and hostname:port. Default port is 25.(REQUIRED)   
	watchalert.taskX.action.smtpfrom - mail from, also used for authentication.(REQUIRED)
	watchalert.taskX.action.smtpsubject - mail subject.(REQUIRED)
	watchalert.taskX.action.smtpbody - mail body.(REQUIRED)
	watchalert.taskX.action.recipients - list of recipients separated by space. Each record must be in format: prefix:email@domain. Prefix should be one of following: to, cc,  bcc.(REQUIRED)
	watchalert.taskX.action.smtppassword - mail password. Not required. No  In case if not exists no authentication will be happened.
		
ACTION HTTP: sending HTTP request, method GET. Both parameters must be present.
	watchalert.taskX.action.httplink - EMS host. Should contain POST query.(REQUIRED)
	watchalert.taskX.action.httpbody - Contains message string. If defined, it will be replace all %MESSAGE% pattern.(REQUIRED)

Reserved passphrases.
	%YEAR% - in format yyyy.
	%MONTH%  - in fromat mm, possible value 01-12.
	%DAY% - in format dd, possible value 01-31.
	%CURDATE% - see watchalert.taskX.timeformat.
	%MESSAGE% - message body. May be replaced by watchalert.taskX.message.
	%HOST% - existing host ID in EMS.
	%EPOCHTIME% - Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
	%TIMESTAMP% - in format  yyyy-MM-ddTHH:mm:ss.SSSZ.
	%TIMESTAMP-PERIOD% - replacing by current datetime minus time defined in watchalert.taskX.period. Default format yyyy-MM-ddTHH:mm:ss.SSSZ.
	
	Example:
	watchalert.elastichost: 192.168.19.174
	watchalert.elasticport: 9200
	watchalert.task1.indice: '/nrx-azure-signin-%YEAR%.%MONTH%/_count?pretty'
	watchalert.task1.period: 300
	watchalert.task1.timeZoneDiff: 4
	watchalert.task1.query1.querybody: '{"query":{"bool":{"must":[{"range":{"@timestamp":{"gte":"%TIMESTAMP-PERIOD%"}}}]}}}'
	watchalert.task1.query1.fields: count
	watchalert.task1.query1.lt: 10
	watchalert.task1.action.httplink: 'https://ems.digitaledge.net/service4/ReportAlert.asp?DeviceId=4631&AllowDelete=1&IsCritical=0&AlertType=Critical&AlertMessage=%MESSAGE%'
	watchalert.task1.action.httpbody: Do not received any logs from CHP AD servers. Call Evghenii 929-202-1178.
	 