package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Power extends CheckableNode {
	
	private List<Node> operands = new ArrayList<>();
	private List<Token> operations = new ArrayList<>();

	public Power(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		operands.add(getGroup(source, variables));
		while(source.hasNext()) {
			Token operation = source.next();
			if(operation.kind() == Kind.POW) {
				operations.add(operation);
				operands.add(getGroup(source, variables));
			} else {
				source.push(operation);
				break;
			}
		}
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
			Operation current = new Operation(operands.get(0), operands.get(1), operationString(operations.get(0)));
			for(int op=1; op<operations.size(); ++op) {
				current = new Operation(current, operands.get(op+1), operationString(operations.get(op)));
			}
			return current;
		} else {
			return operands.get(0).genAST();
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(operations.size() > 0) {
			for(int opIndex=0; opIndex<operations.size(); ++opIndex) {
				addLevel(level+opIndex, print);
				Token operation = operations.get(opIndex);
				if(operation.kind() == Kind.POW) {
					print.append("Power\n");
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
	
	private Node getGroup(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.checkForMoreInput(source, "group");
		Token token = source.next();
		if((token.kind() == Kind.INT_VAL) || (token.kind() == Kind.FLOAT_VAL) || (token.kind() == Kind.TRUE) || (token.kind() == Kind.FALSE)) {
			return new Literal(token);
		} else if(token.kind() == Kind.IDENT) {
			source.push(token);
			return new Designator(source, variables).genAST();
		} else if(token.kind() == Kind.NOT) {
			source.push(token);
			return new Not(source, variables);
		} else if(token.kind() == Kind.OPEN_PAREN) {
			Relation value = new Relation(source, variables);
			ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
			return value;
		} else if(token.kind() == Kind.CALL) {
			source.push(token);
			return new FunctionCall(source, variables);
		} else {
			throw new SyntaxException("Expected group but got " + token.kind() + ".", token);
		}
	}
	
	private String operationString(Token operation) {
		if(operation.kind() == Kind.POW) {
			return "Power";
		} else {
			return operation.kind().name();
		}
	}
	
}
