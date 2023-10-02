package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token.Kind;
import coco.Variables;

public class While extends CheckableNode {
	
	private Relation decision;
	private Statements action;

	public While(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.WHILE, "WHILE", source);
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables);
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
		ErrorChecker.mustBe(Kind.DO, "DO", source);
		action = new Statements(source, variables);
		ErrorChecker.mustBe(Kind.OD, "OD", source);
	}
	
	public void checkFunctionCalls(AST parent) {
		decision.checkFunctionCalls(parent);
		action.checkFunctionCalls(parent);
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
