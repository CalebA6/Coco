package ast;

import coco.Location;
import types.Type;
import types.TypeChecker;

abstract class Node implements Location {
	
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
	
	abstract public int lineNumber();
	abstract public int charPosition();
	
	abstract public Type getType();
	abstract public void checkType(TypeChecker reporter, Type returnType);
	
}
