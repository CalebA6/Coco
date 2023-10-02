package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Repeat extends CheckableNode {

	private Relation decision;
	private Statements action;
	private Token start;

	public Repeat(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.REPEAT, "REPEAT", source);
		start = source.last();
		action = new Statements(source, variables);
		ErrorChecker.mustBe(Kind.UNTIL, "UNTIL", source);
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables);
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
	}
	
	public int line() {
		return start.lineNumber();
	}
	
	public int charPos() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		action.checkFunctionCalls(parent);
		decision.checkFunctionCalls(parent);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("RepeatStatement\n");
		print.append(action.printPreOrder(level+1));
		print.append(decision.printPreOrder(level+1));
		return print.toString();
	}
	
}
