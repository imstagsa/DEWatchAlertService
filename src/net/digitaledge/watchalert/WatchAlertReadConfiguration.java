package net.digitaledge.watchalert;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * This calss is parsing config file.
 * @author esimacenco
 *
 */
public class WatchAlertReadConfiguration {
	
	
	private String elasticHost = new String("127.0.0.1");
	private String elasticPort = new String("9200");
	private String enableDebug = new String("false");
	private Integer maxTasks = new Integer(100);
	private WatchAlertSettings settings;
	private List<WatchAlertTask> watchAlertTaskList = new ArrayList<WatchAlertTask>();
	final static Logger logger = Logger.getLogger("WatchalertService");
	
	
	public WatchAlertReadConfiguration()
	{
		parseConfig();
	}
	
	private void parseConfig()
	{
		settings = new WatchAlertSettings();
		
		try{	
			
			if(settings.get("watchalert.elastichost") != null)
				elasticHost = settings.get("watchalert.elastichost");
			if(settings.get("watchalert.elasticport") != null)
				elasticPort = settings.get("watchalert.elasticport");
			if(settings.get("watchalert.enabledebug") != null)
				enableDebug = settings.get("watchalert.enabledebug");
			//checking configured tasks in config file
			for(int i = 1; i <= maxTasks; i++)
			{
				
				WatchAlertTask watchAlertTask = new WatchAlertTask(i);
				if(settings.get("watchalert.task"+i+".indice") != null && settings.get("watchalert.task"+i+".period") != null){
					
					watchAlertTask.setIndice(settings.get("watchalert.task"+i+".indice"));
					watchAlertTask.setPeriod(Integer.parseInt(settings.get("watchalert.task"+i+".period")));
					
					if(settings.get("watchalert.task"+i+".timeformat") != null)
						watchAlertTask.setTimeformat(settings.get("watchalert.task"+i+".timeformat"));
					
					if(settings.get("watchalert.task"+i+".timeZoneDiff") != null )
						watchAlertTask.setTimeZoneDiff(Integer.parseInt(settings.get("watchalert.task"+i+".timeZoneDiff")));
					
					if(settings.get("watchalert.task"+i+".action.smtpserver") != null &&
					   settings.get("watchalert.task"+i+".action.smtpfrom") != null &&
					   settings.get("watchalert.task"+i+".action.recipients") != null &&
					   settings.get("watchalert.task"+i+".action.smtpsubject") != null &&
					   settings.get("watchalert.task"+i+".action.sendalertemailbody") != null)
					   {
							watchAlertTask.setEmailFlag("YES");
							watchAlertTask.setSmtpServer(settings.get("watchalert.task"+i+".action.smtpserver"));
							watchAlertTask.setSmtpFrom(settings.get("watchalert.task"+i+".action.smtpfrom"));
							watchAlertTask.setRecipients(settings.get("watchalert.task"+i+".action.recipients"));
							watchAlertTask.setSmtpSubject(settings.get("watchalert.task"+i+".action.smtpsubject"));
							watchAlertTask.setSendAlertEmailBody(settings.get("watchalert.task"+i+".action.sendalertemailbody"));
							if(settings.get("watchalert.task"+i+".action.smtppassword")!= null)
								watchAlertTask.setSmtpPassword(settings.get("watchalert.task"+i+".action.smtppassword"));
							if(settings.get("watchalert.task"+i+".action.clearalertemailbody")!= null)
							{
								watchAlertTask.setClearAlertEmailBody(settings.get("watchalert.task"+i+".action.clearalertemailbody"));
								watchAlertTask.setStoreActiveState(1);
							}
						}
						
						if( settings.get("watchalert.task"+i+".action.sendalerthttplink") != null &&
							settings.get("watchalert.task"+i+".action.httpbody") != null)
						{
							watchAlertTask.setSendAlertHttpLink(settings.get("watchalert.task"+i+".action.sendalerthttplink"));
							watchAlertTask.setHttpBody(settings.get("watchalert.task"+i+".action.httpbody"));
							if(settings.get("watchalert.task"+i+".action.clearalerthttplink")!= null)
							{
								watchAlertTask.setClearAlertHttpLink(settings.get("watchalert.task"+i+".action.clearalerthttplink"));
								watchAlertTask.setStoreActiveState(1);
							}
						}
					
					//check all queries for this task 
					for(int y = 1; y <= maxTasks; y++)
					{
						WatchAlertTaskQuery watchAlertTaskQuery = new WatchAlertTaskQuery();
						
						if(settings.get("watchalert.task"+ i +".query"+ y +".querybody") != null)
						{
							logger.debug(settings.get("watchalert.task"+ i +".query"+ y +".querybody"));
							watchAlertTaskQuery.setQuerybody(settings.get("watchalert.task"+ i +".query"+ y +".querybody"));
						}
						if(settings.get("watchalert.task"+ i +".query"+ y +".fields") != null)
							watchAlertTaskQuery.setFields(settings.get("watchalert.task"+ i +".query"+ y +".fields"));
						if(settings.get("watchalert.task"+i+".query"+ y +".procedure") != null)
						{
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
					
					if (watchAlertTask.getEmailFlag().equals("NO") && watchAlertTask.getSendAlertHttpLink().length() == 0)
						logger.error("No action defined for the task "+ i +". Please set up EMAIL or HTTP action, see documentation.");
					else if(watchAlertTask.getTaskQueries().size() <= 0)
						logger.error("No queries defined for the task "+ i +". Please set up: watchalert.task"+i+".queryX.gt or watchalert.task"+i+".queryX.lt or watchalert.task"+i+".queryX.keywords.");
					else watchAlertTaskList.add(watchAlertTask);
					
					printConfig(watchAlertTask, i);
				}			
			}
    	} catch (Exception e) {
    		logger.error(e.toString());
		} 	
	}
	/**
	 * Printing found tasks. For debugging use.
	 * @param settings
	 */
	private void printConfig(WatchAlertTask watchAlertTask, int index)
	{
		
		try{
			logger.debug("---------------------START CONFIG TASK " + index +"----------------------------------------");
			logger.debug("Elastic Host: " + elasticHost);
			logger.debug("Elastic Port: " + elasticPort);
			logger.debug("Enable debug: " + enableDebug);
			logger.debug("Task "+index+" httpAction: " + watchAlertTask.getSendAlertHttpLink());
			logger.debug("Task "+index+" httpBody: " + watchAlertTask.getHttpBody());
			logger.debug("Task "+index+" Period: " + watchAlertTask.getPeriod());
			logger.debug("Task "+index+" TimeZoneDiff: " + watchAlertTask.getTimeZoneDiff());
			int i = 1;
			for(WatchAlertTaskQuery watchAlertTaskQuery: watchAlertTask.getTaskQueries())
			{
				logger.debug("Task "+index+" Query "+i+" Querybody: " + WatchAlertUtils.replaceKeywords(watchAlertTaskQuery.getQuerybody(), watchAlertTask, watchAlertTaskQuery, null));		
				logger.debug("Task "+index+" Query "+i+" Procedure: " + watchAlertTaskQuery.getProcedure());
				logger.debug("Task "+index+" Query "+i+" GreaterThan: " + watchAlertTaskQuery.getGreaterThan());
				logger.debug("Task "+index+" Query "+i+" LessThan: " + watchAlertTaskQuery.getLessThan());
				watchAlertTaskQuery.getFields().forEach(action -> logger.debug("Task "+index+" Query fields: " + action));
				watchAlertTaskQuery.getKeywords().forEach(action -> logger.debug("Task "+index+" Query keywords: " + action));
				watchAlertTaskQuery.getReplaceFields().forEach(action -> logger.debug("Task "+index+" Query Replace Fields: " + action.getValue() +" Pattern: " + action.getVariable()));
				i++;
			}
			
			logger.debug("Task "+index+" SMTP Server: " + watchAlertTask.getSmtpServer());
			logger.debug("Task "+index+" SMTP From: " + watchAlertTask.getSmtpFrom());
			logger.debug("Task "+index+" SMTP Password: " + watchAlertTask.getSmtpPassword());
			logger.debug("Task "+index+" SMTP Subject: " + watchAlertTask.getSmtpSubject());
			logger.debug("Task "+index+" SMTP Body: " + watchAlertTask.getSendAlertEmailBody());
			watchAlertTask.getRecipients().forEach(action -> logger.debug("Task "+index+" SMTP recipient: " + action));
			
			logger.debug("-------------------END CONFIG TASK " + index +"------------------------------------------");
    	} catch (Exception e) {
    		logger.error(e.toString());
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
