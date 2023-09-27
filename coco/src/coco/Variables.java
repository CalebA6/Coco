package coco;

import java.util.HashMap;
import java.util.Map;

public class Variables {
	
	private Variables parent;
	private Map<String, String> variables;
	
	private Variables(Variables parent, Map<String, String> variables) {
		this.parent = parent;
		this.variables = variables;
	}
	
	public Variables() {
		parent = null;
		variables = new HashMap<>();
		
		variables.put("readInt", "()->int");
		variables.put("readFloat", "()->float");
		variables.put("readBool", "()->bool");
		variables.put("printInt", "(int)->void");
		variables.put("printFloat", "(float)->void");
		variables.put("printBool", "(bool)->void");
		variables.put("println", "()->void");
		variables.put("arrcpy", "(T[],T[],int)->void");
	}
	
	public boolean has(Token ident) {
		try {
			get(ident);
			return true;
		} catch(NonexistantVariableException e) {
			return false;
		}
	}
	
	public String get(Token ident) throws NonexistantVariableException {
		if(variables.containsKey(ident.lexeme())) {
			return variables.get(ident.lexeme());
		} else {
			if(parent != null) {
				return parent.get(ident);
			} else {
				throw new NonexistantVariableException(ident);
			}
		}
	}
	
	public void add(Token name, String type) throws RedefinitionException {
		if(variables.containsKey(name.lexeme())) {
			throw new RedefinitionException(name);
		}
		variables.put(name.lexeme(), type);
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
