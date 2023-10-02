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

public class Designator extends Node {

	private Token name;
	private String type;
	private List<Relation> indicies = new ArrayList<>();
	private List<Token> starts = new ArrayList<>();
	
	public Designator(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		name = ErrorChecker.mustBe(Kind.IDENT, "IDENT", source);
		type = variables.get(name).toArray(new String[0])[0];
		while(true) {
			try {
				ErrorChecker.mustBe(Kind.OPEN_BRACKET, "OPEN_BRACKET", source);
				starts.add(source.last());
			} catch(Exception e) {
				break;
			}
			indicies.add(new Relation(source, variables));
			ErrorChecker.mustBe(Kind.CLOSE_BRACKET, "CLOSE_BRACKET", source);
		}
	}
	
	public Node genAST() {
		Variable var = new Variable(name, type);
		if(indicies.size() > 0) {
			return new ArrayIndex(var, 0, indicies, starts);
		} else {
			return var;
		}
	}
	
	public int line() {
		return name.lineNumber();
	}
	
	public int charPos() {
		return name.charPosition();
	}
	
	public Token getName() {
		return name;
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append(name.lexeme());
		print.append(":");
		print.append(type);
		print.append("\n");
		for(Relation index: indicies) {
			addLevel(level+1, print);
			print.append("ArrayIndex\n");
			print.append(index.printPreOrder(level+2));
		}
		return print.toString();
	}
	
}
