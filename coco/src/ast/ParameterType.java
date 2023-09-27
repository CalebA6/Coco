package ast;

import coco.ErrorChecker;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;

public class ParameterType {
	
	Token type;
	int dimensions = 0;

	public ParameterType(ReversibleScanner source) throws SyntaxException {
		ErrorChecker.checkForMoreInput(source, "paramType");
		type = source.next();
		if((type.kind() != Kind.BOOL) && (type.kind() != Kind.INT) && (type.kind() != Kind.FLOAT)) {
			throw new SyntaxException("Expected type but got " + type.kind() + ".", type);
		}
		while(source.hasNext()) {
			Token array = source.next();
			if(array.kind() == Kind.OPEN_BRACKET) {
				++dimensions;
				ErrorChecker.mustBe(Kind.CLOSE_BRACKET, "CLOSE_BRACKET", source);
			} else {
				source.push(array);
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		String str = type.lexeme();
		for(int dim=0; dim<dimensions; ++dim) {
			str += "[]";
		}
		return str;
	}
	
}
