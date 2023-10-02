package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.Scanner;
import coco.SyntaxException;
import coco.Variables;
import coco.Token.Kind;

public class AST extends Node {
	
	private Variables varTable = new Variables(this);
	private List<Exception> error = new ArrayList<>();
	
	private VariableDeclarations varDeclarations = null;
	private FunctionDefinitions functions = null;
	private Statements action = null;

	public AST(Scanner scanner) {
		ReversibleScanner source = new ReversibleScanner(scanner);

		if(!source.hasNext()) {
			error.add(new SyntaxException("no program", 0, 0));
			return;
		}
		
		try {
			ErrorChecker.mustBe(Kind.MAIN, "MAIN", source);
		} catch (SyntaxException e1) {
			error.add(e1);
			return;
		}
		
		try {
			varDeclarations = new VariableDeclarations(source, varTable).getAST();
		} catch (SyntaxException e) {
			error.add(e);
			return;
		} catch(RedefinitionException e) {
			error.add(e);
		}
		
		try {
			ErrorChecker.checkForMoreInput(source, "OPEN_BRACE");
		} catch (SyntaxException e1) {
			error.add(e1);
			return;
		}
		try {
			functions = new FunctionDefinitions(source, varTable).genAST();
			if(functions != null) functions.checkFunctionCalls(this);
		} catch (SyntaxException e2) {
			error.add(e2);
			return;
		} catch (RedefinitionException | NonexistantVariableException e) {
			error.add(e);
		}
		
		try {
			ErrorChecker.mustBe(Kind.OPEN_BRACE, "OPEN_BRACE", source);
		} catch(SyntaxException e) {
			error.add(e);
			return;
		}
		
		try {
			action = new Statements(source, varTable);
			action.checkFunctionCalls(this);
		} catch (SyntaxException e) {
			error.add(e);
			return;
		} catch(NonexistantVariableException e) {
			error.add(e);
		}
		
		try {
			ErrorChecker.mustBe(Kind.CLOSE_BRACE, "CLOSE_BRACE", source);
		} catch (SyntaxException e1) {
			error.add(e1);
			return;
		}
		
		try {
			ErrorChecker.mustBe(Kind.PERIOD, "PERIOD", source);
		} catch (SyntaxException e) {
			error.add(e);
			return;
		}
	}
	
	public boolean hasError() {
		return !error.isEmpty();
	}
	
	public void reportError(Exception e) {
		error.add(e);
	}
	
	public String errorReport() {
		StringBuilder report = new StringBuilder();
		for(Exception e: error) {
			report.append(e);
		}
		return report.toString();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("Computation[main:()->void]\n");
		if(varDeclarations != null) {
			print.append(varDeclarations.printPreOrder(level+1));
		}
		if(functions != null) {
			print.append(functions.printPreOrder(level+1));
		}
		if(action != null) {
			print.append(action.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
