package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Sum extends Traversible {
	
	private List<Product> operands = new ArrayList<>();
	private ArrayList<Token> operations = new ArrayList<>();

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
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(operations.size() > 0) {
			for(int opIndex=0; opIndex<operations.size(); ++opIndex) {
				addLevel(level+opIndex, print);
				Token operation = operations.get(opIndex);
				if(operation.kind() == Kind.ADD) {
					print.append("Addition\n");
				} else if(operation.kind() == Kind.SUB) { 
					print.append("Subtract\n");
				} else if(operation.kind() == Kind.OR) {
					print.append("LogicalOr\n");
				} else {
					print.append(operation.kind());
					print.append("\n");
				}
				operands.get(opIndex).printPreOrder(level+opIndex+1);
			}
			operands.get(operations.size()).printPreOrder(level+operations.size());
		} else {
			print.append(operands.get(0).printPreOrder(level));
		}
		return print.toString();
	}
	
}
