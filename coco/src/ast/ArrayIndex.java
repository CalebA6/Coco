package ast;

import java.util.List;

import coco.Location;
import coco.Token;
import types.ArrayAccessException;
import types.ErrorType;
import types.IntType;
import types.Type;
import types.TypeChecker;

public class ArrayIndex extends NamedNode {

	private Node index;
	private NamedNode item;
	private Token start;
	private String indexSize;
	
	private Location location = index;
	
	public ArrayIndex(NamedNode item, int index, List<Relation> indicies, List<Token> starts, String[] indexSizes) {
		this.index = indicies.get(index).genAST();
		if(index >= indicies.size() - 1) {
			start = starts.get(index);
			indexSize = indexSizes[index];
			this.item = item;
		} else {
			start = starts.get(index);
			indexSize = indexSizes[index];
			this.item = new ArrayIndex(item, index+1, indicies, starts, indexSizes);
		}
	}
	
	public int lineNumber() {
		return item.lineNumber();
	}
	
	public int charPosition() {
		return item.charPosition();
	}
	
	public Token getName() {
		return item.getName();
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Type getType() {
		Type type = item.getType();
		try {
			type.decrementDimensions();
		} catch (ArrayAccessException e) {
			type = new ErrorType();
			((ErrorType) type).setError(start, "Array access on non-array type");
		}
		
		if(!IntType.is(index.getType())) {
			ErrorType error = new ErrorType();
			error.setError(index, "Array index must be integer");
			type = error;
		} else {
			if(index instanceof Literal) {
				boolean outOfBounds = false;
				int index = Integer.parseInt(this.index.toString());
				if(index < 0) outOfBounds = true;
				int indexSize = Integer.parseInt(this.indexSize);
				if(index >= indexSize) outOfBounds = true;
				
				if(outOfBounds) {
					ErrorType error = new ErrorType();
					error.setError(location, "Array Index Out of Bounds : " + index + " for array " + item);
					type = error;
				}
			}
		}
		
		return type;
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(ErrorType.is(getType())) {
			reporter.reportError((ErrorType) getType());
		}

		index.checkType(reporter, returnType, functionName);
		item.checkType(reporter, returnType, functionName);
	}
	
	@Override
	public String toString() {
		return item.toString();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ArrayIndex\n");
		print.append(item.printPreOrder(level+1));
		return print.toString();
	}
	
}
