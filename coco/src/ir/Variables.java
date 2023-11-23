package ir;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Variables {

	Set<String> globalVariables;
	Set<String> variables = new HashSet<>();
	static int tempIndex = 0;
	
	public Variables(Set<String> globalVariables) {
		this.globalVariables = globalVariables;
	}
	
	public void add(Collection<String> variables) {
		this.variables.addAll(variables);
	}
	
	public boolean has(String variable) {
		return globalVariables.contains(variable) || variables.contains(variable);
	}
	
	public String getTemp() {
		while(has("t" + tempIndex)) {
			++tempIndex;
		}
		String temp = "t" + tempIndex;
		variables.add(temp);
		return temp;
	}

}
