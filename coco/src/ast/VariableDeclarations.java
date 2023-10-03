package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NoSuchStatementException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Variables;
import types.Type;
import types.TypeChecker;
import types.VoidType;

public class VariableDeclarations extends Node {

	private List<VariableDeclaration> varDeclarations = new ArrayList<>();
	
	public VariableDeclarations(ReversibleScanner source, Variables varTable) throws SyntaxException, RedefinitionException {
		while(true) {
			try {
				varDeclarations.add(new VariableDeclaration(source, varTable));
			} catch(NoSuchStatementException e) {
				break;
			}
		}
	}
	
	public int lineNumber() {
		return varDeclarations.get(0).lineNumber();
	}
	
	public int charPosition() {
		return varDeclarations.get(0).charPosition();
	}
	
	public VariableDeclarations getAST() {
		if(varDeclarations.isEmpty()) {
			return null;
		}
		return this;
	}
	
	public Type getType() {
		return new VoidType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType) {
		for(VariableDeclaration declaration: varDeclarations) {
			declaration.checkType(reporter, returnType);
		}
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		if(varDeclarations.size() > 0) {
			addLevel(level, print);
			print.append("DeclarationList\n");
		}
		for(VariableDeclaration declaration: varDeclarations) {
			print.append(declaration.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
