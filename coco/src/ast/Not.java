package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import coco.Variables;
import types.Type;

public class Not extends CheckableNode {
	
	Relation relation;
	Token not;

	public Not(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.NOT, "NOT", source);
		not = source.last();
		relation = new Relation(source, variables);
	}
	
	public int line() {
		return not.lineNumber();
	}
	
	public int charPos() {
		return not.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		relation.checkFunctionCalls(parent);
	}
	
	public Type getType() {
		return Type.BOOL;
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("LogicalNot\n");
		print.append(relation.printPreOrder(level+1));
		return print.toString();
	}
	
}
