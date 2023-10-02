package ast;

public class Operation extends CheckableNode {
	
	private Node left;
	private Node right;
	private String operation;
	
	public Operation(Node left, Node right, String operation) {
		this.left = left;
		this.right = right;
		this.operation = operation;
	}
	
	public void checkFunctionCalls(AST parent) {
		if(left instanceof CheckableNode) ((CheckableNode) left).checkFunctionCalls(parent);
		if(right instanceof CheckableNode) ((CheckableNode) right).checkFunctionCalls(parent);
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
