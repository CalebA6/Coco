package ast;

import coco.Token;
import coco.Token.Kind;
import types.Type;
import types.TypeChecker;

public class Literal extends CheckableNode {

	private Token literal;
	
	public Literal(Token literal) {
		this.literal = literal;
	}
	
	public int lineNumber() {
		return literal.lineNumber();
	}
	
	public int charPosition() {
		return literal.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) { }
	
	public Type getType() {
		return Type.fromToken(literal);
	}
	
	public void checkType(TypeChecker reporter, Type returnType) { }
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		if((literal.kind() == Kind.TRUE) || (literal.kind() == Kind.FALSE)) {
			print.append("BoolLiteral[");
		} else if(literal.kind() == Kind.INT_VAL) {
			print.append("IntegerLiteral[");
		} else if(literal.kind() == Kind.FLOAT_VAL) {
			print.append("FloatLiteral[");
		} else {
			print.append("Literal[");
		}
		print.append(literal.lexeme());
		print.append("]\n");
		return print.toString();
	}
	
}
