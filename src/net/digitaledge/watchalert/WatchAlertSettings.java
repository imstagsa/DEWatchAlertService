package net.digitaledge.watchalert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class WatchAlertSettings {

	private Map<String, String> settings = new HashMap<String, String>();
	final static Logger logger = Logger.getLogger("WatchalertService");
	
	public WatchAlertSettings()
	{
		if(readFile("watchalert.yml") == null)
			System.exit(0);
	}
	
	public String get(String key)
	{
		return key == null ? null : (String)settings.get(key);
	}
	
	private List<String> readFile(String filename)
	{
		  BufferedReader reader;
		  try {
			  reader = new BufferedReader(new FileReader(filename));
			  return readParse(reader);
				  
		  } catch (FileNotFoundException e1) {
			logger.error("Cannot read config file " + filename + ". Exiting...");
			return null;
		  }
	}
	
	private List<String> readParse(BufferedReader reader)
	{
		List<String> records = new ArrayList<String>();
  		String line = new String();
  		String record = new String();
  		Boolean  isSingleQuote = false;
  		String key = new String();	  
  		try
  		{
	    	while ((line = reader.readLine()) != null)
	    	{
	    		line = line.trim();
	    		if(!line.startsWith("#"))
	    		{
	    			if(isSingleQuote)
	    			{
	    				if(line.endsWith("'"))
	    				{
	    					isSingleQuote = false;
	    					record = record.replaceAll("'", "");
	    					line = line.replaceAll("'", "");
	    					settings.put(key.trim(), record + line);
	    					record = "";
	    				}
					 else record = record + line;
				 }
	    		
	    		else if(line.startsWith("watchalert"))
				{
					 key = line.split(":")[0];
					 String value = new String();
					 if(line.split(":").length > 2)
						 value = line.replaceFirst(key+":", "").trim();
					 else value = line.split(":")[1].trim();
					 
					 if(value.startsWith("'"))
					 {
						 if(value.endsWith("'"))
						 {
							 value = value.replaceAll("'", "");
							 settings.put(key.trim(), value);
							 isSingleQuote = false;
						 }
						 else
						 {
							 isSingleQuote = true;
							 record = record + value;
						 }
					 }
					 else
					 {
						 settings.put(key.trim(), value);
					 }
					 
				}   			
	    		records.add(line);
	    	}
	    }
	    reader.close();
	    return records;

	  }
	  catch (Exception e)
	  {
	    logger.error("Exception occurred trying to read config file in line: " + line);
	    logger.error(e.toString());
	    return null;
	  }
	}

}
