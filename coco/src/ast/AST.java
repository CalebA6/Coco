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

public class AST extends Traversible {
	
	private Variables varTable = new Variables();
	private Exception error = null;
	
	private VariableDeclarations varDeclarations = null;
	private List<FunctionDeclaration> functions = new ArrayList<>();
	private Statements action = null;

	public AST(Scanner scanner) {
		ReversibleScanner source = new ReversibleScanner(scanner);

		if(!source.hasNext()) {
			error = new SyntaxException("no program", 0, 0);
			return;
		}
		
		try {
			ErrorChecker.mustBe(Kind.MAIN, "MAIN", source);
		} catch (SyntaxException e1) {
			error = e1;
			return;
		}
		
		try {
			varDeclarations = new VariableDeclarations(source, varTable);
		} catch (SyntaxException | RedefinitionException e) {
			error = e;
			return;
		}
		
		try {
			ErrorChecker.checkForMoreInput(source, "OPEN_BRACE");
		} catch (SyntaxException e1) {
			error = e1;
			return;
		}
		while(source.peek().kind() == Kind.FUNC) {
			try {
				functions.add(new FunctionDeclaration(source, varTable));
			} catch (SyntaxException | RedefinitionException | NonexistantVariableException e) {
				error = e;
				return;
			}
			try {
				ErrorChecker.checkForMoreInput(source, "OPEN_BRACE");
			} catch (SyntaxException e) {
				error = e;
				return;
			}
		}
		
		try {
			ErrorChecker.mustBe(Kind.OPEN_BRACE, "OPEN_BRACE", source);
		} catch(SyntaxException e) {
			error = e;
			return;
		}
		
		try {
			action = new Statements(source, varTable);
		} catch (SyntaxException | NonexistantVariableException e) {
			error = e;
			return;
		}
		
		try {
			ErrorChecker.mustBe(Kind.CLOSE_BRACE, "CLOSE_BRACE", source);
		} catch (SyntaxException e1) {
			error = e1;
			return;
		}
		
		try {
			ErrorChecker.mustBe(Kind.PERIOD, "PERIOD", source);
		} catch (SyntaxException e) {
			error = e;
			return;
		}
	}
	
	public boolean hasError() {
		return error != null;
	}
	
	public String errorReport() {
		return error.toString();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("Computation[main:()->void]\n");
		if(varDeclarations != null) {
			print.append(varDeclarations.printPreOrder(level+1));
		}
		for(FunctionDeclaration function: functions) {
			print.append(function.printPreOrder(level+1));
		}
		if(action != null) {
			print.append(action.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
