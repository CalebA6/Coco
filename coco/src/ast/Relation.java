package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;
import types.BoolType;
import types.Type;
import types.TypeChecker;

public class Relation extends CheckableNode {
	
	private List<Sum> operands = new ArrayList<>();
	private List<Token> operations = new ArrayList<>();

	public Relation(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		operands.add(new Sum(source, variables));
		while(source.hasNext()) {
			Token operation = source.next();
			if((operation.kind() == Kind.EQUAL_TO) || (operation.kind() == Kind.NOT_EQUAL) || (operation.kind() == Kind.LESS_EQUAL) || (operation.kind() == Kind.GREATER_EQUAL) || (operation.kind() == Kind.LESS_THAN) || (operation.kind() == Kind.GREATER_THAN)) {
				operations.add(operation);
				operands.add(new Sum(source, variables));
			} else {
				source.push(operation);
				break;
			}
		}
	}
	
	public int lineNumber() {
		return operands.get(0).lineNumber();
	}
	
	public int charPosition() {
		return operands.get(0).charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		for(Sum operand: operands) {
			operand.checkFunctionCalls(parent);
		}
	}
	
	public Node genAST() {
		if(operations.size() > 0) {
			Operation current = new Operation(operands.get(0).genAST(), operands.get(1).genAST(), "Relation[" + operations.get(0).lexeme() + "]", operations.get(0));
			for(int op=1; op<operations.size(); ++op) {
				current = new Operation(current, operands.get(op+1).genAST(), "Relation[" + operations.get(op).lexeme() + "]", operations.get(op));
			}
			return current;
		} else {
			return operands.get(0).genAST();
		}
	}
	
	public Type getType() {
		return new BoolType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		throw new RuntimeException("Relation is not in AST and should not be typechecked.");
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(operations.size() > 0) {
			for(int relation=0; relation<operations.size(); ++relation) {
				addLevel(level+relation, print);
				print.append("Relation[");
				print.append(operations.get(relation).lexeme());
				print.append("]\n");
				print.append(operands.get(relation).printPreOrder(level+relation+1));
			}
			print.append(operands.get(operations.size()).printPreOrder(level+operations.size()));
		} else {
			print.append(operands.get(0).printPreOrder(level));
		}
		return print.toString();
	}
	
	/* public Type getType() {
		int state = 0;
		for(Sum operand: operands) {
			if(operand.getType() )
		}
	} */
	
}
