package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
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
	
	public void checkFunctionCalls(AST parent) {
		for(Power operand: operands) {
			operand.checkFunctionCalls(parent);
		}
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
	
}
