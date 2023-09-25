package coco;

import java.util.ArrayList;
import java.util.List;

import coco.Token.Kind;

public class VariableDeclaration {
	
	private Token type;
	private List<Token> arrayDimensions = new ArrayList<>();

	public VariableDeclaration(ReversibleScanner source) {
		type = source.next();
		Token.Kind typeType = type.kind();
		
		if((typeType == Token.Kind.BOOL) || (typeType == Token.Kind.INT) || (typeType == Token.Kind.FLOAT)) {
			ErrorChecker.checkForMoreInput(source, "VARIABLE NAME");
			
			Token array = source.next();
			while(array.kind() == Kind.OPEN_BRACKET) {
				ErrorChecker.checkForMoreInput(source, "SIZE OF ARRAY");
				Token size = ErrorChecker.mustBe(Kind.INT_VAL, "INT_VAL", source);
				arrayDimensions.add(size);
				ErrorChecker.mustBe(Kind.CLOSE_BRACKET, "CLOSE_BRACKET", source);
				array = source.next();
			}
			source.push(array);
			
			declareIdentifier(type);
			
			while(true) {
				ErrorChecker.checkForMoreInput(source, "SEMICOLON OR COMMA");
				Token token = source.next();
				if(token.kind() == Kind.COMMA) {
					declareIdentifier(type);
				} else if(token.kind() == Kind.SEMICOLON) {
					break;
				} else {
					System.err.println("UNEXPECTED TOKEN; EXPECTED SEMICOLON OR COMMA");
					System.exit(-1);
				}
			}
		} else {
			source.push(type);
			throw new NoSuchStatementException();
		}
	}
	
}
