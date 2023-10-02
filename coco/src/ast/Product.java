package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import types.Type;
import coco.Token.Kind;

public class Product extends CheckableNode {
	
	private List<Power> operands = new ArrayList<>();
	private List<Token> operations = new ArrayList<>();

	public Product(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		operands.add(new Power(source, variables));
		while(source.hasNext()) {
			Token operation = source.next();
			if((operation.kind() == Kind.MUL) || (operation.kind() == Kind.DIV) || (operation.kind() == Kind.MOD) || (operation.kind() == Kind.AND)) {
				operations.add(operation);
				operands.add(new Power(source, variables));
			} else {
				source.push(operation);
				break;
			}
		}
	}
	
	public int line() {
		return operands.get(0).line();
	}
	
	public int charPos() {
		return operands.get(0).charPos();
	}
	
	public void checkFunctionCalls(AST parent) {
		for(Power operand: operands) {
			operand.checkFunctionCalls(parent);
		}
	}
	
	public Node genAST() {
		if(operations.size() > 0) {
			Operation current = new Operation(operands.get(0), operands.get(1), operationString(operations.get(0)), operations.get(0));
			for(int op=1; op<operations.size(); ++op) {
				current = new Operation(current, operands.get(op+1), operationString(operations.get(op)), operations.get(op));
			}
			return current;
		} else {
			return operands.get(0).genAST();
		}
	}
	
	public Type getType() {
		return operands.get(0).getType();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(operations.size() > 0) {
			for(int opIndex=0; opIndex<operations.size(); ++opIndex) {
				addLevel(level+opIndex, print);
				Token operation = operations.get(opIndex);
				if(operation.kind() == Kind.MUL) {
					print.append("Multiplication\n");
				} else if(operation.kind() == Kind.DIV) { 
					print.append("Division\n");
				} else if(operation.kind() == Kind.MOD) {
					print.append("Modulo\n");
				} else if(operation.kind() == Kind.AND) {
					print.append("LogicalAnd\n");
				} else {
					print.append(operation.kind());
					print.append("\n");
				}
				print.append(operands.get(opIndex).printPreOrder(level+opIndex+1));
			}
			print.append(operands.get(operations.size()).printPreOrder(level+operations.size()));
		} else {
			print.append(operands.get(0).printPreOrder(level));
		}
		return print.toString();
	}
	
	private String operationString(Token operation) {
		if(operation.kind() == Kind.MUL) {
			return "Multiplication";
		} else if(operation.kind() == Kind.DIV) { 
			return "Division";
		} else if(operation.kind() == Kind.MOD) {
			return "Modulo";
		} else if(operation.kind() == Kind.AND) {
			return "LogicalAnd";
		} else {
			return operation.kind().name();
		}
	}
	
}
