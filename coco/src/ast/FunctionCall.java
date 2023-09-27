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

public class FunctionCall extends Traversible {
	
	Token function;
	private List<Relation> parameters = new ArrayList<>();
	String type;

	public FunctionCall(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.CALL, "CALL", source);
		ErrorChecker.checkForMoreInput(source, "FUNCTION NAME");
		function = source.next();
		type = variables.get(function);
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
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("FunctionCall[");
		print.append(function.lexeme());
		print.append(type);
		print.append("]\n");
		addLevel(level+1, print);
		print.append("ArgumentList\n");
		for(Relation parameter: parameters) {
			print.append(parameter.printPreOrder(level+2));
		}
		return print.toString();
	}
	
}
