package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import types.Type;
import types.TypeChecker;
import coco.Token.Kind;

public class Sum extends CheckableNode {
	
	private List<Node> operands = new ArrayList<>();
	private ArrayList<Token> operations = new ArrayList<>();
	
	// Increment
	public Sum(Node var, Token op) {
		operands.add(var);
		Token add = new Token("+", op.lineNumber(), op.charPosition());
		operations.add(add);
		operands.add(new Literal(new Token("1", op.lineNumber(), op.charPosition())));
	}

	public Sum(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		operands.add(new Product(source, variables));
		while(source.hasNext()) {
			Token operation = source.next();
			if((operation.kind() == Kind.ADD) || (operation.kind() == Kind.SUB) || (operation.kind() == Kind.OR)) {
				operations.add(operation);
				operands.add(new Product(source, variables));
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
		for(Node operand: operands) {
			if(operand instanceof CheckableNode) {
				((CheckableNode) operand).checkFunctionCalls(parent);
			}
		}
	}
	
	public Node genAST() {
		if(operations.size() > 0) {
			Operation current = new Operation(operands.get(0).genAST(), operands.get(1).genAST(), operationString(operations.get(0)), operations.get(0));
			for(int op=1; op<operations.size(); ++op) {
				current = new Operation(current, operands.get(op+1).genAST(), operationString(operations.get(op)), operations.get(op));
			}
			return current;
		} else {
			return operands.get(0).genAST();
		}
	}
	
	public Type getType() {
		return operands.get(0).getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		throw new RuntimeException("Sum is not in AST and should not be typechecked.");
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(operations.size() > 0) {
			for(int opIndex=0; opIndex<operations.size(); ++opIndex) {
				addLevel(level+opIndex, print);
				Token operation = operations.get(opIndex);
				if(operation.kind() == Kind.ADD) {
					print.append("Addition\n");
				} else if(operation.kind() == Kind.SUB) { 
					print.append("Subtraction\n");
				} else if(operation.kind() == Kind.OR) {
					print.append("LogicalOr\n");
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
		if(operation.kind() == Kind.ADD) {
			return "Addition";
		} else if(operation.kind() == Kind.SUB) { 
			return "Subtraction";
		} else if(operation.kind() == Kind.OR) {
			return "LogicalOr";
		} else {
			return operation.kind().name();
		}
	}
	
	/* public Type getType() {
		int state = 0;
		for(int opPos=0; opPos<operands.size(); ++opPos) {
			Node operand = operands.get(opPos);
			if(operand.getType() == Type.ERROR) return operand.getType();
			if(operand.getType() == Type.VOID) {
				Type 
			}
		}
	} */
	
}
