package net.digitaledge.watchalert;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class WatchAlertInformer {

	
	public void notify(List<MapAlertStrings> taskNodes, WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery)
	{
		for(MapAlertStrings alertBody : taskNodes)
		{
			System.out.println("ALERT: " + alertBody.getAlertString());
			//for(MapVariableValue mapVariableValue : alertBody.getAlertMapStrings())
				//System.out.println("mapVariableValue: " + mapVariableValue.getVariable() + ": " + mapVariableValue.getValue());
			
			System.out.println("watchAlertTask.getEmailFlag(): " + watchAlertTask.getEmailFlag());
			if(watchAlertTask.getEmailFlag().equals("YES"))
				if(watchAlertTask.getSmtpPassword().length()>0) sendEmailWithAuth(watchAlertTask, alertBody.getAlertString(), watchAlertTaskQuery, alertBody.getAlertMapStrings());
				else sendEmailWithoutAuth(watchAlertTask, alertBody.getAlertString(), watchAlertTaskQuery, alertBody.getAlertMapStrings());
			if(watchAlertTask.getHttpLink().length() > 1)
				sendAlertViaHTTP(watchAlertTask, alertBody.getAlertString(), watchAlertTaskQuery, alertBody.getAlertMapStrings());
		}
	}
	
	/**
	 * Sending emails with authorization.
	 * @param watchAlertTask
	 */
	public void sendEmailWithoutAuth(WatchAlertTask watchAlertTask, String alertBody, WatchAlertTaskQuery watchAlertTaskQuery, List<MapVariableValue> nodes)
	{
		try{
			System.out.println("BEFORE SENDING EMAIL");
			String[] stringArray = watchAlertTask.getSmtpServer().split(":");
			String smtpServer = stringArray[0];
			String smtpPort = new String("25");
			String alertMessage = new String();
			
			if(stringArray.length == 2)  smtpPort = stringArray[1];			 	 
			
			alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getSmtpBody(), watchAlertTask, watchAlertTaskQuery, nodes);
			//System.out.println("alertMessage " + alertMessage);
			alertMessage = alertMessage.replaceAll("%MESSAGE%", alertBody);
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpServer);
			//if (enableDebug.equals("true")) props.put("mail.debug", "true");
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.socketFactory.fallback", "true");
			props.put("mail.smtp.port", smtpPort);
			props.put("java.net.preferIPv4Stack", "true");
			Session session = Session.getInstance(props);
			MimeMessage msg = new MimeMessage(session);
			
			for(String addressstr : watchAlertTask.getRecipients())
			{
				addressstr = WatchAlertUtils.replaceKeywords(addressstr, watchAlertTask, watchAlertTaskQuery, nodes);
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
	public void sendEmailWithAuth(final WatchAlertTask watchAlertTask, String alertBody, WatchAlertTaskQuery watchAlertTaskQuery, List<MapVariableValue> nodes)
	{
		 
		Transport trans=null;
		String[] stringArray = watchAlertTask.getSmtpServer().split(":");
		String smtpServer = stringArray[0];
		String smtpPort = new String("25");
		if(stringArray.length == 2)
			smtpPort = stringArray[1];
		
		String alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getSmtpBody(), watchAlertTask, watchAlertTaskQuery, nodes);
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
				addressstr = WatchAlertUtils.replaceKeywords(addressstr, watchAlertTask, watchAlertTaskQuery, nodes);
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
	
	/**
	 * Sends alert to EMS.
	 */
	private void sendAlertViaHTTP(WatchAlertTask watchAlertTask, String alertString, WatchAlertTaskQuery watchAlertTaskQuery, List<MapVariableValue> nodes)
	{
		URL url;	
    	try {
    		
    		String alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getHttpBody(), watchAlertTask, watchAlertTaskQuery, nodes);
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
	

}
