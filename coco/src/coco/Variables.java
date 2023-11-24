package coco;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ast.AST;

public class Variables {
	
	private Table table;
	private Map<String, Set<Function>> functions = new HashMap<>();
	private AST parent;
	
	public Variables(AST parent) {
		table = new Table(null, new HashMap<>());
		this.parent = parent;
		
		Set<Function> defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "int"));
		functions.put("readInt", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "float"));
		functions.put("readFloat", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "bool"));
		functions.put("readBool", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(int)", "void"));
		functions.put("printInt", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(float)", "void"));
		functions.put("printFloat", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(bool)", "void"));
		functions.put("printBool", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "void"));
		functions.put("println", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(T[],T[],int)", "void"));
		functions.put("arrcpy", defaultType);
	}
	
	public Variables() {
		table = new Table(null, new HashMap<>());
		
		Set<Function> defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "int"));
		functions.put("readInt", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "float"));
		functions.put("readFloat", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "bool"));
		functions.put("readBool", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(int)", "void"));
		functions.put("printInt", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(float)", "void"));
		functions.put("printFloat", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(bool)", "void"));
		functions.put("printBool", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("()", "void"));
		functions.put("println", defaultType);
		defaultType = new LinkedHashSet<>();
		defaultType.add(new Function("(T[],T[],int)", "void"));
		functions.put("arrcpy", defaultType);
	}
	
	public boolean has(Token ident) {
		try {
			get(ident, true);
			return true;
		} catch(NonexistantVariableException e) {
			return false;
		}
	}
	
	public Set<String> get(Token ident) throws NonexistantVariableException {
		return get(ident, false);
	}
	
	public Set<String> get(Token ident, boolean pseudo) throws NonexistantVariableException {
		Set<String> type = new LinkedHashSet<>();
		boolean gotSomething = false;
		Exception error = null;
		try {
			type.add(table.get(ident));
			gotSomething = true;
		} catch(NonexistantVariableException e) {
			error = e;
		}
		if(functions.containsKey(ident.lexeme())) {
			for(Function f: functions.get(ident.lexeme())) {
				type.add(f.toString());
			}
			gotSomething = true;
		}
		if(!gotSomething) {
			if(!pseudo) parent.reportError(error);
			type.add("error");
			return type;
		}
		return type;
	}
	
	public void add(Token name, String type) throws RedefinitionException {
		try {
			if(type.contains("->")) {
				String[] typeParts = type.split("->");
				String parameters = typeParts[0];
				String returnType = typeParts[1];
				Function newFunc = new Function(parameters, returnType);
				if(has(name)) {
					if(functions.containsKey(name.lexeme())) {
						for(Function currType: functions.get(name.lexeme())) {
							if(currType.equals(newFunc)) {
								throw new RedefinitionException(name);
							}
						}
					}
				}
				if(functions.containsKey(name.lexeme())) {
					Set<Function> funcDefinitions = functions.get(name.lexeme());
					if(funcDefinitions.contains(newFunc)) {
						throw new RedefinitionException(name);
					}
					funcDefinitions.add(newFunc);
				} else {
					Set<Function> funcDefinitions = new LinkedHashSet<>();
					funcDefinitions.add(newFunc);
					functions.put(name.lexeme(), funcDefinitions);
				}
			} else {
				table.add(name, type);
			}
		} catch(RedefinitionException e) {
			parent.reportError(e);
		}
	}
	
	public void enterLevel() {
		Table newTable = new Table(table, new HashMap<>());
		table = newTable;
	}
	
	public void exitLevel() {
		table = table.getParent();
	}
	
	public Table getTable() {
		return table;
	}

}

class Function {
	String parameters;
	String type;
	
	Function(String parameters, String type) {
		this.parameters = parameters;
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return parameters.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other.getClass() == Function.class) {
			Function otherFunc = (Function)other;
			return parameters.equals(otherFunc.parameters);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return parameters + "->" + type;
	}
}