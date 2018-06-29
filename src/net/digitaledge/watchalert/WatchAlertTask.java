package net.digitaledge.watchalert;

import java.util.ArrayList;
import java.util.List;

public class WatchAlertTask {

	private Integer taskNumber = 0; 
	private Integer period = 10;
	private Integer storeActiveState = 0; //this variable is designed to keep state of this alert: 0 - this task doesn't keep alerts state, 1 - alert is not active, 2 - alert is active. 
	private String sendAlertHTTPLink = new String();
	private String clearAlertHTTPLink = new String();
	private String indice  = new String();
	private String timeformat = new String();
	private String httpBody = new String();
	private Integer timeZoneDiff = 0;
	private String emailFlag  = new String("NO");
	private String smtpServer = new String();
	private String smtpFrom  = new String();
	private String smtpPassword = new String();
	private String smtpSubject = new String();
	private String sendAlertEmailBody = new String();
	private String clearAlertEmailBody = new String();
	private MapAlertStrings alertBody = new MapAlertStrings(); 

	private List<String> recipients = new ArrayList<String>();

	private List<WatchAlertTaskQuery> taskQueries = new ArrayList<WatchAlertTaskQuery>();

	private Long nextExecuteTime = new Long(0);
	
	public WatchAlertTask(Integer number)
	{
		this.taskNumber = number;	
	}

	public Integer getTaskNumber() {
		return taskNumber;
	}
	
	public String getIndice() {
		return indice;
	}
	
	public void setIndice(String indice) {
		this.indice = indice;
	}
	
	public Integer getPeriod() {
		return period;
	}
	
	public String getSendAlertHttpLink() {
		return sendAlertHTTPLink;
	}

	public void setSendAlertHttpLink(String httpLink) {
		this.sendAlertHTTPLink = httpLink;
	}


	public String getHttpBody() {
		return httpBody;
	}

	public void setHttpBody(String httpBody) {
		this.httpBody = httpBody;
	}
	public void setPeriod(Integer period) {
		this.period = period;
	}
	
	public String getTimeformat() {
		return timeformat;
	}
	
	public void setTimeformat(String timeformat) {
		this.timeformat = timeformat;
	}

	public Long getNextExecuteTime() {
		return nextExecuteTime;
	}
	
	public void setNextExecuteTime(Long nextExecuteTime) {
		this.nextExecuteTime = nextExecuteTime;
	}
	
	public Integer getTimeZoneDiff() {
		return timeZoneDiff;
	}

	public void setTimeZoneDiff(Integer timeZoneDiff) {
		this.timeZoneDiff = timeZoneDiff;
	}
	
	public String getEmailFlag() {
		return emailFlag;
	}

	public void setEmailFlag(String emailFlag) {
		this.emailFlag = emailFlag;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getSmtpFrom() {
		return smtpFrom;
	}

	public void setSmtpFrom(String smtpFrom) {
		this.smtpFrom = smtpFrom;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpSubject() {
		return smtpSubject;
	}

	public void setSmtpSubject(String smtpSubject) {
		this.smtpSubject = smtpSubject;
	}

	public String getSendAlertEmailBody() {
		return sendAlertEmailBody;
	}

	public void setSendAlertEmailBody(String smtpBody) {
		this.sendAlertEmailBody = smtpBody;
	}

	public List<String> getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		String[] stringArray = recipients.split(" ");
		for(int i=0; i < stringArray.length; i++)
			this.recipients.add(stringArray[i]);
	}

	public List<WatchAlertTaskQuery> getTaskQueries() {
		return taskQueries;
	}

	public void setTaskQueries(List<WatchAlertTaskQuery> taskQueries) {
		this.taskQueries = taskQueries;
	}
	
	public String getClearAlertHttpLink() {
		return clearAlertHTTPLink;
	}

	public void setClearAlertHttpLink(String clearAlertHTTPLink) {
		this.clearAlertHTTPLink = clearAlertHTTPLink;
	}
	
	public String getClearAlertEmailBody() {
		return clearAlertEmailBody;
	}

	public void setClearAlertEmailBody(String clearAlertEmailBody) {
		this.clearAlertEmailBody = clearAlertEmailBody;
	}
	
	public Integer getStoreActiveState() {
		return storeActiveState;
	}

	public void setStoreActiveState(Integer storeActiveState) {
		this.storeActiveState = storeActiveState;
	}
	
	
	public MapAlertStrings getAlertBody() {
		return alertBody;
	}

	public void setAlertBody(MapAlertStrings previousAlertBody) {
		this.alertBody = previousAlertBody;
	}

}

