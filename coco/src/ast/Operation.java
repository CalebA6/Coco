package ast;

import coco.Token;
import types.Type;

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
	
	public int line() {
		return opToken.lineNumber();
	}
	
	public int charPos() {
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
