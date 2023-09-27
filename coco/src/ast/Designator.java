package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import coco.Variables;

public class Designator extends Traversible {

	private Token name;
	private List<Relation> indicies = new ArrayList<>();
	
	public Designator(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		name = ErrorChecker.mustBe(Kind.IDENT, "IDENT", source);
		variables.get(name);
		while(true) {
			try {
				ErrorChecker.mustBe(Kind.OPEN_BRACKET, "OPEN_BRACKET", source);
			} catch(Exception e) {
				break;
			}
			indicies.add(new Relation(source, variables));
			ErrorChecker.mustBe(Kind.CLOSE_BRACKET, "CLOSE_BRACKET", source);
		}
	}
	
	public Token getName() {
		return name;
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append(name.lexeme());
		print.append("\n");
		for(Relation index: indicies) {
			addLevel(level+1, print);
			print.append("ArrayIndex\n");
			print.append(index.printPreOrder(level+2));
		}
		return print.toString();
	}
	
}
