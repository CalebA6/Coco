package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Relation extends Traversible {
	
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
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(operations.size() > 0) {
			for(int relation=0; relation<operations.size(); ++relation) {
				addLevel(level+relation, print);
				print.append("Relation[");
				print.append(operations.get(relation).lexeme());
				print.append("]");
				operands.get(relation).printPreOrder(level+relation+1);
			}
			operands.get(operations.size()).printPreOrder(level+operations.size());
		} else {
			print.append(operands.get(0).printPreOrder(level));
		}
		return print.toString();
	}
	
}
