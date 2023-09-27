package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Assignment extends Traversible {
	
	Designator assignee;
	String type;
	Token operation;
	Relation operand = null;

	public Assignment(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		assignee = new Designator(source, variables);
		type = variables.get(assignee.getName());
		ErrorChecker.checkForMoreInput(source, "assignment operator");
		Token assignOp = source.next();
		operation = assignOp;
		if((assignOp.kind() == Kind.ASSIGN) || (assignOp.kind() == Kind.ADD_ASSIGN) || (assignOp.kind() == Kind.SUB_ASSIGN) || (assignOp.kind() == Kind.MUL_ASSIGN) || (assignOp.kind() == Kind.DIV_ASSIGN) || (assignOp.kind() == Kind.MOD_ASSIGN) || (assignOp.kind() == Kind.POW_ASSIGN)) {
			operand = new Relation(source, variables);
		} else if((assignOp.kind() == Kind.UNI_INC) || (assignOp.kind() == Kind.UNI_DEC)) {
		} else {
			throw new SyntaxException("Expected assignment operator but got " + assignOp.kind() + " .", assignOp);
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("Assignment\n");
		addLevel(level+1, print);
		print.append(assignee.getName().lexeme());
		print.append(":");
		print.append(type);
		print.append("\n");
		if(operand != null) {
			addLevel(level+1, print);
			operand.printPreOrder(level+1);
		}
		return print.toString();
	}
	
}
