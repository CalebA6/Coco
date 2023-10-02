package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NoSuchStatementException;
import coco.RedefinitionException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import types.Type;
import types.TypeChecker;
import coco.Token.Kind;

public class VariableDeclaration extends Node {
	
	private Token type;
	private List<Token> arrayDimensions = new ArrayList<>();
	private List<Token> identifiers = new ArrayList<>();
	private Variables variables;

	public VariableDeclaration(ReversibleScanner source, Variables variables) throws SyntaxException, NoSuchStatementException, RedefinitionException {
		this.variables = variables;
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

			Token identifier = ErrorChecker.mustBe(Kind.IDENT, "IDENT", source);
			addIdentifier(identifier);
			
			while(true) {
				ErrorChecker.checkForMoreInput(source, "SEMICOLON OR COMMA");
				Token token = source.next();
				if(token.kind() == Kind.COMMA) {
					identifier = ErrorChecker.mustBe(Kind.IDENT, "IDENT", source);
					addIdentifier(identifier);
				} else if(token.kind() == Kind.SEMICOLON) {
					break;
				} else {
					throw new SyntaxException("Expected COMMA but got " + token.kind() + ".", token);
				}
			}
		} else {
			source.push(type);
			throw new NoSuchStatementException();
		}
	}
	
	public int lineNumber() {
		return type.lineNumber();
	}
	
	public int charPosition() {
		return type.charPosition();
	}
	
	public Type getType() {
		return Type.fromToken(type);
	}
	
	public void checkType(TypeChecker reporter, Type returnType) { }
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		for(Token identifier: identifiers) {
			addLevel(level, print);
			print.append("VariableDeclaration[");
			print.append(identifier.lexeme());
			print.append(":");
			print.append(getTypeString());
			print.append("]\n");
		}
		return print.toString();
	}
	
	private void addIdentifier(Token identifier) throws RedefinitionException {
		variables.add(identifier, getTypeString());
		identifiers.add(identifier);
	}
	
	private String getTypeString() {
		String str = type.lexeme();
		for(Token size: arrayDimensions) {
			str += "[" + size.lexeme() + "]";
		}
		return str;
	}
	
}
