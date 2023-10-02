package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import types.Type;
import types.TypeChecker;
import coco.Token.Kind;

public class FunctionCall extends CheckableNode {
	
	Token function;
	private List<Relation> parameters = new ArrayList<>();
	Variables variables;
	Set<String> types;

	public FunctionCall(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.CALL, "CALL", source);
		ErrorChecker.checkForMoreInput(source, "FUNCTION NAME");
		function = source.next();
		this.variables = variables;
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);

		ErrorChecker.checkForMoreInput(source, "CLOSE_PAREN");
		Token argument = source.peek();
		if(argument.kind() != Kind.CLOSE_PAREN) {
			while(true) {
				parameters.add(new Relation(source, variables));
				ErrorChecker.checkForMoreInput(source, "CLOSE_PAREN");
				Token punctuation = source.next();
				if(punctuation.kind() == Kind.COMMA) {
					continue;
				} else if(punctuation.kind() == Kind.CLOSE_PAREN) {
					break;
				} else {
					throw new SyntaxException("Expected CLOSE_PAREN but got " + punctuation.kind() + ".", punctuation);
				}
			}
		} else {
			source.next();
		}
	}
	
	public int lineNumber() {
		return function.lineNumber();
	}
	
	public int charPosition() {
		return function.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		try {
			types = variables.get(function);
		} catch(NonexistantVariableException e) {
			parent.reportError(e);
		}
	}
	
	public Type getType() {
		String paramsType = parameters.toString();
		for(String type: types) {
			if(type.contains(paramsType)) {
				return Type.fromString(type.split("->")[1], function);
			}
		}
		Type error = Type.ERROR;
		error.setError(function, "No such function");
		return error;
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		Type error = getType();
		if(error == Type.ERROR) {
			reporter.reportError(error);
		}
		
		for(Relation parameter: parameters) {
			parameter.checkType(reporter, returnType);
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("FunctionCall[");
		print.append(function.lexeme());
		print.append(":");
		boolean first = true;
		for(String type: types) {
			if(first) {
				first = false;
			} else {
				print.append(", ");
				print.append(function.lexeme());
				print.append(":");
			}
			print.append(type);
		}
		print.append("]\n");
		addLevel(level+1, print);
		print.append("ArgumentList\n");
		for(Relation parameter: parameters) {
			print.append(parameter.printPreOrder(level+2));
		}
		return print.toString();
	}
	
}
