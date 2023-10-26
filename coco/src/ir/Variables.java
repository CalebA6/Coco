package ir;

import java.util.HashSet;
import java.util.Set;

public class Variables {

	Set<String> globalVariables;
	Set<String> variables = new HashSet<>();
	
	public Variables(Set<String> globalVariables) {
		this.globalVariables = globalVariables;
	}

}
