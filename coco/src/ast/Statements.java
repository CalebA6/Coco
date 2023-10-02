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

public class Statements extends CheckableNode {
	
	private List<Statement> statements = new ArrayList<>();

	public Statements(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		Token next;
		do {
			statements.add(new Statement(source, variables));
			
			ErrorChecker.mustBe(Kind.SEMICOLON, "SEMICOLON", source);
			
			next = source.peek();
		} while((next.kind() != Kind.CLOSE_BRACE) && (next.kind() != Kind.FI) && (next.kind() != Kind.ELSE) && (next.kind() != Kind.OD) && (next.kind() != Kind.UNTIL));
	}
	
	public void checkFunctionCalls(AST parent) {
		for(Statement statement: statements) {
			statement.checkFunctionCalls(parent);
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("StatementSequence\n");
		for(Statement statement: statements) {
			print.append(statement.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
