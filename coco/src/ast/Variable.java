package ast;

import java.util.ArrayList;

import coco.Token;
import ir.ValueCode;
import types.Type;
import types.TypeChecker;

public class Variable extends NamedNode {

	private Token name;
	private String type;
	
	public Variable(Token name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public int lineNumber() {
		return name.lineNumber();
	}
	
	public int charPosition() {
		return name.charPosition();
	}
	
	public Token getName() {
		return name;
	}
	
	public Type getType() {
		return Type.fromString(type, name);
	}
	
	public void checkFunctionCalls(AST parent) { }
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) { }
	
	public ValueCode genCode(ir.Variables variables) {
		return new ValueCode(new ArrayList<>(), name.lexeme());
	}
	
	@Override
	public String toString() {
		return name.lexeme();
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
