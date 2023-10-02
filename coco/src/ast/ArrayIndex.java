package ast;

import java.util.List;

import coco.Token;
import types.ArrayAccessException;
import types.Type;

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
	
	public int line() {
		return start.lineNumber();
	}
	
	public int charPos() {
		return start.charPosition();
	}
	
	public Token getName() {
		return item.getName();
	}
	
	/* public Type getType() {
		Type type = item.getType();
		try {
			type.decrementDimensions();
		} catch (ArrayAccessException e) {
			type = Type.ERROR;
			type.setError(start, "Array access on non-array type");
		}
		return type;
	} */
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ArrayIndex\n");
		print.append(item.printPreOrder(level+1));
		return print.toString();
	}
	
}
