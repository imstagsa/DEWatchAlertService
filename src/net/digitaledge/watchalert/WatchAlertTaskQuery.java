package net.digitaledge.watchalert;

import java.util.ArrayList;
import java.util.List;



public class WatchAlertTaskQuery {
	
	private String querybody = new String();
	private Integer greaterThan = new Integer(0);
	private Integer lessThan = new Integer(0);
	private String campareFlag = new String("NO_COMPARE");
	private String procedure  = new String();
	private List<String> keywords = new ArrayList<String>();
	private List<String> fields = new ArrayList<String>();
	private List<MapVariableValue> replaceFields = new ArrayList<MapVariableValue>();
	
	public WatchAlertTaskQuery(){}
	
	public String getProcedure() {
		return procedure;
	}

	public void setProcedure(String procedure) {
		this.procedure = procedure;
	}
	
	public String getCampareFlag() {
		return campareFlag;
	}

	public void setCampareFlag(String campareFlag) {
		this.campareFlag = campareFlag;
	}

	public Integer getGreaterThan() {
		return greaterThan;
	}

	public void setGreaterThan(Integer greaterThan) {
		this.greaterThan = greaterThan;
	}

	public Integer getLessThan() {
		return lessThan;
	}

	public void setLessThan(Integer lessThan) {
		this.lessThan = lessThan;
	}
	
	public String getQuerybody() {
		return querybody;
	}
	
	public void setQuerybody(String querybody) {
		this.querybody = querybody;
	}
	
	public List<String> getFields() {
		return fields;
	}

	public void setFields(String field) {
		String[] stringArray = field.split(" ");
		for(int i=0; i < stringArray.length; i++)
			this.fields.add(stringArray[i]);
	}
	
	public List<String> getKeywords() {
		return keywords;
	}
	
	public void setKeywords(String keywords) {
		String[] stringArray = keywords.split(" ");
		for(int i=0; i < stringArray.length; i++)
			this.keywords.add(stringArray[i]);
	}
	
	public List<MapVariableValue> getReplaceFields() {
		return replaceFields;
	}

	public void setReplaceFields(String fieldsFor) {
		try{
			String[] stringArray = fieldsFor.split(" ");
			for(int y = 0; y < stringArray.length; y++)
			{
				String[] stringArray2 = stringArray[y].split(":");
				if(stringArray2.length == 2)
				{
					MapVariableValue watchAlertReplaceFields = new MapVariableValue();
					watchAlertReplaceFields.setValue(stringArray2[0]);
					watchAlertReplaceFields.setVariable(stringArray2[1]);
					this.replaceFields.add(watchAlertReplaceFields);
				}
			}
		}
		catch(Exception e){ e.toString();}
	}
}
