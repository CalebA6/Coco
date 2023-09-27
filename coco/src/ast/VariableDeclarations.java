package ast;

import java.util.ArrayList;
import java.util.List;

import coco.NoSuchStatementException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Variables;

public class VariableDeclarations extends Traversible {

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
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("DeclarationList\n");
		for(VariableDeclaration declaration: varDeclarations) {
			print.append(declaration.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
