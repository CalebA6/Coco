package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Statement extends Traversible {
	
	Traversible statement;

	public Statement(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.checkForMoreInput(source, "STATEMENT");
		Token token = source.peek();
		if(token.kind() == Kind.IDENT) {
			statement = new Assignment(source, variables);
		} else if(token.kind() == Kind.CALL) {
			statement = new FunctionCall(source, variables);
		} else if(token.kind() == Kind.IF) {
			statement = new If(source, variables);
		} else if(token.kind() == Kind.WHILE) {
			statement = new While(source, variables);
		} else if(token.kind() == Kind.REPEAT) {
			statement = new Repeat(source, variables);
		} else if(token.kind() == Kind.RETURN) {
			statement = new Return(source, variables);
		} else {
			throw new SyntaxException("Expected statement but got " + token.kind() + ".", token);
		}
	}
	
	public String printPreOrder(int level) {
		return statement.printPreOrder(level);
	}
	
}
