package coco;

import java.util.Map;

public class Table {
	
	private Table parent;
	private Map<String, Variable> variables;
	
	public Table(Table parent, Map<String, Variable> variables) {
		this.parent = parent;
		this.variables = variables;
	}
	
	public String get(Token ident) throws NonexistantVariableException {
		if(variables.containsKey(ident.lexeme())) {
			return variables.get(ident.lexeme()).type;
		} else {
			if(parent != null) {
				return parent.get(ident);
			} else {
				throw new NonexistantVariableException(ident);
			}
		}
	}
	
	public boolean has(Token name) {
		try {
			get(name);
			return true;
		} catch(NonexistantVariableException e) {
			return false;
		}
	}
	
	public void add(Token name, String type) throws RedefinitionException {
		if(variables.containsKey(name.lexeme())) {
			throw new RedefinitionException(name);
		}
		variables.put(name.lexeme(), new Variable(type));
	}
	
	/* public String addAlias(String name) {
		Variable variable = variables.get(name);
		while(true) {
			Token newName = new Token(name + variable.nextIndex++, 0, 0);
			if(!has(newName)) {
				
			}
		}
	} */
	
	protected Table getParent() {
		return parent;
	}

}


class Variable {
	
	public String type;
	public int nextIndex = 0;
	
	public Variable(String type) {
		this.type = type;
	}
	
}