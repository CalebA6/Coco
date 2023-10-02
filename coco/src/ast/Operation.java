package ast;

import coco.Token;
import coco.Token.Kind;
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
		if(operation.startsWith("Relation")) {
			return Type.BOOL;
		} else {
			return left.getType();
		}
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		if(opToken.kind() == Kind.AND || opToken.kind() == Kind.OR) {
			Node[] operands = {left, right};
			for(Node operand: operands) {
				if(operand.getType() != Type.BOOL) {
					Type error = Type.ERROR;
					error.setError(operand, "Operands of " + operation + " operation must be BOOL.");
				}
			}
		} else {
			Node[] operands = {left, right};
			for(Node operand: operands) {
				if(operand.getType() != Type.INT && operand.getType() != Type.FLOAT) {
					Type error = Type.ERROR;
					error.setError(operand, "Operands of " + operation + " operation must be numerical.");
				}
			}
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
	
}
