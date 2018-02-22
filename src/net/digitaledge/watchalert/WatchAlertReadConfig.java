package net.digitaledge.watchalert;

import java.util.ArrayList;
import java.util.List;

/**
 * This calss is parsing config file.
 * @author esimacenco
 *
 */
public class WatchAlertReadConfig {
	
	
	private String elasticHost = new String("127.0.0.1");
	private String elasticPort = new String("9200");
	private String enableDebug = new String("false");
	private Integer maxTasks = new Integer(20);
	private WatchAlertSettings settings;
	private List<WatchAlertTask> watchAlertTaskList = new ArrayList<WatchAlertTask>();
	
	public WatchAlertReadConfig()
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
			//check configured tasks in config file
			for(int i = 1; i <= maxTasks; i++)
			{
				
				WatchAlertTask watchAlertTask = new WatchAlertTask(i);
				if(settings.get("watchalert.task"+i+".indice") != null &&
					//settings.get("watchalert.task"+i+".querybody") != null &&
					settings.get("watchalert.task"+i+".period") != null){
					
					watchAlertTask.setIndice(settings.get("watchalert.task"+i+".indice"));
					watchAlertTask.setPeriod(Integer.parseInt(settings.get("watchalert.task"+i+".period")));
					
					if(settings.get("watchalert.task"+i+".timeformat") != null)
						watchAlertTask.setTimeformat(settings.get("watchalert.task"+i+".timeformat"));
					
					if(settings.get("watchalert.task"+i+".timeZoneDiff") != null )
						watchAlertTask.setTimeZoneDiff(Integer.parseInt(settings.get("watchalert.task"+i+".timeZoneDiff")));
					
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
					
					//check all queries for this task 
					for(int y = 1; y <= maxTasks; y++)
					{
						//boolean foundAllRequiredFields = false;
						WatchAlertTaskQuery watchAlertTaskQuery = new WatchAlertTaskQuery();
						
						
						if(settings.get("watchalert.task"+ i +".query"+ y +".querybody") != null)
						{
							System.out.println(settings.get("watchalert.task"+ i +".query"+ y +".querybody"));
							watchAlertTaskQuery.setQuerybody(settings.get("watchalert.task"+ i +".query"+ y +".querybody"));
						}
						if(settings.get("watchalert.task"+ i +".query"+ y +".fields") != null)
							watchAlertTaskQuery.setFields(settings.get("watchalert.task"+ i +".query"+ y +".fields"));
						if(settings.get("watchalert.task"+i+".query"+ y +".procedure") != null)
						{
							//foundAllRequiredFields = true;
							watchAlertTaskQuery.setCampareFlag("OTHER");
							watchAlertTaskQuery.setProcedure(settings.get("watchalert.task"+i+".query"+ y +".procedure"));
						}

						if(settings.get("watchalert.task"+i+".query"+ y +".gt") != null )
						{
							watchAlertTaskQuery.setCampareFlag("GREATER_THAN");
							String str = settings.get("watchalert.task"+ i +".query"+ y +".gt");
							watchAlertTaskQuery.setGreaterThan(Integer.parseInt(str.trim()));
						}
						else if(settings.get("watchalert.task"+ i +".query"+ y +".lt") != null )
						{
							watchAlertTaskQuery.setCampareFlag("LESS_THAN");
							String str = settings.get("watchalert.task"+ i +".query"+ y +".lt");
							watchAlertTaskQuery.setLessThan(Integer.parseInt(str.trim()));
						}
						else if(settings.get("watchalert.task"+ i +".query"+ y +".keywords")!= null)
						{
							watchAlertTaskQuery.setCampareFlag("FIND_KEYWORD");
							watchAlertTaskQuery.setKeywords(settings.get("watchalert.task"+i+".query"+ y +".keywords"));
						}
						
						if(settings.get("watchalert.task"+i+".query"+ y +".replaceFields") != null )
							watchAlertTaskQuery.setReplaceFields(settings.get("watchalert.task"+i+".query"+ y +".replaceFields"));
						
						if(watchAlertTaskQuery.getQuerybody().length() > 0)
							watchAlertTask.getTaskQueries().add(watchAlertTaskQuery);
						
					}
					
					if (watchAlertTask.getEmailFlag().equals("NO") && watchAlertTask.getHttpLink().length() == 0)
						System.out.println("No action defined for the task "+ i +". Please set up MAIL or HTTP action, see documentation");
					else if(watchAlertTask.getTaskQueries().size() <= 0)
						System.out.println("No queries defined for the task "+ i +". Please set up: watchalert.task"+i+".queryX.gt or watchalert.task"+i+".queryX.lt or watchalert.task"+i+".queryX.keywords.");
					else watchAlertTaskList.add(watchAlertTask);
					
					printConfig(watchAlertTask, i);
					
						//if(foundAllRequiredFields)
						//{
							//System.out.println("foundAllRequiredFileds " + foundAllRequiredFields +" " + i);
							//System.out.println("watchalert.task"+i+".period:" + settings.get("watchalert.task"+i+".period"));
						//}
					//else
					//	System.out.println("No required fields found for the task "+i+". Please set up: watchalert.task"+i+".indice, watchalert.task"+i+".querybody and watchalert.task"+i+".period");
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
			System.out.println("Task "+index+" httpAction: " + watchAlertTask.getHttpLink());
			System.out.println("Task "+index+" httpBody: " + watchAlertTask.getHttpBody());
			//System.out.println("Task Indice: " + replaceKeywords(watchAlertTask.getIndice(), watchAlertTask, null));
			System.out.println("Task "+index+" Period: " + watchAlertTask.getPeriod());
			System.out.println("Task "+index+" TimeZoneDiff: " + watchAlertTask.getTimeZoneDiff());
			int i = 1;
			for(WatchAlertTaskQuery watchAlertTaskQuery: watchAlertTask.getTaskQueries())
			{
				System.out.println("Task "+index+" Query "+i+" Querybody: " + WatchAlertUtils.replaceKeywords(watchAlertTaskQuery.getQuerybody(), watchAlertTask, watchAlertTaskQuery, null));		
				System.out.println("Task "+index+" Query "+i+" Procedure: " + watchAlertTaskQuery.getProcedure());
				System.out.println("Task "+index+" Query "+i+" GreaterThan: " + watchAlertTaskQuery.getGreaterThan());
				System.out.println("Task "+index+" Query "+i+" LessThan: " + watchAlertTaskQuery.getLessThan());
				watchAlertTaskQuery.getFields().forEach(action -> System.out.println("Task "+index+" Query fields: " + action));
				watchAlertTaskQuery.getKeywords().forEach(action -> System.out.println("Task "+index+" Query keywords: " + action));
				watchAlertTaskQuery.getReplaceFields().forEach(action -> System.out.println("Task "+index+" Query Replace Fields: " + action.getValue() +" Pattern: " + action.getVariable()));
				i++;
			}
			
			if(watchAlertTask.getEmailFlag().equals("YES"))
			{
				System.out.println("Task "+index+" SMTP Server: " + watchAlertTask.getSmtpServer());
				System.out.println("Task "+index+" SMTP From: " + watchAlertTask.getSmtpFrom());
				System.out.println("Task "+index+" SMTP Password: " + watchAlertTask.getSmtpPassword());
				//System.out.println.info("Task recipients: " + watchAlertTask.getsm);
				System.out.println("Task "+index+" SMTP Subject: " + watchAlertTask.getSmtpSubject());
				System.out.println("Task "+index+" SMTP Body: " + watchAlertTask.getSmtpBody());
				watchAlertTask.getRecipients().forEach(action -> System.out.println("Task "+index+" SMTP recipient: " + action));
			}
			
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
