package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import coco.Variables;
import ir.Instruction;
import types.Type;
import types.TypeChecker;

public class FunctionBody extends Node {
	
	VariableDeclarations varDeclarations;
	Statements statements;
	Token start;

	public FunctionBody(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException, RedefinitionException {
		ErrorChecker.mustBe(Kind.OPEN_BRACE, "OPEN_BRACE", source);
		start = source.last();
		varDeclarations = new VariableDeclarations(source, variables);
		statements = new Statements(source, variables);
		ErrorChecker.mustBe(Kind.CLOSE_BRACE, "CLOSE_BRACE", source);
		ErrorChecker.mustBe(Kind.SEMICOLON, "SEMICOLON", source);
	}
	
	public int lineNumber() {
		return start.lineNumber();
	}
	
	public int charPosition() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		statements.checkFunctionCalls(parent);
	}
	
	public Type getType() {
		return statements.getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		statements.checkType(reporter, returnType, functionName);
	}
	
	public List<Instruction> genCode() {
		return new ArrayList<>();
		// return statements.genIr();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("FunctionBody\n");
		print.append(varDeclarations.printPreOrder(level+1));
		print.append(statements.printPreOrder(level+1));
		return print.toString();
	}
	
}
