package ast;

import coco.Token;

public class Variable extends NamedNode {

	private Token name;
	private String type;
	
	public Variable(Token name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public int line() {
		return name.lineNumber();
	}
	
	public int charPos() {
		return name.charPosition();
	}
	
	public Token getName() {
		return name;
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append(name.lexeme());
		print.append(":");
		print.append(type);
		print.append("\n");
		return print.toString();
	}

}
