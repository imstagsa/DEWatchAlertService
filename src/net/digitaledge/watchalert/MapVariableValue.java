package net.digitaledge.watchalert;

public class MapVariableValue {
	
	private String value;
	private String variable;
	
	public MapVariableValue(String variable)
	{
		this.variable = variable;
		this.value = new String();
	}

	public MapVariableValue(String variable, String value)
	{
		this.variable = variable;
		this.value = value;
	}
	
	public MapVariableValue()
	{
		this.variable = new String();
		this.value = new String();
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}
	
}