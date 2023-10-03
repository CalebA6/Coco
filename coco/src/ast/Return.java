package ast;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import types.ErrorType;
import types.Type;
import types.TypeChecker;
import types.VoidType;
import coco.Variables;

public class Return extends CheckableNode {
	
	private Node value;
	private Token start;

	public Return(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.RETURN, "RETURN", source);
		start = source.last();
		Token next = source.peek();
		if(next.kind() == Kind.SEMICOLON) {
			value = null;
		} else {
			value = new Relation(source, variables).genAST();
		}
	}
	
	public int lineNumber() {
		return start.lineNumber();
	}
	
	public int charPosition() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(value != null && value instanceof CheckableNode) ((CheckableNode) value).checkFunctionCalls(parent);
	}
	
	public Type getType() {
		if(value == null) return new VoidType();
		return value.getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(value != null) value.checkType(reporter, returnType, functionName);
		
		if(value == null) {
			if(!returnType.equals(value.getType())) {
				ErrorType error = new ErrorType();
				error.setError(this, "Function " + functionName + " returns " + value.getType() + " instead of " + returnType + ".");
				reporter.reportError(error);
			}
		} else {
			if(!returnType.equals(value.getType())) {
				ErrorType error = new ErrorType();
				error.setError(this, "Function " + functionName + " returns " + value.getType() + " instead of " + returnType + ".");
				reporter.reportError(error);
			}
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ReturnStatement\n");
		if(value != null) {
			print.append(value.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
