package net.digitaledge.watchalert;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This class is parsing JSON string and providing basic functianality such as find an JSON Object or convert JSON to List<MapVariableValue>
 * @author esimacenco
 *
 */

public class JSONUtils {
	
	
	private JSONParser parser = new JSONParser();
	
	public JSONUtils() {}
	
	public Object parse(String json)
	{
        Object obj;
		try {
			obj = parser.parse(json);
			return obj;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
    public static Object findObject(JSONObject jsonObject, String objectName)
    {
    	try {
    	 for (Object key : jsonObject.keySet()) {

             String keyStr = (String)key;
             Object keyvalue = jsonObject.get(keyStr);
             if(keyStr.equals(objectName))
            	 return keyvalue;
             
             if(keyvalue instanceof JSONArray)
              	parseJsonArray((JSONArray)keyvalue);
             else if ( keyvalue instanceof JSONObject)
             	parseJson((JSONObject)keyvalue);
             
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
    	return null;
    }
	
    public static List<MapVariableValue> convertToMapVariableValue(JSONObject jsonObject)
    {
    	
    	List<MapVariableValue> listOfObjects = new ArrayList<MapVariableValue>();
    	for (Object key : ((JSONObject)jsonObject).keySet()) 
    		convertJSONObject(jsonObject, listOfObjects);
    	return listOfObjects;
    }
    
    private static void convertJSONObject(JSONObject jsonObject, List<MapVariableValue> listOfObjects)
    {
    	try {
    	 for (Object key : jsonObject.keySet()) {

             String keyStr = (String)key;
             Object keyvalue = jsonObject.get(keyStr);
             if(keyvalue instanceof JSONArray)
             {
            	 listOfObjects.add(new MapVariableValue(keyStr, ""));
            	 convertJsonArray((JSONArray)keyvalue, listOfObjects);
             }
             else if ( keyvalue instanceof JSONObject)
             {
            	 listOfObjects.add(new MapVariableValue(keyStr, ""));
            	 convertJSONObject((JSONObject)keyvalue, listOfObjects);
             }
             else
            	 if (keyvalue instanceof Long)
            		 listOfObjects.add(new MapVariableValue(keyStr, keyvalue.toString()));
            	 else if (keyvalue instanceof String)
            		 listOfObjects.add(new MapVariableValue(keyStr, (String) keyvalue));
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
    	
    } 
    
    private static void convertJsonArray(JSONArray  jsonArray, List<MapVariableValue> listOfObjects)
    {
    	try {
    		
    		int questionSize =  jsonArray.size();
            for(int i=0; i< questionSize; i++){
            	
            	Object keyvalue = jsonArray.get(i);
                if(keyvalue instanceof JSONArray)
                	convertJsonArray((JSONArray)keyvalue, listOfObjects);
                else if ( keyvalue instanceof JSONObject)
                	convertJSONObject((JSONObject)keyvalue, listOfObjects);
            }
     } catch (Exception e) {
         e.printStackTrace();
     }
    }
    
    public static List<JSONObject> findArrayObject(JSONObject jsonObject, String objectName)
    {
    	List<JSONObject> listObjects = new ArrayList<JSONObject>();
    	
    	try {
    	 for (Object key : jsonObject.keySet()) {

             String keyStr = (String)key;
             Object keyvalue = jsonObject.get(keyStr);
             
             if(keyStr.equals(objectName))
                 if(keyvalue instanceof JSONArray)
                	 for(int i=0; i< ((JSONArray)keyvalue).size(); i++)
                	 {
                	       Object keyvalue2 = ((JSONArray)keyvalue).get(i);
                	       if(keyvalue2 instanceof JSONObject)
                	    	   listObjects.add((JSONObject)keyvalue2);
                	 }
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
    	return listObjects;
    }
    
    private static void parseJson(JSONObject jsonObject)
    {
    	try {
    	 for (Object key : jsonObject.keySet()) {

             String keyStr = (String)key;
             Object keyvalue = jsonObject.get(keyStr);
             if(keyvalue instanceof JSONArray)
             {
              	parseJsonArray((JSONArray)keyvalue);
             }
             else if ( keyvalue instanceof JSONObject)
             {
             	parseJson((JSONObject)keyvalue);
             }
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
    	
    } 

    private static void parseJsonArray(JSONArray  jsonArray)
    {
    	try {
    		
    		int questionSize =  jsonArray.size();
            for(int i=0; i< questionSize; i++){
            	
            	Object keyvalue = jsonArray.get(i);
                if(keyvalue instanceof JSONArray)
                 	parseJsonArray((JSONArray)keyvalue);
                else if ( keyvalue instanceof JSONObject)
                	parseJson((JSONObject)keyvalue);
            }
     } catch (Exception e) {
         e.printStackTrace();
     }
    }
    
    
    
/*    public static void printJson(JSONObject jsonObject)
    {
    	
    	try {
    	 for (Object key : jsonObject.keySet()) {

             String keyStr = (String)key;
             Object keyvalue = jsonObject.get(keyStr);
             System.out.println("key: "+ keyStr);
             if(keyvalue instanceof JSONArray)
             {
              	System.out.println("value is JSONObject");
              	printJsonArray((JSONArray)keyvalue);
             }
             else if ( keyvalue instanceof JSONObject)
             {
             	System.out.println("value is JSONObject");
             	printJson((JSONObject)keyvalue);
             }
             else
             	System.out.println("value: " + keyvalue);            
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
    	
    } 

    public static void printJsonArray(JSONArray  jsonArray)
    {
    	try {
    		
    		int questionSize =  jsonArray.size();
            for(int i=0; i< questionSize; i++){
            	
            	Object keyvalue = jsonArray.get(i);
            	System.out.println(keyvalue.toString());
                if(keyvalue instanceof JSONArray)
                {
                 	System.out.println("value is JSONObject");
                 	printJsonArray((JSONArray)keyvalue);
                }
                else if ( keyvalue instanceof JSONObject)
                {
                	System.out.println("value is JSONObject");
                	printJson((JSONObject)keyvalue);
                }
                else
                	System.out.println("value: " + keyvalue);
            }
     } catch (Exception e) {
         e.printStackTrace();
     }
    }*/
}
