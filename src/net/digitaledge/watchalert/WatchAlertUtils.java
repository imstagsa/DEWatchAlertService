package net.digitaledge.watchalert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WatchAlertUtils {
	
	public static Long getEpochTime()
	{
		return Instant.now().getEpochSecond();
	}
	
	public static String getTimeStamp(Integer seconds, Integer diff)
	{
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat df2 = new SimpleDateFormat("HH:mm:ss.SSS");
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + diff);
		today.setSeconds(today.getSeconds() - seconds);
		return  df1.format(today) + "T"+df2.format(today)+"Z";
	}
	
	private static String getDateTime(String format, Integer diff)
	{
		DateFormat df = new SimpleDateFormat(format);
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + diff);
		today.setSeconds(today.getSeconds() - diff);
		return  df.format(today);
		
		/*DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDateTime now = LocalDateTime.now().minusHours(diff);
		return now.format(formatter);*/
		
	}
	
	private static String getDateTimeMinusPeriod(String format, Integer diff, Integer seconds)
	{
		DateFormat df = new SimpleDateFormat(format);
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + diff);
		today.setSeconds(today.getSeconds() - seconds);
		return  df.format(today);
	}

	public static String replaceKeywords(String str, WatchAlertTask watchAlertTask, List<MapVariableValue> nodes)
	{
		try{
			
			str = str.replaceAll("%YEAR%", getDateTime("yyyy", watchAlertTask.getTimeZoneDiff()));
			str = str.replaceAll("%MONTH%", getDateTime("MM", watchAlertTask.getTimeZoneDiff()));
			str = str.replaceAll("%DAY%", getDateTime("dd", watchAlertTask.getTimeZoneDiff()));
			if(watchAlertTask.getTimeformat().length() > 0)
			{
				str = str.replaceAll("%TIMESTAMP%", getDateTime(watchAlertTask.getTimeformat(), watchAlertTask.getTimeZoneDiff()));
				str = str.replaceAll("%TIMESTAMP-PERIOD%", getDateTimeMinusPeriod(watchAlertTask.getTimeformat(), watchAlertTask.getTimeZoneDiff(), watchAlertTask.getPeriod()));
			}
			else
			{
				str = str.replaceAll("%TIMESTAMP%", getTimeStamp(0, watchAlertTask.getTimeZoneDiff()));
				str = str.replaceAll("%TIMESTAMP-PERIOD%", getTimeStamp(watchAlertTask.getPeriod(), watchAlertTask.getTimeZoneDiff()));
			}
			//System.out.println.info("After watchAlertTask.getTimeformat().length()");
			str = str.replaceAll("%EPOCHTIME%", Long.toString(getEpochTime()));
			str = str.replaceAll("%CURDATE%", getDateTime(watchAlertTask.getTimeformat(), watchAlertTask.getTimeZoneDiff()));	
			str = str.replaceAll("%HOST%", "4443");
			
			if(nodes != null)
			{
				for(int i = 0; i < nodes.size(); i++)
				{
					if(nodes.get(i).getVariable() != null)
					{
						for(MapVariableValue replaceFields: watchAlertTask.getReplaceFields())
						{
							if(replaceFields.getValue().toLowerCase().equals(nodes.get(i).getVariable().toLowerCase()))
							{
								try 
								{
									//System.out.println(nodes.get(i).getValue());
									if(nodes.get(i).getValue().contains("$"))
									{
										//System.out.println("Found wrong value" + nodes.get(i).getValue());
										nodes.get(i).setValue(((String)nodes.get(i).getValue()).replaceAll("\\$", ""));
										//System.out.println("Replased wrong value" + nodes.get(i).getValue());
									}
									str = str.replaceAll("%"+replaceFields.getVariable()+"%", nodes.get(i).getValue());
									System.out.println("Found replacement field:" + nodes.get(i).getValue() + " with pattern: " + replaceFields.getVariable());
								}
								catch(Exception e) {
									System.out.println(e.toString());
								}
							}
						}
					}
				}
			}
			
			return str;
		
    	} catch (Exception e) {
    		System.out.println(e.toString());
    		return null;
		} 		
	}
	
}
