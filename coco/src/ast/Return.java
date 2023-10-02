package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import coco.Variables;

public class Return extends CheckableNode {
	
	private Relation value;
	private Token start;

	public Return(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.RETURN, "RETURN", source);
		start = source.last();
		Token next = source.peek();
		if(next.kind() == Kind.SEMICOLON) {
			value = null;
		} else {
			value = new Relation(source, variables);
		}
	}
	
	public int line() {
		return start.lineNumber();
	}
	
	public int charPos() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(value != null) value.checkFunctionCalls(parent);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ReturnStatement\n");
		if(value != null) {
			print.append(value.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
