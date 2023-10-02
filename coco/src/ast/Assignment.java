package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Assignment extends CheckableNode {
	
	NamedNode assignee;
	Token operation;
	CheckableNode operand = null;

	public Assignment(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		assignee = (NamedNode)new Designator(source, variables).genAST();
		ErrorChecker.checkForMoreInput(source, "assignment operator");
		Token assignOp = source.next();
		operation = assignOp;
		if((assignOp.kind() == Kind.ASSIGN) || (assignOp.kind() == Kind.ADD_ASSIGN) || (assignOp.kind() == Kind.SUB_ASSIGN) || (assignOp.kind() == Kind.MUL_ASSIGN) || (assignOp.kind() == Kind.DIV_ASSIGN) || (assignOp.kind() == Kind.MOD_ASSIGN) || (assignOp.kind() == Kind.POW_ASSIGN)) {
			operand = new Relation(source, variables);
		} else if((assignOp.kind() == Kind.UNI_INC) || (assignOp.kind() == Kind.UNI_DEC)) {
			operand = new Sum(assignee, assignOp);
		} else {
			throw new SyntaxException("Expected assignment operator but got " + assignOp.kind() + " .", assignOp);
		}
	}
	
	public int line() {
		return operation.lineNumber();
	}
	
	public int charPos() {
		return operation.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(operand != null) {
			operand.checkFunctionCalls(parent);
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("Assignment\n");
		print.append(assignee.printPreOrder(level+1));
		if(operand != null) {
			print.append(operand.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
