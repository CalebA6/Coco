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
import types.Type;
import types.TypeChecker;
import types.VoidType;

public class Statements extends CheckableNode {
	
	private List<CheckableNode> statements = new ArrayList<>();

	public Statements(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		Token next;
		do {
			statements.add(genStatement(source, variables));
			
			ErrorChecker.mustBe(Kind.SEMICOLON, "SEMICOLON", source);
			
			next = source.peek();
		} while((next.kind() != Kind.CLOSE_BRACE) && (next.kind() != Kind.FI) && (next.kind() != Kind.ELSE) && (next.kind() != Kind.OD) && (next.kind() != Kind.UNTIL));
	}
	
	public int lineNumber() {
		return statements.get(0).lineNumber();
	}
	
	public int charPosition() {
		return statements.get(0).charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		for(CheckableNode statement: statements) {
			statement.checkFunctionCalls(parent);
		}
	}
	
	public Type getType() {
		for(Node statement: statements) {
			if(statement instanceof Return) {
				return statement.getType();
			}
		}
		return new VoidType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		for(Node statement: statements) {
			statement.checkType(reporter, returnType);
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("StatementSequence\n");
		for(Node statement: statements) {
			print.append(statement.printPreOrder(level+1));
		}
		return print.toString();
	}
	
	private CheckableNode genStatement(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		CheckableNode statement;
		
		ErrorChecker.checkForMoreInput(source, "STATEMENT");
		Token token = source.peek();
		if(token.kind() == Kind.IDENT) {
			statement = new Assignment(source, variables);
		} else if(token.kind() == Kind.CALL) {
			statement = new FunctionCall(source, variables);
		} else if(token.kind() == Kind.IF) {
			statement = new If(source, variables);
		} else if(token.kind() == Kind.WHILE) {
			statement = new While(source, variables);
		} else if(token.kind() == Kind.REPEAT) {
			statement = new Repeat(source, variables);
		} else if(token.kind() == Kind.RETURN) {
			statement = new Return(source, variables);
		} else {
			throw new SyntaxException("Expected statement but got " + token.kind() + ".", token);
		}
		
		return statement;
	}
	
}
