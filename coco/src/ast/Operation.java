package ast;

import coco.Token;
import coco.Token.Kind;
import types.BoolType;
import types.ErrorType;
import types.NumberType;
import types.Type;
import types.TypeChecker;

public class Operation extends CheckableNode {
	
	private Node left;
	private Node right;
	private String operation;
	private Token opToken;
	
	public Operation(Node left, Node right, String operation, Token opToken) {
		this.left = left;
		this.right = right;
		this.operation = operation;
		this.opToken = opToken;
	}
	
	public int lineNumber() {
		return opToken.lineNumber();
	}
	
	public int charPosition() {
		return opToken.lineNumber();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(left instanceof CheckableNode) ((CheckableNode) left).checkFunctionCalls(parent);
		if(right instanceof CheckableNode) ((CheckableNode) right).checkFunctionCalls(parent);
	}
	
	public Type getType() {
		if(opToken.kind() == Kind.AND || opToken.kind() == Kind.OR) {
			if(!BoolType.is(left.getType()) || !BoolType.is(right.getType())) {
				ErrorType error = new ErrorType();
				error.setError(opToken, "Cannot " + opName(opToken) + " " + left.getType().toString().toLowerCase() + " to " + right.getType().toString().toLowerCase() + ".");
				return error;
			} else {
				return new BoolType();
			}
		} else {
			if(!NumberType.is(left.getType()) || !NumberType.is(right.getType())) {
				ErrorType error = new ErrorType();
				String opAction = opName(opToken).equals("compare") ? " with " : " to ";
				error.setError(opToken, "Cannot " + opName(opToken) + " " + left.getType().toString().toLowerCase() + opAction + right.getType().toString().toLowerCase() + ".");
				return error;
			} else {
				return left.getType();
			}
		}
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		Type opType = getType();
		if(opType instanceof ErrorType) {
			reporter.reportError((ErrorType) opType);
		}
		
		left.checkType(reporter, returnType);
		right.checkType(reporter, returnType);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append(operation);
		print.append("\n");
		print.append(left.printPreOrder(level+1));
		print.append(right.printPreOrder(level+1));
		return print.toString();
	}
	
	private String opName(Token op) {
		switch(op.kind()) {
			case ADD: 
				return "add";
			case EQUAL_TO: 
				return "compare";
			default: 
				return op.kind().name();
		}
	}
	
}
