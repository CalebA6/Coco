package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class FunctionDeclaration extends CheckableNode {
	
	private Token name;
	private Parameters parameters;
	private Token type;
	private FunctionBody action;
	
	public FunctionDeclaration(ReversibleScanner source, Variables variables) throws SyntaxException, RedefinitionException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.FUNC, "FUNC", source);
		name = ErrorChecker.mustBe(Kind.IDENT, "IDENT", source);
		variables.enterLevel();
		parameters = new Parameters(source, variables);
		ErrorChecker.mustBe(Kind.COLON, "COLON", source);
		ErrorChecker.checkForMoreInput(source, "void or type");
		type = source.next();
		if((type.kind() != Kind.VOID) && (type.kind() != Kind.BOOL) && (type.kind() != Kind.INT) && (type.kind() != Kind.FLOAT)) {
			throw new SyntaxException("Expected void or type but got " + type.kind() + ".", type);
		}
		variables.add(name, parameters + "->" + type.lexeme());
		action = new FunctionBody(source, variables);
		variables.exitLevel();
	}
	
	public Token getName() {
		return name;
	}
	
	public Parameters getParameters() {
		return parameters;
	}
	
	public void checkFunctionCalls(AST parent) {
		action.checkFunctionCalls(parent);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("FunctionDeclaration[" + name.lexeme() + ":");
		print.append(parameters);
		print.append("->");
		print.append(type.lexeme());
		print.append("]\n");
		print.append(action.printPreOrder(level+1));
		return print.toString();
	}

}
