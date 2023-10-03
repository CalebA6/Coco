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
import types.VoidType;

public class If extends CheckableNode {
	
	private Token statement;
	private Node decision;
	private Statements action;
	private Statements inaction = null;

	public If(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.IF, "IF", source);
		statement = source.last();
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables).genAST();
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
		ErrorChecker.mustBe(Kind.THEN, "THEN", source);
		variables.enterLevel();
		action = new Statements(source, variables);
		variables.exitLevel();
		
		ErrorChecker.checkForMoreInput(source, "FI");
		Token next = source.next();
		if(next.kind() == Kind.ELSE) {
			variables.enterLevel();
			inaction = new Statements(source, variables);
			variables.exitLevel();
		} else if(next.kind() == Kind.FI) {
			return;
		} else {
			throw new SyntaxException("Expected FI but got " + next.kind() + ".", next);
		}
		
		ErrorChecker.mustBe(Kind.FI, "FI", source);
	}
	
	public int lineNumber() {
		return statement.lineNumber();
	}
	
	public int charPosition() {
		return statement.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(decision instanceof CheckableNode) ((CheckableNode) decision).checkFunctionCalls(parent);
		action.checkFunctionCalls(parent);
		if(inaction != null) {
			inaction.checkFunctionCalls(parent);
		}
	}
	
	public Type getType() {
		if(!VoidType.is(action.getType())) return action.getType();
		if(!VoidType.is(inaction.getType())) return inaction.getType();
		return new VoidType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(!BoolType.is(decision.getType())) {
			ErrorType error = new ErrorType();
			error.setError(decision, "IfStat requires bool condition not " + decision.getType() + ".");
			reporter.reportError(error);
		}
		
		decision.checkType(reporter, returnType, functionName);
		action.checkType(reporter, returnType, functionName);
		if(inaction != null) inaction.checkType(reporter, returnType, functionName);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("IfStatement\n");
		print.append(decision.printPreOrder(level+1));
		print.append(action.printPreOrder(level+1));
		if(inaction != null) {
			print.append(inaction.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
