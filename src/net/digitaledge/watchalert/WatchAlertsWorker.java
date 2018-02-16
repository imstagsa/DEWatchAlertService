package net.digitaledge.watchalert;

import java.io.BufferedReader;
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
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.simple.JSONObject;

public class WatchAlertsWorker implements Runnable {

	private String enableDebug = new String("false");
	private WatchAlertConfig watchAlertConfig;
	private int arrayList = 0, key = 0;
	
	//
	private Boolean jsonStrated = false, parseValue = false;
	//private static Logger LOGGER = Logger.getLogger("InfoLogging");
	
	/**
	 * Parsing configuration and creating list of tasks.
	 * @param settings
	 */
	public WatchAlertsWorker(WatchAlertSettings settings)
	{
		watchAlertConfig = new WatchAlertConfig(); 
	}

	/**
	 * Sends alert to EMS.
	 */
	private void sendAlertViaHTTP(WatchAlertTask watchAlertTask, String alertString, List<MapVariableValue> nodes)
	{
		URL url;	
    	try {
    		
    		String alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getHttpBody(), watchAlertTask, nodes);
    		String alertBody = watchAlertTask.getHttpLink();
    		
    		if(watchAlertTask.getHttpBody().length() > 0)  			
    			alertMessage = alertMessage.replaceAll("%MESSAGE%", alertString);    			
    		else alertMessage = alertString;
    		
    		alertBody = alertBody.replaceAll("%MESSAGE%", alertMessage);
    		alertBody = alertBody.replaceAll(" ", "%20");
     		url = new URL(alertBody);
     		System.out.println("Alert string: " + alertBody);
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
    		connection.setRequestMethod("GET");
    		connection.connect();
    		System.out.println("Sent Alert: " + connection.getResponseCode());
    		connection.disconnect();
    	} catch (Exception e) {
    		System.out.println(e.toString());
		} 	
	}
	
	public void sendEmailWithoutAuth(final WatchAlertTask watchAlertTask, String alertBody, List<MapVariableValue> nodes)
	{
		try{
			System.out.println("BEFORE SENDING EMAIL");
			String[] stringArray = watchAlertTask.getSmtpServer().split(":");
			String smtpServer = stringArray[0];
			String smtpPort = new String("25");
			String alertMessage = new String();
			
			if(stringArray.length == 2)  smtpPort = stringArray[1];			 	 
			
			alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getSmtpBody(), watchAlertTask, nodes);
			alertMessage = alertMessage.replaceAll("%MESSAGE%", alertBody);
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpServer);
			if (enableDebug.equals("true")) props.put("mail.debug", "true");
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.socketFactory.fallback", "true");
			props.put("mail.smtp.port", smtpPort);
			props.put("java.net.preferIPv4Stack", "true");
			Session session = Session.getInstance(props);
			MimeMessage msg = new MimeMessage(session);
			
			
			
			for(String addressstr : watchAlertTask.getRecipients())
			{
				addressstr = WatchAlertUtils.replaceKeywords(addressstr, watchAlertTask, nodes);
				String[] addressstr2 = addressstr.split(":");
				
				InternetAddress internetAddress = new InternetAddress(addressstr2[1]);
				if(addressstr2.length > 1)
				{
					if(addressstr2[0].toLowerCase().equals("to"))
						msg.addRecipient(MimeMessage.RecipientType.TO, internetAddress);
					else if(addressstr2[0].toLowerCase().equals("cc"))
						msg.addRecipient(MimeMessage.RecipientType.CC, internetAddress);
					else if(addressstr2[0].toLowerCase().equals("bcc"))
						msg.addRecipient(MimeMessage.RecipientType.BCC, internetAddress);
					else System.out.println("Cannot find prefix in \'" +addressstr+"\' . Should be one of the to, cc or bcc.");	        			
				}
				else
					System.out.println("Cannot parse address \'" +addressstr+"\' . Should be in format: pefix:email@domain");
			}  
		
        	//for(int i = 0; i< msg.getAllRecipients().length; i++)
        	//	System.out.println("Sending email to " + msg.getAllRecipients()[i].toString());
        	
			msg.setFrom(new InternetAddress(watchAlertTask.getSmtpFrom()));
			msg.setSubject(watchAlertTask.getSmtpSubject());
			msg.setText(alertMessage, "utf-8", "html");
			Transport transport = session.getTransport();
			transport.connect();
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
			System.out.println("AFTER SENDING EMAIL");
		}catch (MessagingException mex) {
			System.out.println(mex.toString());
		}
	}
	
	/**
	 * Sending emails with authorization.
	 * @param watchAlertTask
	 */
	public void sendEmailWithAuth(final WatchAlertTask watchAlertTask, String alertBody, List<MapVariableValue> nodes)
	{
		 
		Transport trans=null;
		String[] stringArray = watchAlertTask.getSmtpServer().split(":");
		String smtpServer = stringArray[0];
		String smtpPort = new String("25");
		if(stringArray.length == 2)
			smtpPort = stringArray[1];
		
		String alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getSmtpBody(), watchAlertTask, nodes);
		alertMessage = alertMessage.replaceAll("%MESSAGE%", alertBody);
		
        Properties props = new Properties();  
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", smtpServer); 
        props.put("mail.smtp.port", smtpPort); 
        props.put("mail.smtp.auth", "true");  
        props.put("mail.debug", "true");              

        Session session =  
            Session.getInstance(props, new javax.mail.Authenticator() {  
                protected PasswordAuthentication getPasswordAuthentication() {  
                    return new PasswordAuthentication(watchAlertTask.getSmtpFrom(), watchAlertTask.getSmtpPassword());  
                }  
            }); 

        try {
        	MimeMessage msg = new MimeMessage(session);             
        	InternetAddress addressFrom = new InternetAddress(watchAlertTask.getSmtpFrom());  
        	msg.setFrom(addressFrom);
        	       	
        	for(String addressstr : watchAlertTask.getRecipients())
        	{
				addressstr = WatchAlertUtils.replaceKeywords(addressstr, watchAlertTask, nodes);
				//System.out.println("=============================================================================================");
				//System.out.println("addressstr: " + addressstr);
				
        		String[] addressstr2 = addressstr.split(":");
        		InternetAddress internetAddress = new InternetAddress(addressstr2[1]);
        		if(addressstr2.length > 1)
        		{
        			if(addressstr2[0].toLowerCase().equals("to"))
        				msg.addRecipient(MimeMessage.RecipientType.TO,internetAddress);
        			else if(addressstr2[0].toLowerCase().equals("cc"))
        				msg.addRecipient(MimeMessage.RecipientType.CC, internetAddress);
        			else if(addressstr2[0].toLowerCase().equals("bcc"))
        				msg.addRecipient(MimeMessage.RecipientType.BCC, internetAddress);
        			else System.out.println("Cannot find prefix in \'" +addressstr+"\' . Should be one of the to, cc or bcc.");	        			
        		}
        		else
        			System.out.println("Cannot parse address \'" +addressstr+"\' . Should be in format: pefix:email@domain");
        	}     
        	       	
        	//for(int i = 0; i< msg.getAllRecipients().length; i++)
        	//	System.out.println("Sending email to " + msg.getAllRecipients()[i].toString());
        	
        	msg.setSubject(watchAlertTask.getSmtpSubject());  
        	msg.setText(alertMessage, "utf-8", "html");
        	trans = session.getTransport("smtp");
        	trans.connect(smtpServer, watchAlertTask.getSmtpFrom(), watchAlertTask.getSmtpPassword());
        	msg.saveChanges();
        	trans.sendMessage(msg, msg.getAllRecipients());
        	trans.close();
        	System.out.println("Email successfully sent.");
        }
        catch (MessagingException mex) {
        	System.out.println(mex.toString());
        }
	}
	
	private List<MapAlertStrings> findAlertInLogs(WatchAlertTask watchAlertTask, List<MapVariableValue> receivedNodes)
	{
		MapAlertStrings mapAlertStrings = new MapAlertStrings();
		Boolean ifAggregationStarted = false;
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>();
		System.out.println("========================================================================================");
		
		
		for(int i = 0; i< receivedNodes.size(); i++)
		{
			//System.out.println("Node key: " + i + "  Variable: " + receivedNodes.get(i).getVariable() + "  value: " + receivedNodes.get(i).getValue());
			if(receivedNodes.get(i).getVariable() != null)
			{
				
				if(receivedNodes.get(i).getVariable().equals("buckets"))
				{
					ifAggregationStarted = true;
					System.out.println("ifAggregationStarted = TRUE");
				}
				
				if((receivedNodes.get(i).getVariable().equals("_index")|| ifAggregationStarted ) && mapAlertStrings.getAlertString().length() > 0 )
				{
					taskNodes.add(mapAlertStrings);
					System.out.println("Added alert task");
					mapAlertStrings = new MapAlertStrings();
				}
				
				mapAlertStrings.getAlertMapStrings().add(receivedNodes.get(i));
				
				for(String field: watchAlertTask.getFields())
				{
					if(receivedNodes.get(i).getVariable().equals(field))
					{
						if(receivedNodes.get(i).getValue() != null)
						{	
							if(watchAlertTask.getCampareFlag().equals("LESS_THAN"))
							{
								Double value1 = Double.parseDouble(receivedNodes.get(i).getValue());
								if(value1 < watchAlertTask.getLessThan())
								{
									//activeAlert.add(receivedNodes.get(i).getValue());
									mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									System.out.println("Found less than value: " + value1 +" in " + watchAlertTask.getLessThan());
								}
							}
							else if(watchAlertTask.getCampareFlag().equals("GREATER_THAN"))
							{
								Double value1 = Double.parseDouble(receivedNodes.get(i).getValue());
								if(value1 > watchAlertTask.getGreaterThan())
								{
									//activeAlert.add(receivedNodes.get(i).getValue());
									mapAlertStrings.setAlertString(receivedNodes.get(i).getValue());
									System.out.println("Found greater than value: " + value1 +" in " + watchAlertTask.getGreaterThan());
								}
							}
							else if(watchAlertTask.getCampareFlag().equals("FIND_KEYWORD"))
							{
								for(String keyword: watchAlertTask.getKeywords())
									if(receivedNodes.get(i).getValue().contains(keyword))
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
	
	private void parseJSON(WatchAlertTask watchAlertTask, String json)
	{
		JSONUtils jSONParser = new JSONUtils();
		Object obj =  jSONParser.parse(json);
		List<MapAlertStrings> taskNodes = new ArrayList<MapAlertStrings>();

		if(watchAlertTask.getProcedure().equals("numerixGetUserLoginsLocations"))
		{
			System.out.println("1");
			taskNodes = numerixGetUserLoginsLocations(obj);
		}
		else
		{
			System.out.println("2");
			List<MapVariableValue> receivedNodes = JSONUtils.convertToMapVariableValue((JSONObject)obj);
			taskNodes = findAlertInLogs(watchAlertTask, receivedNodes);
		}
		
		for(MapAlertStrings alertBody : taskNodes)
		{
			System.out.println("ALERT: " + alertBody.getAlertString());
			//for(MapVariableValue mapVariableValue : alertBody.getAlertMapStrings())
				//System.out.println("mapVariableValue: " + mapVariableValue.getVariable() + ": " + mapVariableValue.getValue());
			
			System.out.println("watchAlertTask.getEmailFlag(): " + watchAlertTask.getEmailFlag());
			if(watchAlertTask.getEmailFlag().equals("YES"))
				if(watchAlertTask.getSmtpPassword().length()>0) sendEmailWithAuth(watchAlertTask, alertBody.getAlertString(), alertBody.getAlertMapStrings());
				else sendEmailWithoutAuth(watchAlertTask, alertBody.getAlertString(), alertBody.getAlertMapStrings());
			if(watchAlertTask.getHttpLink().length() > 1)
				sendAlertViaHTTP(watchAlertTask, alertBody.getAlertString(), alertBody.getAlertMapStrings());
		}
	}
	
	private String getNewLogs(WatchAlertTask watchAlertTask)
	{

		try {
			Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
			System.out.println("getNewLogs sending request to socket.");
			String urlParameters = WatchAlertUtils.replaceKeywords(watchAlertTask.getQuerybody(), watchAlertTask, null);
			System.out.println(urlParameters);
			byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
			Socket socket = new Socket(InetAddress.getByName(watchAlertConfig.getElasticHost()), Integer.parseInt(watchAlertConfig.getElasticPort()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.print("XGET " + WatchAlertUtils.replaceKeywords(watchAlertTask.getIndice(), watchAlertTask, null) + " HTTP/1.1\r\n");
			pw.print("Host: "+ InetAddress.getByName(watchAlertConfig.getElasticHost())+":"+Integer.parseInt(watchAlertConfig.getElasticPort())+"\r\n");
			pw.print("Accept: */*\r\n");
			pw.print("Content-Length: " + Integer.toString(postData.length) +"\r\n");
			pw.print("Content-Type: application/x-www-form-urlencoded\r\n");
			pw.print("\r\n");
			pw.print(urlParameters);
			pw.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			
			arrayList = 0;
			jsonStrated = false;

			System.out.println("getNewLogs starting receiving from socket.");
/*			String t;
 * 			//key = 0;
			//parseValue = false;
 			while((t = br.readLine()) != null)
			{			
				String str = t.trim();
				//System.out.println("Line " + str);
				parseLine(str, receivedNodes);
				
				if(jsonStrated && arrayList <= 0)
				{
					jsonStrated = false;
					System.out.println("Break");
					break;
				}
			}*/
			
			String t, json = new String();
			while((t = br.readLine()) != null)
			{			
				String str = t.trim();
				System.out.println("Line " + str);
				
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

		for(WatchAlertTask  watchAlertTask : watchAlertConfig.getWatchAlertTaskList())
		{
				System.out.println("Task next timestamp: " + watchAlertTask.getNextExecuteTime() + " and now timestamp: " + now);
				if(watchAlertTask.getNextExecuteTime() <= now)
				{
					System.out.println("Executing getNewLogs for TASK: " + watchAlertTask.getTaskNumber());
					String json = getNewLogs(watchAlertTask);
					parseJSON(watchAlertTask, json);
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
