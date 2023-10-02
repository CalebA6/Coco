package coco;

import java.util.Map;

public class Table {
	
	private Table parent;
	private Map<String, String> variables;
	
	public Table(Table parent, Map<String, String> variables) {
		this.parent = parent;
		this.variables = variables;
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
	
	protected Table getParent() {
		return parent;
	}

}
