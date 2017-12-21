package net.digitaledge.watchalert;

import java.io.BufferedReader;
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
		 List<String> records = readConfigFile("elasticsearch.yml");
		 
		 //List<String> mappedList  = records.stream().map(emp -> emp.split(":")[0]).collect(toList());	 
		 //for(String map: mappedList)
		 //	 System.out.println(map);
		 for(String record : records)
		 {
			 if(record.startsWith("watchalert"))
			 {
				 //System.out.println(record);
				 String key = record.split(":")[0];
				 String value = new String();
				 if(record.split(":")[0].length() > 2)
					 value = record.replaceFirst(key+":", "").trim();
				 else value = record.split(":")[1].trim();
				 //System.out.println("KEY: " + key);
				 //System.out.println("VALUE: " + value);
				 settings.put(key.trim(), value);
			 }
		 }
		 //for(int i =0; i < settings.size(); i++)
			// System.out.println(settings.);
	}
	
	public String get(String key)
	{
		return key == null ? null : (String)settings.get(key);
	}
	
	private List<String> readConfigFile(String filename)
	{
	  List<String> records = new ArrayList<String>();
	  try
	  {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    while ((line = reader.readLine()) != null)
	    {
	    	line = line.trim();
	    	if(!line.startsWith("#"))
	    		records.add(line);
	    }
	    reader.close();
	    return records;
	  }
	  catch (Exception e)
	  {
	    System.err.format("Exception occurred trying to read '%s'.", filename);
	    e.printStackTrace();
	    return null;
	  }
	}

}
