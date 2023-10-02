package ast;

import coco.Type;

abstract class Node {
	
	public Node genAST() {
		return this;
	}

	public String printPreOrder() {
		return printPreOrder(0);
	};
	abstract public String printPreOrder(int level);
	
	protected void addLevel(int level, StringBuilder print) {
		for(int l=0; l<level; ++l) {
			print.append("  ");
		}
	}
	
	// abstract public Type getType();
	
}
