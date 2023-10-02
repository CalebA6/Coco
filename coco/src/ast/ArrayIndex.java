package ast;

import java.util.List;

import coco.Token;
import types.ArrayAccessException;
import types.Type;
import types.TypeChecker;

public class ArrayIndex extends NamedNode {

	private Relation index;
	private NamedNode item;
	private Token start;
	
	public ArrayIndex(NamedNode item, int index, List<Relation> indicies, List<Token> starts) {
		this.index = indicies.get(index);
		if(index >= indicies.size() - 1) {
			this.item = item;
		} else {
			start = starts.get(index);
			this.item = new ArrayIndex(item, index+1, indicies, starts);
		}
	}
	
	public int lineNumber() {
		return start.lineNumber();
	}
	
	public int charPosition() {
		return start.charPosition();
	}
	
	public Token getName() {
		return item.getName();
	}
	
	public Type getType() {
		Type type = item.getType();
		try {
			type.decrementDimensions();
		} catch (ArrayAccessException e) {
			type = Type.ERROR;
			type.setError(start, "Array access on non-array type");
		}
		return type;
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		if(getType() == Type.ERROR) {
			reporter.reportError(getType());
		}
		
		if(index.getType() != Type.INT) {
			Type error = Type.ERROR;
			error.setError(index, "Array index must be integer");
			reporter.reportError(error);
		}
		
		item.checkType(reporter, returnType);
		index.checkType(reporter, returnType);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ArrayIndex\n");
		print.append(item.printPreOrder(level+1));
		return print.toString();
	}
	
}
