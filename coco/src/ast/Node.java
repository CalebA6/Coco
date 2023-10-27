package ast;

import java.util.ArrayList;

import coco.Location;
import ir.ValueCode;
import types.Type;
import types.TypeChecker;

public abstract class Node implements Location {
	
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
	
	public ValueCode genCode(ir.Variables variables) {
		return new ValueCode(new ArrayList<>(), "-invalid");
	}
	
	abstract public int lineNumber();
	abstract public int charPosition();
	
	abstract public Type getType();
	abstract public void checkType(TypeChecker reporter, Type returnType, String functionName);
	
}
