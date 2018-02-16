package net.digitaledge.watchalert;

import java.util.ArrayList;
import java.util.List;

public class WatchAlertConfig {
	
	
	private String elasticHost = new String("127.0.0.1");
	private String elasticPort = new String("9200");
	private String enableDebug = new String("false");
	private Integer maxTasks = new Integer(20);
	private WatchAlertSettings settings;
	private List<WatchAlertTask> watchAlertTaskList = new ArrayList<WatchAlertTask>();
	
	public WatchAlertConfig()
	{
		settings = new WatchAlertSettings();
		parseConfig(settings);
	}
	
	private void parseConfig(WatchAlertSettings settings)
	{
		try{	
			
			if(settings.get("watchalert.elastichost") != null)
				elasticHost = settings.get("watchalert.elastichost");
			if(settings.get("watchalert.elasticport") != null)
				elasticPort = settings.get("watchalert.elasticport");
			if(settings.get("watchalert.enabledebug") != null)
				enableDebug = settings.get("watchalert.enabledebug");
			
			for(int i = 1; i <= maxTasks; i++)
			{
				boolean foundAllRequiredFields = false;
				//System.out.println("foundAllRequiredFileds " + foundAllRequiredFields +" " + i);
				
				
				WatchAlertTask watchAlertTask = new WatchAlertTask(i);
				if(settings.get("watchalert.task"+i+".indice") != null &&
					settings.get("watchalert.task"+i+".querybody") != null &&
					settings.get("watchalert.task"+i+".period") != null){
					
						foundAllRequiredFields = true;
						watchAlertTask.setIndice(settings.get("watchalert.task"+i+".indice"));
						watchAlertTask.setQuerybody(settings.get("watchalert.task"+i+".querybody"));
						watchAlertTask.setPeriod(Integer.parseInt(settings.get("watchalert.task"+i+".period")));
						
					
						if(settings.get("watchalert.task"+i+".fields") != null)
						{
							foundAllRequiredFields = true;
							watchAlertTask.setFields(settings.get("watchalert.task"+i+".fields"));
						}
						else if (settings.get("watchalert.task"+i+".procedure") != null)
						{
							foundAllRequiredFields = true;
							watchAlertTask.setCampareFlag("OTHER");
							watchAlertTask.setProcedure(settings.get("watchalert.task"+i+".procedure"));
						}
						
						if(foundAllRequiredFields)
						{
							
							System.out.println("foundAllRequiredFileds " + foundAllRequiredFields +" " + i);
							
							if(settings.get("watchalert.task"+i+".timeformat") == null)
								watchAlertTask.setTimeformat("");
							else watchAlertTask.setTimeformat(settings.get("watchalert.task"+i+".timeformat"));
							
							if(settings.get("watchalert.task"+i+".timeZoneDiff") != null )
								watchAlertTask.setTimeZoneDiff(Integer.parseInt(settings.get("watchalert.task"+i+".timeZoneDiff")));
							
							if(settings.get("watchalert.task"+i+".replaceFields") != null )
								watchAlertTask.setReplaceFields(settings.get("watchalert.task"+i+".replaceFields"));
											
							if(settings.get("watchalert.task"+i+".gt") != null )
							{
								watchAlertTask.setCampareFlag("GREATER_THAN");
								String str = settings.get("watchalert.task"+i+".gt");
								watchAlertTask.setGreaterThan(Integer.parseInt(str.trim()));
							}
							else if(settings.get("watchalert.task"+i+".lt") != null )
							{
								watchAlertTask.setCampareFlag("LESS_THAN");
								String str = settings.get("watchalert.task"+i+".lt");
								watchAlertTask.setLessThan(Integer.parseInt(str.trim()));
							}
							
							else if(settings.get("watchalert.task"+i+".keywords")!= null)
							{
								watchAlertTask.setCampareFlag("FIND_KEYWORD");
								watchAlertTask.setKeywords(settings.get("watchalert.task"+i+".keywords"));
							}
							System.out.println("watchalert.task"+i+".period:" + settings.get("watchalert.task"+i+".period"));
							
							if(
								settings.get("watchalert.task"+i+".action.smtpserver") != null &&
								settings.get("watchalert.task"+i+".action.smtpfrom") != null &&
								settings.get("watchalert.task"+i+".action.recipients") != null &&
								settings.get("watchalert.task"+i+".action.smtpsubject") != null &&
								settings.get("watchalert.task"+i+".action.smtpbody") != null)
							{
								watchAlertTask.setEmailFlag("YES");
								watchAlertTask.setSmtpServer(settings.get("watchalert.task"+i+".action.smtpserver"));
								watchAlertTask.setSmtpFrom(settings.get("watchalert.task"+i+".action.smtpfrom"));
								watchAlertTask.setRecipients(settings.get("watchalert.task"+i+".action.recipients"));
								watchAlertTask.setSmtpSubject(settings.get("watchalert.task"+i+".action.smtpsubject"));
								watchAlertTask.setSmtpBody(settings.get("watchalert.task"+i+".action.smtpbody"));
								if(settings.get("watchalert.task"+i+".action.smtppassword")!= null)
									watchAlertTask.setSmtpPassword(settings.get("watchalert.task"+i+".action.smtppassword"));
							}
							
							if( settings.get("watchalert.task"+i+".action.httplink") != null &&
								settings.get("watchalert.task"+i+".action.httpbody") != null)
							{
								watchAlertTask.setHttpLink(settings.get("watchalert.task"+i+".action.httplink"));
								watchAlertTask.setHttpBody(settings.get("watchalert.task"+i+".action.httpbody"));
							}
							
							if(watchAlertTask.getCampareFlag().equals("NO_COMPARE"))
								System.out.println("No options found for the task "+i+". Please set up: watchalert.task"+i+".gt or watchalert.task"+i+".lt or watchalert.task"+i+".keywords");
							else if (watchAlertTask.getEmailFlag().equals("NO") && watchAlertTask.getHttpLink().length() == 0)
								System.out.println("No action defined for the task "+i+". Please set up MAIL or HTTP action, see documentation");
							else	
								watchAlertTaskList.add(watchAlertTask);
							printConfig(watchAlertTask, i);
						}
						else
							System.out.println("No required fields found for the task "+i+". Please set up: watchalert.task"+i+".indice, watchalert.task"+i+".querybody and watchalert.task"+i+".period");
				}			
			}
    	} catch (Exception e) {
    		System.out.println(e.toString());
		} 	
	}
	/**
	 * Printing found tasks. For debugging use.
	 * @param settings
	 */
	private void printConfig(WatchAlertTask watchAlertTask, int index)
	{
		try{
			System.out.println("---------------------START CONFIG----------------------------------------");
			//System.out.println.info("Found task " + index);
			System.out.println("Elastic Host: " + elasticHost);
			System.out.println("Elastic Port: " + elasticPort);
			System.out.println("Enable debug: " + enableDebug);
			System.out.println("Task httpAction: " + watchAlertTask.getHttpLink());
			System.out.println("Task httpBody: " + watchAlertTask.getHttpBody());
			//System.out.println("Task Indice: " + replaceKeywords(watchAlertTask.getIndice(), watchAlertTask, null));
			//System.out.println("Task Querybody: " + replaceKeywords(watchAlertTask.getQuerybody(), watchAlertTask, null));
			System.out.println("Task Period: " + watchAlertTask.getPeriod());
			System.out.println("Task Fields: " + watchAlertTask.getFields());
			System.out.println("Task Procedure: " + watchAlertTask.getProcedure());
			System.out.println("Task Keywords: " + watchAlertTask.getKeywords());
			System.out.println("Task GreaterThan: " + watchAlertTask.getGreaterThan());
			System.out.println("Task LessThan: " + watchAlertTask.getLessThan());
			System.out.println("Task TimeZoneDiff: " + watchAlertTask.getTimeZoneDiff());
			for(MapVariableValue replaceFields: watchAlertTask.getReplaceFields()) 
				System.out.println("Task Replace Fields: " + replaceFields.getValue() +" Pattern: " + replaceFields.getVariable());
			if(watchAlertTask.getEmailFlag().equals("YES"))
			{
				System.out.println("Task SMTP Server: " + watchAlertTask.getSmtpServer());
				System.out.println("Task SMTP From: " + watchAlertTask.getSmtpFrom());
				System.out.println("Task SMTP Password: " + watchAlertTask.getSmtpPassword());
				//System.out.println.info("Task recipients: " + watchAlertTask.getsm);
				System.out.println("Task SMTP Subject: " + watchAlertTask.getSmtpSubject());
				System.out.println("Task SMTP Body: " + watchAlertTask.getSmtpBody());
			}
			
			watchAlertTask.getFields().forEach(action -> System.out.println("Task field: " + action));
			watchAlertTask.getKeywords().forEach(action -> System.out.println("Task keyword: " + action));
			watchAlertTask.getRecipients().forEach(action -> System.out.println("Task recipient: " + action));
			watchAlertTask.getReplaceFields().forEach(action -> System.out.println("Task replace field: " +  action.getValue() + " with pattern: " + action.getVariable()));
			
			System.out.println("-------------------END CONFIG------------------------------------------");
    	} catch (Exception e) {
    		System.out.println(e.toString());
		} 
	}
	
	public List<WatchAlertTask> getWatchAlertTaskList() {
		return watchAlertTaskList;
	}

	public void setWatchAlertTaskList(List<WatchAlertTask> watchAlertTaskList) {
		this.watchAlertTaskList = watchAlertTaskList;
	}

	public String getElasticHost() {
		return elasticHost;
	}

	public void setElasticHost(String elasticHost) {
		this.elasticHost = elasticHost;
	}

	public String getElasticPort() {
		return elasticPort;
	}

	public void setElasticPort(String elasticPort) {
		this.elasticPort = elasticPort;
	}

	public String getEnableDebug() {
		return enableDebug;
	}

	public void setEnableDebug(String enableDebug) {
		this.enableDebug = enableDebug;
	}
}
