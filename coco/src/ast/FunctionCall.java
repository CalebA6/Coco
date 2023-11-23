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
import ir.Instruction;
import ir.ValueCode;
import types.ErrorType;
import types.Type;
import types.TypeChecker;
import types.TypeList;
import coco.Token.Kind;

public class FunctionCall extends CheckableNode {
	
	private Token function;
	private List<Node> parameters = new ArrayList<>();
	private Variables variables;
	private Set<String> types;
	
	private Token call;

	public FunctionCall(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.CALL, "CALL", source);
		call = source.last();
		ErrorChecker.checkForMoreInput(source, "FUNCTION NAME");
		function = source.next();
		this.variables = variables;
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);

		ErrorChecker.checkForMoreInput(source, "CLOSE_PAREN");
		Token argument = source.peek();
		if(argument.kind() != Kind.CLOSE_PAREN) {
			while(true) {
				parameters.add(new Relation(source, variables).genAST());
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
		return call.lineNumber();
	}
	
	public int charPosition() {
		return call.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		try {
			types = variables.get(function);
		} catch(NonexistantVariableException e) {
			parent.reportError(e);
		}
		
		for(Node parameter: parameters) {
			if(parameter instanceof CheckableNode) ((CheckableNode) parameter).checkFunctionCalls(parent);
		}
	}
	
	public Type getType() {
		if(types != null) {
			String returnType = getReturnType();
			if(returnType != null) return Type.fromString(returnType, this);
		}
		ErrorType error = new ErrorType();
		error.setError(this, "Call with args " + TypeList.fromList(parameters) + " matches no function signature.");
		return error;
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		Type error = getType();
		if(ErrorType.is(error)) {
			reporter.reportError((ErrorType) error);
		}

		for(Node parameter: parameters) {
			parameter.checkType(reporter, returnType, functionName);
		}
	}
	
	public ValueCode genCode(ir.Variables variables) {
		List<Instruction> instructions = new ArrayList<>();
		String result = variables.getTemp();
		List<String> params = new ArrayList<>();
		for(Node param: parameters) {
			ValueCode paramCode = param.genCode(variables);
			instructions.addAll(paramCode.instructions);
			params.add(paramCode.returnValue);
		}
		
		String call = "call " + function.lexeme() + "(";
		boolean first = true;
		for(String param: params) {
			if(first) {
				first = false;
			} else {
				call += ", ";
			}
			call += param;
		}
		call += ")";
		
		if(getReturnType().equals("void")) {
			instructions.add(new Instruction(call));
			return new ValueCode(instructions, call);
		}
		instructions.add(new Instruction(result, call));
		return new ValueCode(instructions, result);
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
		for(Node parameter: parameters) {
			print.append(parameter.printPreOrder(level+2));
		}
		return print.toString();
	}
	
	private String getReturnType() {
		for(String type: types) {
			String[] typeParts = type.split("->");
			String paramTypes = typeParts[0];
			String returnType = typeParts[1];
			TypeList possibleParameters = TypeList.fromString(paramTypes, call);
			boolean correct = possibleParameters.size() == parameters.size();
			for(int p=0; p<parameters.size()&&correct; ++p) {
				correct &= parameters.get(p).getType().equals(possibleParameters.get(p));
			}
			if(correct) {
				return returnType;
			}
		}
		return null;
	}
	
}
