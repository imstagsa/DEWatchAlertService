package net.digitaledge.watchalert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

public class WatchAlertSettings {

	private Map settings = new HashMap<String, String>();
	public WatchAlertSettings()
	{
		if(readFile("watchalert.yml") == null)
			System.exit(0);
		 
		 //List<String> mappedList  = records.stream().map(emp -> emp.split(":")[0]).collect(toList());	 
		 //for(String map: mappedList)
		 //	 System.out.println(map);
/*		 for(String record : records)
		 {
			 if(record.startsWith("watchalert"))
			 {
				 System.out.println(record);
				 String key = record.split(":")[0];
				 String value = new String();
				 if(record.split(":")[0].length() > 2)
					 value = record.replaceFirst(key+":", "").trim();
				 else value = record.split(":")[1].trim();
				 //System.out.println("KEY: " + key);
				 //System.out.println("VALUE: " + value);
				 settings.put(key.trim(), value);
			 }
		 }*/
		 //for(int i =0; i < settings.size(); i++)
			// System.out.println(settings.);
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
			  if(reader!=null)
				  return readParse(reader);
			  else return null;
				  
		  } catch (FileNotFoundException e1) {
			System.out.println("Cannot read config file " + filename + ". Exiting...");
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
	    		//System.out.println(line);
	    		if(isSingleQuote)
				 {
					 if(line.endsWith("'"))
					 {
						 isSingleQuote = false;
						 //System.out.println("Starting with: " + record);
						 record = record.replaceAll("'", "");
						 line = line.replaceAll("'", "");
					 	 settings.put(key.trim(), record + line);
					 	 record = "";
					 	 //System.out.println("KEY: " + key.trim());
						 //System.out.println("VALUE: " + record + line);
					 }
					 else
					 {
						 //System.out.println("Starting with: " + record);
						 record = record + line;
					 }
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
						 //System.out.println("Start with '  " + value );
						 if(value.endsWith("'"))
						 {
							 //System.out.println("false '  " + value );
							 value = value.replaceAll("'", "");
							 settings.put(key.trim(), value);
							 //System.out.println("KEY: " + key.trim());
							 //System.out.println("VALUE: " + value);
							 isSingleQuote = false;
						 }
						 else
						 {
							 //System.out.println("true '  " + value );
							 isSingleQuote = true;
							 record = record + value;
						 }
						 
					 }
					 else
					 {
						 settings.put(key.trim(), value);
						 //System.out.println("KEY: " + key);
						 //System.out.println("VALUE: " + value);
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
	    System.err.format("Exception occurred trying to read config file in line: " + line);
	    System.out.format(e.toString());
	    return null;
	  }
	}

}
