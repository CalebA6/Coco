package ast;

import java.util.List;

import coco.Token;

public class ArrayIndex extends NamedNode {

	private Relation index;
	private NamedNode item;
	
	public ArrayIndex(NamedNode item, int index, List<Relation> indicies) {
		this.index = indicies.get(index);
		if(index >= indicies.size() - 1) {
			this.item = item;
		} else {
			this.item = new ArrayIndex(item, index+1, indicies);
		}
	}
	
	public Token getName() {
		return item.getName();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ArrayIndex\n");
		print.append(item.printPreOrder(level+1));
		return print.toString();
	}
	
}
