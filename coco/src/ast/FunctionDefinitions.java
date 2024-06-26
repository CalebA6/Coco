package ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import ir.Graph;
import coco.Token.Kind;
import types.Type;
import types.TypeChecker;
import types.VoidType;

public class FunctionDefinitions extends CheckableNode {
	
	private List<FunctionDeclaration> functions = new ArrayList<>();
	private Token first;
	
	public FunctionDefinitions(ReversibleScanner source, Variables variables) throws SyntaxException, RedefinitionException, NonexistantVariableException {
		first = source.peek();
		while(source.peek().kind() == Kind.FUNC) {
			functions.add(new FunctionDeclaration(source, variables));
			ErrorChecker.checkForMoreInput(source, "OPEN_BRACE");
		}
	}
	
	public int lineNumber() {
		return first.lineNumber();
	}
	
	public int charPosition() {
		return first.charPosition();
	}
	
	public FunctionDefinitions genAST() {
		if(functions.isEmpty()) return null;
		return this;
	}
	
	public void checkFunctionCalls(AST parent) {
		for(FunctionDeclaration function: functions) {
			function.checkFunctionCalls(parent);
		}
	}
	
	public Type getType() {
		return new VoidType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		for(FunctionDeclaration function: functions) {
			function.checkType(reporter, returnType, functionName);
		}
	}
	
	public List<Graph> genIr(Set<String> globalVariables) {
		List<Graph> graphs = new ArrayList<>();
		for(FunctionDeclaration function: functions) {
			ir.Variables variables = new ir.Variables(globalVariables);
			String[] parameters = function.getParameters().getNames().toArray(new String[0]);
			String type = function.getParameters().getTypes();
			graphs.add(new Graph(function.getName().lexeme(), function.genCode(variables), parameters, type, globalVariables));
		}
		return graphs;
	}
	
	public Collection<String> getNames() {
		List<String> names = new ArrayList<>();
		for(FunctionDeclaration function: functions) {
			names.add(function.getName().lexeme());
		}
		return names;
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("DeclarationList\n");
		for(FunctionDeclaration function: functions) {
			print.append(function.printPreOrder(level+1));
		}
		return print.toString();
	}

}
