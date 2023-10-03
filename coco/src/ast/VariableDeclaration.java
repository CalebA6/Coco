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
import types.ErrorType;
import types.Type;
import types.TypeChecker;
import coco.Token.Kind;

public class VariableDeclaration extends Node {
	
	private Token type;
	private List<Token> arrayDimensions = new ArrayList<>();
	private List<Token> identifiers = new ArrayList<>();
	private Variables variables;
	
	private Token end;

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
					end = token;
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
		return end.lineNumber();
	}
	
	public int charPosition() {
		return end.charPosition();
	}
	
	public Type getType() {
		Type type = Type.fromToken(this.type);
		type.setDimensions(arrayDimensions.size());
		return type;
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		for(Token dimension: arrayDimensions) {
			if((dimension.kind() != Kind.INT_VAL)) {
				ErrorType error = new ErrorType();
				error.setError(dimension, "Cannot set array size to " + Type.fromToken(dimension));
			}
			String number = dimension.lexeme();
			boolean negative = false;
			if(number.charAt(0) == '-') negative = true;
			boolean zero = true;
			for(int c=0; c<number.length(); ++c) {
				if(number.charAt(c) != '0' && number.charAt(c) != '.') {
					zero = false;
					break;
				}
			}
			/* if(negative || zero) {
				ErrorType error = new ErrorType();
				StringBuilder vars = new StringBuilder();
				for(Token name: identifiers) {
					if(vars.length() > 0) {
						vars.append(",");
					}
					vars.append(name.lexeme());
				}
				error.setError(this, "Array " + vars.toString() + " has invalid size " + dimension.lexeme());
				reporter.reportError(error);
			} */
		}
	}
	
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
