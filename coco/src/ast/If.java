package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import coco.Variables;
import types.Type;
import types.TypeChecker;

public class If extends CheckableNode {
	
	private Token statement;
	private Relation decision;
	private Statements action;
	private Statements inaction = null;

	public If(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.IF, "IF", source);
		statement = source.last();
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables);
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
		decision.checkFunctionCalls(parent);
		action.checkFunctionCalls(parent);
		if(inaction != null) {
			inaction.checkFunctionCalls(parent);
		}
	}
	
	public Type getType() {
		if(action.getType() != Type.VOID) return action.getType();
		if(inaction.getType() != Type.VOID) return inaction.getType();
		return Type.VOID;
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		if(decision.getType() != Type.BOOL) {
			Type error = Type.ERROR;
			error.setError(decision, "If decision block must have BOOL type.");
			reporter.reportError(error);
		}
		
		decision.checkType(reporter, returnType);
		action.checkType(reporter, returnType);
		if(inaction != null) inaction.checkType(reporter, returnType);
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
