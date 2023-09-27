package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token.Kind;
import coco.Variables;

public class Not extends Traversible {
	
	Relation relation;

	public Not(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.NOT, "NOT", source);
		relation = new Relation(source, variables);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("LogicalNot\n");
		print.append(relation.printPreOrder(level+1));
		return print.toString();
	}
	
}
