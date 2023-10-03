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
				error.setError(opToken, "Cannot " + toString(opToken, left.getType().toString(), right.getType().toString()) + ".");
				return error;
			} else {
				return new BoolType();
			}
		} else {
			if(!NumberType.is(left.getType()) || !NumberType.is(right.getType())) {
				ErrorType error = new ErrorType();
				error.setError(opToken, "Cannot " + toString(opToken, left.getType().toString(), right.getType().toString()) + ".");
				return error;
			} else {
				if(operation.startsWith("Relation")) {
					return new BoolType();
				} else {
					return left.getType();
				}
			}
		}
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		Type opType = getType();
		if(opType instanceof ErrorType) {
			reporter.reportError((ErrorType) opType);
		}
		
		left.checkType(reporter, returnType, functionName);
		right.checkType(reporter, returnType, functionName);
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
	
	private String toString(Token op, String left, String right) {
		switch(op.kind()) {
			case ADD: 
				return "add " + left + " to " + right;
			case SUB: 
				return "subtract " + right + " from " + left;
			case EQUAL_TO: 
				return "compare " + left + " with " + right;
			default: 
				return op.kind().name() + " " + left + " to " + right;
		}
	}
	
}
