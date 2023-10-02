package ast;

import coco.ErrorChecker;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import coco.Token.Kind;

public class Parameter {
	
	private ParameterType type;
	private Token name;

	public Parameter(ReversibleScanner source, Variables variables) throws SyntaxException, RedefinitionException {
		type = new ParameterType(source);
		name = ErrorChecker.mustBe(Kind.IDENT, "IDENT", source);
		variables.add(name, type.toString());
	}
	
	public String getTypeString() {
		return type.toString();
	}
	
}
