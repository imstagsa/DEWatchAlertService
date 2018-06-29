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

import org.apache.log4j.Logger;

public class WatchAlertInformer {

	final static Logger logger = Logger.getLogger("WatchalertService");
	
	public void notify(List<MapAlertStrings> taskNodes, WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery)
	{
		
		logger.debug("watchAlertTask.getStoreActiveState() = " + watchAlertTask.getStoreActiveState());	
		//if alert was cleared
		if(taskNodes.size() == 0 && watchAlertTask.getStoreActiveState() == 2)
		{
			
			logger.debug("CLEAR: " + watchAlertTask.getAlertBody().getAlertString());		
			watchAlertTask.setStoreActiveState(1);
			
			if(watchAlertTask.getEmailFlag().equals("YES"))
				if(watchAlertTask.getSmtpPassword().length() > 0) 
					sendEmailWithAuth(watchAlertTask, watchAlertTaskQuery);
				else 
					sendEmailWithoutAuth(watchAlertTask, watchAlertTaskQuery);
			
			if(watchAlertTask.getSendAlertHttpLink().length() > 1)
				sendAlertViaHttp(watchAlertTask, watchAlertTaskQuery);
			
			watchAlertTask.setAlertBody(new MapAlertStrings());
			
			
		}
		else
		{
			//if(watchAlertTask.getStoreActiveState() == 1 || watchAlertTask.getStoreActiveState() == 0)
			//{
				for(MapAlertStrings alertBody : taskNodes)
				{
					logger.debug("ALERT: " + alertBody.getAlertString());		
					logger.info("watchAlertTask.getEmailFlag(): " + watchAlertTask.getEmailFlag());
					
					watchAlertTask.setStoreActiveState(2);
					watchAlertTask.setAlertBody(alertBody);
					
					if(watchAlertTask.getEmailFlag().equals("YES"))
						if(watchAlertTask.getSmtpPassword().length() > 0) 
							sendEmailWithAuth(watchAlertTask, watchAlertTaskQuery);
						else 
							sendEmailWithoutAuth(watchAlertTask, watchAlertTaskQuery);
					
					if(watchAlertTask.getSendAlertHttpLink().length() > 1)
						sendAlertViaHttp(watchAlertTask, watchAlertTaskQuery);
				}
			//}
		}
	}
	
	/**
	 * Sending emails without authentication.
	 * @param watchAlertTask
	 */
	public void sendEmailWithoutAuth(WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery)
	{
		
		String alertEmailBody = new String();
		String alertString = watchAlertTask.getAlertBody().getAlertString();
		List<MapVariableValue> nodes = watchAlertTask.getAlertBody().getAlertMapStrings();
		
		try{
			logger.info("BEFORE SENDING EMAIL");
			String[] stringArray = watchAlertTask.getSmtpServer().split(":");
			String smtpServer = stringArray[0];
			String smtpPort = new String("25");
				
			if(stringArray.length == 2)  smtpPort = stringArray[1];			 	 
			
			if(watchAlertTask.getStoreActiveState() == 2)
				alertEmailBody = WatchAlertUtils.replaceKeywords(watchAlertTask.getSendAlertEmailBody(), watchAlertTask, watchAlertTaskQuery, nodes);
			else
				alertEmailBody = WatchAlertUtils.replaceKeywords(watchAlertTask.getClearAlertEmailBody(), watchAlertTask, watchAlertTaskQuery, nodes);
			
			alertEmailBody = alertEmailBody.replaceAll("%MESSAGE%", alertString);
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpServer);
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
					else logger.info("Cannot find prefix in \'" +addressstr+"\' . Should be one of the to, cc or bcc.");	        			
				}
				else
					logger.info("Cannot parse address \'" +addressstr+"\' . Should be in format: pefix:email@domain");
			}  
		
        	//for(int i = 0; i< msg.getAllRecipients().length; i++)
        	//	logger.info("Sending email to " + msg.getAllRecipients()[i].toString());
        	
			msg.setFrom(new InternetAddress(watchAlertTask.getSmtpFrom()));
			msg.setSubject(watchAlertTask.getSmtpSubject());
			msg.setText(alertEmailBody, "utf-8", "html");
			Transport transport = session.getTransport();
			transport.connect();
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
			logger.info("AFTER SENDING EMAIL");
		}catch (MessagingException mex) {
			logger.error(mex.toString());
		}
	}
	
	/**
	 * Sending emails with authentication.
	 * @param watchAlertTask
	 */
	public void sendEmailWithAuth(final WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery)
	{
	
		String alertEmailBody = new String();
		String alertString = watchAlertTask.getAlertBody().getAlertString();
		List<MapVariableValue> nodes = watchAlertTask.getAlertBody().getAlertMapStrings();
		Transport trans=null;
		String[] stringArray = watchAlertTask.getSmtpServer().split(":");
		String smtpServer = stringArray[0];
		String smtpPort = new String("25");
		if(stringArray.length == 2)
			smtpPort = stringArray[1];
		
		if(watchAlertTask.getStoreActiveState() == 2)
			alertEmailBody = WatchAlertUtils.replaceKeywords(watchAlertTask.getSendAlertEmailBody(), watchAlertTask, watchAlertTaskQuery, nodes);
		else
			alertEmailBody = WatchAlertUtils.replaceKeywords(watchAlertTask.getClearAlertEmailBody(), watchAlertTask, watchAlertTaskQuery, nodes);
		
		alertEmailBody = alertEmailBody.replaceAll("%MESSAGE%", alertString);
		
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
        			else logger.info("Cannot find prefix in \'" +addressstr+"\' . Should be one of the to, cc or bcc.");	        			
        		}
        		else
        			logger.info("Cannot parse address \'" +addressstr+"\' . Should be in format: pefix:email@domain");
        	}     
        	       	
        	//for(int i = 0; i< msg.getAllRecipients().length; i++)
        	//	logger.info("Sending email to " + msg.getAllRecipients()[i].toString());
        	
        	msg.setSubject(watchAlertTask.getSmtpSubject());  
        	msg.setText(alertEmailBody, "utf-8", "html");
        	trans = session.getTransport("smtp");
        	trans.connect(smtpServer, watchAlertTask.getSmtpFrom(), watchAlertTask.getSmtpPassword());
        	msg.saveChanges();
        	trans.sendMessage(msg, msg.getAllRecipients());
        	trans.close();
        	logger.info("Email successfully sent.");
        }
        catch (MessagingException mex) {
        	logger.error(mex.toString());
        }
	}
	
	/**
	 * Sending alert via HTTP.
	 */
	private void sendAlertViaHttp(WatchAlertTask watchAlertTask, WatchAlertTaskQuery watchAlertTaskQuery)
	{
		URL url;
		String alertHttpLink;
		String alertString = watchAlertTask.getAlertBody().getAlertString();
		List<MapVariableValue> nodes = watchAlertTask.getAlertBody().getAlertMapStrings();
		
    	try {
    		
    		String alertMessage = WatchAlertUtils.replaceKeywords(watchAlertTask.getHttpBody(), watchAlertTask, watchAlertTaskQuery, nodes);
    		if(watchAlertTask.getStoreActiveState() == 2)
    			alertHttpLink = watchAlertTask.getSendAlertHttpLink();
    		else
    			alertHttpLink = watchAlertTask.getClearAlertHttpLink();
    		
    		if(watchAlertTask.getHttpBody().length() > 0)  			
    			alertMessage = alertMessage.replaceAll("%MESSAGE%", alertString);    			
    		else alertMessage = alertString;
    		
    		alertHttpLink = alertHttpLink.replaceAll("%MESSAGE%", alertMessage);
    		alertHttpLink = alertHttpLink.replaceAll(" ", "%20");
    		
    		logger.debug("HTTP LINK:" + alertHttpLink);
    		
     		url = new URL(alertHttpLink);
     		logger.info("Alert string: " + alertHttpLink);
    		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
    		connection.setRequestMethod("GET");
    		connection.connect();
    		logger.info("Sent Alert: " + connection.getResponseCode());
    		connection.disconnect();
    	} catch (Exception e) {
    		logger.error(e.toString());
		} 	
	}
}
