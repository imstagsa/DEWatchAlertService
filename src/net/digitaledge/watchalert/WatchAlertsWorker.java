package net.digitaledge.watchalert;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public class WatchAlertsWorker implements Runnable {

	//private String enableDebug = new String("false");
	private WatchAlertReadConfig watchAlertConfig;
	private int arrayList = 0;
	private Boolean jsonStrated = false;
	//private static Logger LOGGER = Logger.getLogger("InfoLogging");
	
	/**
	 * Parsing configuration and creating list of tasks.
	 * @param settings
	 */
	public WatchAlertsWorker(WatchAlertSettings settings)
	{
		watchAlertConfig = new WatchAlertReadConfig(); 
	}
	
	private List<MapAlertStrings> findAlertInLogs(WatchAlertTaskQuery watchAlertTaskQuery, List<MapVariableValue> receivedNodes)
	{
		MapAlertStrings mapAlertStrings = new MapAlertStrings();
		Boolean ifAggregationStarted = false;
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>();
		System.out.println("========================================================================================");
		
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
									//activeAlert.add(receivedNodes.get(i).getValue());
									mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									System.out.println("Found less than value: " + value1 +" in " + watchAlertTaskQuery.getLessThan());
								}
							}
							else if(watchAlertTaskQuery.getCampareFlag().equals("GREATER_THAN"))
							{
								Double value1 = Double.parseDouble(receivedNodes.get(i).getValue());
								if(value1 > watchAlertTaskQuery.getGreaterThan())
								{
									//activeAlert.add(receivedNodes.get(i).getValue());
									mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									System.out.println("Found greater than value: " + value1 +" in " + watchAlertTaskQuery.getGreaterThan() + " with field "+field);
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
		{
			taskNodes.add(mapAlertStrings);
			System.out.println("Added last alert task");
		}
		
		System.out.println("activeAlert length: " + taskNodes.size());
		return taskNodes;
	}
	
	private List<MapAlertStrings> numerixGetUserLoginsLocations(Object obj)
	{
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>(); 
		obj =  JSONUtils.findObject((JSONObject)obj, "aggregations");
		obj =  JSONUtils.findObject((JSONObject)obj, "userPrincipalName");
		List<JSONObject> listObjects = JSONUtils.findArrayObject((JSONObject)obj, "buckets");
		
		for (JSONObject jsonobject : listObjects)
		{
			obj =  JSONUtils.findObject(jsonobject, "key");
			if(!obj.equals("numerix.com"))
			{//System.out.println("key: " + obj.toString());
				Object obj2 =  JSONUtils.findObject(jsonobject, "location");
				List<JSONObject> listCities = JSONUtils.findArrayObject((JSONObject)obj2, "buckets");
				MapAlertStrings mapAlertStrings = new MapAlertStrings();
				mapAlertStrings.setAlertString(obj.toString());
				//MapVariableValue mapVariableValue = new MapVariableValue("user",obj.toString());
				
				MapVariableValue mapVariableValue = new MapVariableValue("ATTEMPTS");
				System.out.println(mapAlertStrings.getAlertString());
				for (JSONObject jsonobject2 : listCities)
				{
					Object obj3 =  JSONUtils.findObject(jsonobject2, "key");
					mapVariableValue.setValue(mapVariableValue.getValue() + " " + obj3.toString());
					//System.out.println(mapVariableValue.getVariable() + " ==1 " + mapVariableValue.getValue());
				}
				mapAlertStrings.getAlertMapStrings().add(mapVariableValue);
				taskNodes.add(mapAlertStrings);
			}
		}
		return taskNodes;
	}
	
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
				json = getNewLogs(watchAlertTask, watchAlertTaskQuery, null);
				JSONUtils jSONParser = new JSONUtils();
				Object obj =  jSONParser.parse(json);
				if(watchAlertTaskQuery.getProcedure().equals("numerixGetUserLoginsLocations"))
					taskNodes = numerixGetUserLoginsLocations(obj);
				else
				{
					List<MapVariableValue> receivedNodes = JSONUtils.convertToMapVariableValue((JSONObject)obj);
					taskNodes = findAlertInLogs(watchAlertTaskQuery, receivedNodes);
				}
			}
			else
			{
				List<MapAlertStrings> taskNodesTmp1 = new ArrayList<MapAlertStrings>();			
				for(MapAlertStrings mapAlertStrings : taskNodes)
				{
					List<MapAlertStrings> taskNodesTmp2 = new ArrayList<MapAlertStrings>();
					json = getNewLogs(watchAlertTask, watchAlertTaskQuery, mapAlertStrings);			
					JSONUtils jSONParser = new JSONUtils();
					Object obj =  jSONParser.parse(json);
					if(watchAlertTaskQuery.getProcedure().equals("numerixGetUserLoginsLocations"))
						taskNodesTmp2 = numerixGetUserLoginsLocations(obj);
					else
					{
						List<MapVariableValue> receivedNodes = JSONUtils.convertToMapVariableValue((JSONObject)obj);
						taskNodesTmp2 = findAlertInLogs(watchAlertTaskQuery, receivedNodes);
					}
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
		
		//for(MapAlertStrings mapAlertStrings: taskNodes)
		//	mapAlertStrings.getAlertMapStrings().forEach(action -> System.out.println(action.getVariable()+"==="+action.getValue()));
		
		WatchAlertInformer informer = new WatchAlertInformer();
		informer.notify(taskNodes, watchAlertTask, watchAlertTaskQueryTmp);
	}
	
	
	private String getNewLogs(WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery, MapAlertStrings taskNode) {
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
        	if(urlParameters.length() > 0){
        		urlParameters = urlParameters + "\r\n";
        		con.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
        		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        		wr.writeBytes(urlParameters);
        		wr.flush();
        		wr.close();
        	}
        	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        	while ((json = in.readLine()) != null) {
        		response.append(json);
        	}
        	in.close();
        	System.out.println(response.toString());
        } catch(Exception e){System.out.println(e.toString());}
        
        System.out.println("Spend time: " + (timestamp1.getTime() - timestamp1.getTime()) + "ms");
        return response.toString();
	}
	
	private String getNewLogsOld(WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery, MapAlertStrings taskNode)
	{

		try {
			Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
			System.out.println("getNewLogs sending request to socket.");
			String urlParameters = new String();
			if(taskNode == null)
				urlParameters = WatchAlertUtils.replaceKeywords(watchAlertTaskQuery.getQuerybody(), watchAlertTask, watchAlertTaskQuery, null);
			else
				urlParameters = WatchAlertUtils.replaceKeywords(watchAlertTaskQuery.getQuerybody(), watchAlertTask, watchAlertTaskQuery, taskNode.getAlertMapStrings());
			System.out.println(urlParameters);
			//byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
			Socket socket = new Socket(InetAddress.getByName(watchAlertConfig.getElasticHost()), Integer.parseInt(watchAlertConfig.getElasticPort()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.print("XGET " + WatchAlertUtils.replaceKeywords(watchAlertTask.getIndice(), watchAlertTask, watchAlertTaskQuery, null) + " HTTP/1.1\r\n");
			pw.print("Host: "+ InetAddress.getByName(watchAlertConfig.getElasticHost())+":"+Integer.parseInt(watchAlertConfig.getElasticPort())+"\r\n");
			pw.print("Accept: */*\r\n");
			//pw.print("Content-Length: " + Integer.toString(postData.length) +"\r\n");
			pw.print("Content-Type: application/x-www-form-urlencoded\r\n");
			pw.print("\r\n");
			pw.print(urlParameters);
			pw.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			arrayList = 0;
			jsonStrated = false;
			String t, json = new String();

			System.out.println("getNewLogs starting receiving from socket.");		
			
			while((t = br.readLine()) != null)
			{			
				String str = t.trim();
				//System.out.println("Line " + str);
				
				for (int y=0; y<str.length(); y++)
				{
					if(str.charAt(y) == '{')
					{
						jsonStrated = true;
						arrayList += 1;
					}
					if(str.charAt(y) == '}')
						arrayList -= 1;
				}
										
				if(jsonStrated) json = json + str;
				
				if(jsonStrated && arrayList <= 0)
				{
					jsonStrated = false;
					System.out.println("Break, arrayList = " + arrayList);
					break;
				}
			}
			
			br.close();
			if(socket.isConnected())
				socket.close();
			
			Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
			System.out.println("Spend time: " + (timestamp2.getTime() - timestamp1.getTime()) + "ms");
			
			return json;
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		
		return null;
	}
		
	public void executeJob()
	{
		Long now = WatchAlertUtils.getEpochTime();

		for(WatchAlertTask watchAlertTask : watchAlertConfig.getWatchAlertTaskList())
		{
				System.out.println("Task next timestamp: " + watchAlertTask.getNextExecuteTime() + " and now timestamp: " + now);
				if(watchAlertTask.getNextExecuteTime() <= now)
				{
					System.out.println("Executing getNewLogs for TASK: " + watchAlertTask.getTaskNumber());
					
					parseJSON(watchAlertTask);
					watchAlertTask.setNextExecuteTime(WatchAlertUtils.getEpochTime() + watchAlertTask.getPeriod());
				}
		}
	}
	
	@Override
	public void run() 
	{
		System.out.println("WatchAlertsWorker run");
		while (true) 
		{
			System.out.println("executeJob()");
			executeJob();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				System.out.println(e.toString());
			}
		}
	}

}
