package coco;

import java.util.HashMap;
import java.util.Map;

public class Variables {
	
	private Variables parent;
	private Map<String, Variable> variables;
	
	private Variables(Variables parent, Map<String, Variable> variables) {
		this.parent = parent;
		this.variables = variables;
	}
	
	public Variables() {
		parent = null;
		variables = new HashMap<>();
	}
	
	public Variable get(String name) throws NonexistantVariableException {
		if(variables.containsKey(name)) {
			return variables.get(name);
		} else {
			if(parent != null) {
				return parent.get(name);
			} else {
				throw new NonexistantVariableException();
			}
		}
	}
	
	public void add(String name, Variable variable) {
		variables.put(name, variable);
	}
	
	public void enterLevel() {
		parent = new Variables(parent, variables);
		variables = new HashMap<>();
	}
	
	public void exitLevel() {
		variables = parent.variables;
		parent = parent.parent;
	}

}
