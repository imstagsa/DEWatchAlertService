package net.digitaledge.watchalert;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class WatchAlertsWorker implements Runnable {

	private WatchAlertReadConfiguration watchAlertConfig;
	final static Logger logger = Logger.getLogger("WatchalertService");
	
	/**
	 * Parsing configuration and creating list of tasks.
	 * @param settings
	 */
	public WatchAlertsWorker(WatchAlertSettings settings)
	{
		watchAlertConfig = new WatchAlertReadConfiguration(); 
	}
	
	private List<MapAlertStrings> findAlertInLogs(WatchAlertTaskQuery watchAlertTaskQuery, List<MapVariableValue> receivedNodes)
	{
		MapAlertStrings mapAlertStrings = new MapAlertStrings();
		Boolean ifAggregationStarted = false;
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>();
		logger.info("========================================================================================");
		
		for(int i = 0; i< receivedNodes.size(); i++)
		{
			if(receivedNodes.get(i).getVariable() != null)
			{
				
				if(receivedNodes.get(i).getVariable().equals("buckets"))
					ifAggregationStarted = true;
				
				if((receivedNodes.get(i).getVariable().equals("_index")|| ifAggregationStarted ) && mapAlertStrings.getAlertString().length() > 0 )
				{
					taskNodes.add(mapAlertStrings);
					mapAlertStrings = new MapAlertStrings();
				}
				
				mapAlertStrings.getAlertMapStrings().add(receivedNodes.get(i));
				
				for(String field: watchAlertTaskQuery.getFields())
				{
					if(receivedNodes.get(i).getVariable().equals(field))
					{
						if(receivedNodes.get(i).getValue() != null)
						{	
							if(watchAlertTaskQuery.getCampareFlag().equals("LESS_THAN"))
							{
								Double value1 = Double.parseDouble(receivedNodes.get(i).getValue());
								if(value1 < watchAlertTaskQuery.getLessThan())
								{
									mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									logger.info("Found less than value: " + value1 +" in " + watchAlertTaskQuery.getLessThan());
								}
							}
							else if(watchAlertTaskQuery.getCampareFlag().equals("GREATER_THAN"))
							{
								Double value1 = Double.parseDouble(receivedNodes.get(i).getValue());
								if(value1 > watchAlertTaskQuery.getGreaterThan())
								{
									mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									logger.info("Found greater than value: " + value1 +" in " + watchAlertTaskQuery.getGreaterThan() + " with field "+field);
								}
							}
							else if(watchAlertTaskQuery.getCampareFlag().equals("FIND_KEYWORD"))
							{
								for(String keyword: watchAlertTaskQuery.getKeywords())
									if(keyword.equals("*"))
										mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									else if(receivedNodes.get(i).getValue().contains(keyword))
										mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
							}
						}
					}
				}
			}
		}
		
		//Adding last alert
		if(mapAlertStrings.getAlertString().length() > 0)
			taskNodes.add(mapAlertStrings);
		
		logger.info("activeAlert length: " + taskNodes.size());
		return taskNodes;
	}
	
/*	private List<MapAlertStrings> numerixGetUserLoginsLocations(Object obj)
	{
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>(); 
		obj =  JSONUtils.findObject((JSONObject)obj, "aggregations");
		obj =  JSONUtils.findObject((JSONObject)obj, "userPrincipalName");
		List<JSONObject> listObjects = JSONUtils.findArrayObject((JSONObject)obj, "buckets");
		
		for (JSONObject jsonobject : listObjects)
		{
			obj =  JSONUtils.findObject(jsonobject, "key");
			if(!obj.equals("numerix.com"))
			{
				Object obj2 =  JSONUtils.findObject(jsonobject, "location");
				List<JSONObject> listCities = JSONUtils.findArrayObject((JSONObject)obj2, "buckets");
				MapAlertStrings mapAlertStrings = new MapAlertStrings();
				mapAlertStrings.setAlertString(obj.toString());
				
				MapVariableValue mapVariableValue = new MapVariableValue("ATTEMPTS");
				logger.debug(mapAlertStrings.getAlertString());
				for (JSONObject jsonobject2 : listCities)
				{
					Object obj3 =  JSONUtils.findObject(jsonobject2, "key");
					mapVariableValue.setValue(mapVariableValue.getValue() + " " + obj3.toString());
				}
				mapAlertStrings.getAlertMapStrings().add(mapVariableValue);
				taskNodes.add(mapAlertStrings);
			}
		}
		return taskNodes;
	}*/
	
	private void parseJSON(WatchAlertTask watchAlertTask)
	{
		
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>();
		WatchAlertTaskQuery watchAlertTaskQueryTmp = new WatchAlertTaskQuery();
		for(WatchAlertTaskQuery watchAlertTaskQuery : watchAlertTask.getTaskQueries())
		{
			
			String json = new String();
			watchAlertTaskQueryTmp = watchAlertTaskQuery;
			
			if(taskNodes.size() == 0)
			{
				json = getNewLogsFromElasticsearch(watchAlertTask, watchAlertTaskQuery, null);
				JSONUtils jSONParser = new JSONUtils();
				Object obj =  jSONParser.parse(json);
				//if(watchAlertTaskQuery.getProcedure().equals("numerixGetUserLoginsLocations"))
				//	taskNodes = numerixGetUserLoginsLocations(obj);
				//else
				//{
					List<MapVariableValue> receivedNodes = JSONUtils.convertToMapVariableValue((JSONObject)obj);
					taskNodes = findAlertInLogs(watchAlertTaskQuery, receivedNodes);
				//}
			}
			else
			{
				List<MapAlertStrings> taskNodesTmp1 = new ArrayList<MapAlertStrings>();			
				for(MapAlertStrings mapAlertStrings : taskNodes)
				{
					List<MapAlertStrings> taskNodesTmp2 = new ArrayList<MapAlertStrings>();
					json = getNewLogsFromElasticsearch(watchAlertTask, watchAlertTaskQuery, mapAlertStrings);			
					JSONUtils jSONParser = new JSONUtils();
					Object obj =  jSONParser.parse(json);
					//if(watchAlertTaskQuery.getProcedure().equals("numerixGetUserLoginsLocations"))
					//	taskNodesTmp2 = numerixGetUserLoginsLocations(obj);
					//else
					//{
						List<MapVariableValue> receivedNodes = JSONUtils.convertToMapVariableValue((JSONObject)obj);
						taskNodesTmp2 = findAlertInLogs(watchAlertTaskQuery, receivedNodes);
					//}
					taskNodesTmp1.addAll(taskNodesTmp2);
				}
				taskNodes = taskNodesTmp1;
			}
			
			List<MapAlertStrings> mapsForDeletion = new ArrayList<MapAlertStrings>();
			for(MapAlertStrings mapAlertStrings : taskNodes)
			{
				for(MapVariableValue mapVariableValue : mapAlertStrings.getAlertMapStrings())
				{
					if(mapVariableValue.getVariable().equals("userPrincipalName") && mapVariableValue.getValue().contains("@"))
						mapVariableValue.setValue(mapVariableValue.getValue().split("@")[0]);
					if(mapVariableValue.getValue().equals("numerix.com"))
						mapsForDeletion.add(mapAlertStrings);
				}
			}
			
			for(MapAlertStrings mapAlertStrings : mapsForDeletion)
				taskNodes.remove(mapAlertStrings);
		}
		
		logger.info("taskNodes length: " + taskNodes.size());
		WatchAlertInformer informer = new WatchAlertInformer();
		informer.notify(taskNodes, watchAlertTask, watchAlertTaskQueryTmp);
	}
	
	private String getNewLogsFromElasticsearch(WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery, MapAlertStrings taskNode) {
		String json = new String();
	    Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
	    
		String urlParameters = new String();
		if(taskNode == null)
			urlParameters = WatchAlertUtils.replaceKeywords(watchAlertTaskQuery.getQuerybody(), watchAlertTask, watchAlertTaskQuery, null);
		else
			urlParameters = WatchAlertUtils.replaceKeywords(watchAlertTaskQuery.getQuerybody(), watchAlertTask, watchAlertTaskQuery, taskNode.getAlertMapStrings());
	    	
        StringBuilder stringBuilder = new StringBuilder("http://"+watchAlertConfig.getElasticHost()+":" + watchAlertConfig.getElasticPort() + WatchAlertUtils.replaceKeywords(watchAlertTask.getIndice(), watchAlertTask, watchAlertTaskQuery, null));
        StringBuffer response = new StringBuffer();
        try{
        	URL obj = new URL(stringBuilder.toString());
        	HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        	con.setRequestMethod("GET");
        	con.setRequestProperty("User-Agent", "java");
        	con.setRequestProperty("Accept-Charset", "UTF-8");
        	con.setRequestProperty("Accept", "*/*");
        	con.setRequestProperty("Content-Type","application/json");
        	con.setDoOutput(true);
        	
        	logger.debug("urlParameters: " + urlParameters);
        	
        	if(urlParameters.length() > 0){
        		urlParameters = urlParameters + "\r\n";
        		con.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
        		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        		wr.writeBytes(urlParameters);
        		wr.flush();
        		wr.close();
        	}
        	
        	int responseCode = con.getResponseCode();
        	if(responseCode!=200)
        	{
        		logger.error("Response Code: " + responseCode + ". Something wrong!");
        		return new String("{}");
        	}
        	
        	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        	while ((json = in.readLine()) != null) {
        		response.append(json);
        	}
        	in.close();
        } catch(Exception e){ logger.info(e.toString()); }
        
        logger.info("Spend time: " + (timestamp1.getTime() - timestamp1.getTime()) + "ms");
        logger.debug("JSON: " + response.toString());
        return response.toString();
	}
	
	public void executeJob()
	{
		Long now = WatchAlertUtils.getEpochTime();

		for(WatchAlertTask watchAlertTask : watchAlertConfig.getWatchAlertTaskList())
		{
				logger.info("Task next timestamp: " + watchAlertTask.getNextExecuteTime() + " and now timestamp: " + now);
				if(watchAlertTask.getNextExecuteTime() <= now)
				{
					logger.info("Executing getNewLogs for TASK: " + watchAlertTask.getTaskNumber());
					
					parseJSON(watchAlertTask);
					watchAlertTask.setNextExecuteTime(WatchAlertUtils.getEpochTime() + watchAlertTask.getPeriod());
				}
		}
	}
	
	@Override
	public void run() 
	{
		logger.info("WatchAlertsWorker run");
		while (true) 
		{
			logger.info("executeJob()");
			executeJob();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}
		}
	}

}
