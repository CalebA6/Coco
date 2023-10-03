package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import coco.Variables;
import types.BoolType;
import types.ErrorType;
import types.Type;
import types.TypeChecker;

public class While extends CheckableNode {
	
	private Node decision;
	private Statements action;
	private Token start;

	public While(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.WHILE, "WHILE", source);
		start = source.last();
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables).genAST();
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
		ErrorChecker.mustBe(Kind.DO, "DO", source);
		action = new Statements(source, variables);
		ErrorChecker.mustBe(Kind.OD, "OD", source);
	}
	
	public int lineNumber() {
		return start.lineNumber();
	}
	
	public int charPosition() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(decision instanceof CheckableNode) ((CheckableNode) decision).checkFunctionCalls(parent);
		action.checkFunctionCalls(parent);
	}
	
	public Type getType() {
		return action.getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(!BoolType.is(decision.getType())) {
			ErrorType error = new ErrorType();
			error.setError(this, "WhileStat requires bool condition not " + decision.getType() + ".");
			reporter.reportError(error);
		}
		
		action.checkType(reporter, returnType, functionName);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("WhileStatement\n");
		print.append(decision.printPreOrder(level+1));
		print.append(action.printPreOrder(level+1));
		return print.toString();
	}
	
}
