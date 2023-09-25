package coco;

import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import coco.Token.Kind;

public class Interpreter {
	
	private ReversibleScanner source = null;
	private java.util.Scanner input;
	private Variables variables = new Variables();
	private Exception error = null;
	
	public Interpreter(Scanner scanner, InputStream input) {
		this.source = new ReversibleScanner(scanner);
		this.input = new java.util.Scanner(input);
	}
	
	public void interpret() {
		if(!source.hasNext()) {
			error = new Exception("NO PROGRAM");
			return;
		}
		
		Token first = source.next();
		if(first.kind() == Token.Kind.MAIN) {
			while(true) {
				try {
					declareVariable();
				} catch(NoSuchStatementException e) {
					break;
				} catch (SyntaxException e) {
					error = e;
					return;
				}
			}
			
			try {
				ErrorChecker.checkForMoreInput(source, "OPENNING BRACE");
			} catch (SyntaxException e1) {
				error = e1;
				return;
			}
			Token openningBracket = source.next();
			if(openningBracket.kind() != Kind.OPEN_BRACE) {
				error = new SyntaxException("UNEXPECTED TOKEN; EXPECTED OPEN BRACE", openningBracket);
				return;
			}
			
			try {
				runStatements();
			} catch (SyntaxException e) {
				error = e;
				return;
			}
			
			while(true) {
				try {
					ErrorChecker.checkForMoreInput(source, "END OF FUNCTION");
				} catch (SyntaxException e) {
					error = e;
					return;
				}
				Token token = source.next();
				if(token.kind() == Kind.CLOSE_BRACE) break;
			}
			
			try {
				mustBe(Kind.PERIOD, "PERIOD");
			} catch (SyntaxException e) {
				error = e;
				return;
			}
		} else {
			error = new SyntaxException("MAIN FUNCTION NOT DECLARED", first);
		}
	}
	
	public boolean hasError() {
		return error != null;
	}
	
	public String errorReport() {
		return error.toString();
	}

	/* public static void main(String[] args) {
		if(args.length < 1) {
			System.err.println("NOT ENOUGH ARGUMENTS: MAKE SURE TO GIVE A PROGRAM FILE NAME");
			System.exit(-1);
		}
		
		try {
			source = new ReversibleScanner(new FileReader(args[0]));
			if(args.length >= 2) {
				input = new java.util.Scanner(new File(args[1]));
			} else {
				input = new java.util.Scanner(System.in);
			}
		} catch (FileNotFoundException e) {
			System.err.println("FAILED TO OPEN FILE");
			System.exit(-1);
		}
		
		if(source.hasNext() && (source.next().kind() == Token.Kind.MAIN)) {
			while(true) {
				try {
					declareVariable();
				} catch(NoSuchStatementException e) {
					break;
				}
			}
			
			ErrorChecker.checkForMoreInput(source, "OPENNING BRACE");
			Token openningBracket = source.next();
			if(openningBracket.kind() != Kind.OPEN_BRACE) {
				System.err.println("UNEXPECTED TOKEN; EXPECTED OPEN BRACE");
				System.exit(-1);
			}
			
			try {
				runStatements();
			} catch (FatalException e) {
				System.err.println(e);
				System.exit(-1);
			}
			
			while(true) {
				ErrorChecker.checkForMoreInput(source, "END OF FUNCTION");
				Token token = source.next();
				if(token.kind() == Kind.CLOSE_BRACE) break;
			}
			
			try {
				mustBe(Kind.PERIOD, "PERIOD");
			} catch (FatalException e) {
				System.err.println(e);
				System.exit(-1);
			}
		} else {
			System.err.println("MAIN FUNCTION NOT DECLARED");
		}
	} */
	
	private void declareVariable() throws NoSuchStatementException, SyntaxException {
		Token type = source.next();
		Token.Kind typeType = type.kind();
		
		if((typeType == Token.Kind.BOOL) || (typeType == Token.Kind.INT) || (typeType == Token.Kind.FLOAT)) {
			ErrorChecker.checkForMoreInput(source, "VARIABLE NAME");
			
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
	
	private void declareIdentifier(Token type) {
		Token identifier = source.next();
		if(identifier.kind() != Token.Kind.IDENT) {
			System.err.println("UNEXPECTED TOKEN; EXPECTED IDENTIFIER");
			System.exit(-1);
		}
		
		try {
			Variable variable = new Variable(type.kind());
			variables.add(identifier.lexeme(), variable);
		} catch(ValueException e) {
			System.err.println(type.lexeme() + " IS NOT A VALID TYPE");
			System.exit(-1);
		}
	}
	
	private Variable runStatements() throws SyntaxException {
		while(true) {
			ErrorChecker.checkForMoreInput(source, "STATEMENT");
			Token token = source.next();
			if(token.kind() == Kind.IDENT) {
				runAssignment(token);
			} else if(token.kind() == Kind.CALL) {
				runFunction();
			} else if(token.kind() == Kind.IF) {
				mustBe(Kind.OPEN_PAREN, "OPENNING PARENTHESI");
				
				Variable runVar = calculateExpression();
				boolean run = false;
				try {
					run = (boolean)runVar.getValue();
				} catch(ClassCastException e) {
					throw new SyntaxException("TYPE ERROR; EXPECTED BOOLEAN TYPE", token);
				}
				
				mustBe(Kind.CLOSE_PAREN, "CLOSING PARENTHSI");
				mustBe(Kind.THEN, "THEN BLOCK");
				if(run) {
					Variable returnValue = runStatements();
					if(returnValue != null) return returnValue;
					
					ErrorChecker.checkForMoreInput(source, "ELSE OR FI");
					Token next = source.next();
					if(next.kind() == Kind.ELSE) {
						int numberIfs = 1;
						
						while(true) {
							Token thenBodyToken = source.next();
							if(thenBodyToken.kind() == Kind.IF) {
								++numberIfs;
							} else if(thenBodyToken.kind() == Kind.FI) {
								--numberIfs;
							}
							
							if(numberIfs <= 0) {
								if(numberIfs < 0) {
									throw new SyntaxException("OVERFLOW; TOO DEEPLY NESTED IF STATEMENTS", thenBodyToken);
								}
								
								next = thenBodyToken;
								break;
							}
						}
					}
					
					if(next.kind() != Kind.FI) {
						throw new SyntaxException("Expected FI but got " + next.kind() + ".", next);
					}
				} else {
					int numberIfs = 1;
					
					while(true) {
						Token thenBodyToken = source.next();
						if(thenBodyToken.kind() == Kind.IF) {
							++numberIfs;
						} else if(thenBodyToken.kind() == Kind.FI) {
							--numberIfs;
						}
						
						if(numberIfs <= 1) {
							if(numberIfs < 0) {
								throw new SyntaxException("OVERFLOW; TOO DEEPLY NESTED IF STATEMENTS", thenBodyToken);
							} else if((numberIfs == 1) && (thenBodyToken.kind() == Kind.ELSE)) {
								Variable returnValue = runStatements();
								if(returnValue != null) return returnValue;
								mustBe(Kind.FI, "FI");
								break;
							} else if(numberIfs == 0) {
								break;
							}
						}
					}
				}
			} else if(token.kind() == Kind.RETURN) {
				Token next = source.next();
				source.push(next);
				try {
					return calculateExpression();
				} catch(SyntaxException e) {
					return new Variable();
				}
			} else if(token.kind() == Kind.CLOSE_BRACE) {
				source.push(token);
				return null;
			} else if((token.kind() == Kind.FI) || (token.kind() == Kind.ELSE)) {
				source.push(token);
				return null;
			} else {
				throw new SyntaxException("UNEXPECTED TOKEN; EXPECTED STATEMENT", token);
			}
			
			mustBe(Kind.SEMICOLON, "SEMICOLON");
		}
	}
	
	private void runAssignment(Token var) throws SyntaxException {
		Variable value;
		try {
			value = variables.get(var.lexeme());
		} catch (NonexistantVariableException e1) {
			throw new SyntaxException("MALFORMED PROGRAM; VARIABLE ASSIGNMENT BEFORE DECLARATION", var);
		}
		ErrorChecker.checkForMoreInput(source, "ASSIGNMENT OPERATOR");
		Token assignOp = source.next();
		if((assignOp.kind() == Kind.ASSIGN) || (assignOp.kind() == Kind.ADD_ASSIGN) || (assignOp.kind() == Kind.SUB_ASSIGN) || (assignOp.kind() == Kind.MUL_ASSIGN) || (assignOp.kind() == Kind.DIV_ASSIGN) || (assignOp.kind() == Kind.MOD_ASSIGN) || (assignOp.kind() == Kind.POW_ASSIGN)) {
			Variable newValue = calculateExpression();
			try {
				switch(assignOp.kind()) {
					case ASSIGN: 
						value.setValue(newValue);
						break;
					case ADD_ASSIGN: 
						value.setValue(value.add(newValue));
						break;
					case SUB_ASSIGN: 
						value.setValue(value.subtract(newValue));
						break;
					case DIV_ASSIGN: 
						value.setValue(value.divide(newValue));
						break;
					case MUL_ASSIGN: 
						value.setValue(value.multiply(newValue));
						break;
					case MOD_ASSIGN: 
						value.setValue(value.mod(newValue));
						break;
					case POW_ASSIGN: 
						value.setValue(value.pow(newValue));
						break;
					default: 
						throw new SyntaxException("Unknown assignment operator.", assignOp);
				}
			} catch(IncompatibleTypeException|InvalidOperationException e) {
				throw new SyntaxException("TYPE ERROR got " + newValue.getTypeName() + " expected " + value.getTypeName(), assignOp);
			}
		} else if((assignOp.kind() == Kind.UNI_INC) || (assignOp.kind() == Kind.UNI_DEC)) {
			try {
				switch(assignOp.kind()) {
					case UNI_INC: 
						value.increment();
						break;
					case UNI_DEC: 
						value.decrement();
						break;
					default: 
						throw new SyntaxException("Unknown unary operator.", assignOp);
				}
			} catch(InvalidOperationException e) {
				throw new SyntaxException("Unary operator failed.", assignOp);
			}
		} else {
			throw new SyntaxException("Expected assignment operator but got " + assignOp.kind() + " .", assignOp);
		}
	}
	
	private Variable calculateExpression() throws SyntaxException {
		Variable value = calculateSum();
		while(source.hasNext()) {
			Token operation = source.next();
			if((operation.kind() == Kind.EQUAL_TO) || (operation.kind() == Kind.NOT_EQUAL) || (operation.kind() == Kind.LESS_EQUAL) || (operation.kind() == Kind.GREATER_EQUAL) || (operation.kind() == Kind.LESS_THAN) || (operation.kind() == Kind.GREATER_THAN)) {
				Variable other = calculateSum();
				
				try {
					if(operation.kind() == Kind.EQUAL_TO) value = value.equal(other);
					else if(operation.kind() == Kind.NOT_EQUAL) value = value.notEqual(other);
					else if(operation.kind() == Kind.LESS_EQUAL) value = value.lessThanOrEqual(other);
					else if(operation.kind() == Kind.GREATER_EQUAL) value = value.greaterThanOrEqual(other);
					else if(operation.kind() == Kind.LESS_THAN) value = value.lessThan(other);
					else if(operation.kind() == Kind.GREATER_THAN) value = value.greaterThan(other);
				} catch (InvalidOperationException e) {
					throw new SyntaxException("TYPE ERROR; EXPECTED INTEGER OR FLOAT TYPE", operation);
				}
			} else {
				source.push(operation);
				break;
			}
		}
		return value;
	}
	
	private Variable calculateSum() throws SyntaxException {
		Variable sum = calculateProduct();
		while(source.hasNext()) {
			Token operation = source.next();
			if((operation.kind() == Kind.ADD) || (operation.kind() == Kind.SUB) || (operation.kind() == Kind.OR)) {
				Variable addend = calculateProduct();
				
				try {
					if(operation.kind() == Kind.ADD) sum = sum.add(addend);
					else if(operation.kind() == Kind.SUB) sum = sum.subtract(addend);
				} catch (InvalidOperationException e) {
					throw new SyntaxException("TYPE ERROR; EXPECTED INTEGER OR FLOAT TYPE", operation);
				}
				
				if(operation.kind() == Kind.OR) {
					try {
						sum = sum.or(addend);
					} catch (InvalidOperationException e) {
						throw new SyntaxException("TYPE ERROR; EXPECTED BOOLEAN TYPE", operation);
					}
				}
			} else {
				source.push(operation);
				break;
			}
		}
		return sum;
	}
	
	private Variable calculateProduct() throws SyntaxException {
		Variable product = calculatePower();
		while(source.hasNext()) {
			Token operation = source.next();
			if((operation.kind() == Kind.MUL) || (operation.kind() == Kind.DIV) || (operation.kind() == Kind.MOD) || (operation.kind() == Kind.AND)) {
				Variable multiplier = calculatePower();
				
				try {
					if(operation.kind() == Kind.MUL) product = product.multiply(multiplier);
					else if(operation.kind() == Kind.DIV) product = product.divide(multiplier);
					else if(operation.kind() == Kind.MOD) product = product.mod(multiplier);
				} catch (InvalidOperationException e) {
					throw new SyntaxException("TYPE ERROR; EXPECTED INTEGER OR FLOAT TYPE", operation);
				}
				
				if(operation.kind() == Kind.AND) {
					try {
						product = product.and(multiplier);
					} catch (InvalidOperationException e) {
						throw new SyntaxException("TYPE ERROR; EXPECTED BOOLEAN TYPE", operation);
					}
				}
			} else {
				source.push(operation);
				break;
			}
		}
		return product;
	}
	
	private Variable calculatePower() throws SyntaxException {
		Variable base = calculateGroup();
		while(source.hasNext()) {
			Token operation = source.next();
			if(operation.kind() == Kind.POW) {
				Variable exponent = calculateGroup();
				try {
					base = base.pow(exponent);
				} catch (InvalidOperationException e) {
					throw new SyntaxException("TYPE ERROR; EXPECTED INTEGER OR FLOAT TYPE", operation);
				}
			} else {
				source.push(operation);
				break;
			}
		}
		return base;
	}
	
	private Variable calculateGroup() throws SyntaxException {
		ErrorChecker.checkForMoreInput(source, "A VALUE");
		Token token = source.next();
		if((token.kind() == Kind.INT_VAL) || (token.kind() == Kind.FLOAT_VAL) || (token.kind() == Kind.TRUE) || (token.kind() == Kind.FALSE)) {
			try {
				if(token.kind() == Kind.INT_VAL) {
						return new Variable(Kind.INT, Integer.parseInt(token.lexeme()));
				} else if(token.kind() == Kind.FLOAT_VAL) {
					return new Variable(Kind.FLOAT, Float.parseFloat(token.lexeme()));
				} else if(token.kind() == Kind.TRUE) {
					return new Variable(Kind.BOOL, true);
				} else {
					return new Variable(Kind.BOOL, false);
				}
			} catch (NumberFormatException e) {
				throw new SyntaxException("TOKENIZER ERROR; INCORRECTLY IDENTIFIED \"" + token.lexeme() + "\" AS LITERAL", token);
			} catch (ValueException e) {
				throw new SyntaxException("INTERPETER ERROR", token);
			}
		} else if(token.kind() == Kind.IDENT) {
			try {
				return variables.get(token.lexeme());
			} catch (NonexistantVariableException e) {
				throw new SyntaxException("UNKNOWN VARIABLE", token);
			}
		} else if(token.kind() == Kind.NOT) {
			try {
				return calculateExpression().not();
			} catch (InvalidOperationException e) {
				throw new SyntaxException("TYPE ERROR; EXPECTED BOOLEAN TYPE", token);
			}
		} else if(token.kind() == Kind.OPEN_PAREN) {
			Variable value = calculateExpression();
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			return value;
		} else if(token.kind() == Kind.CALL) {
			Variable value = runFunction();
			if(value == null) {
				throw new SyntaxException("TYPE ERROR; DID NOT EXPECT VOID TYPE", token);
			}
			return value;
		} else {
			throw new SyntaxException("UNEXPECTED TOKEN; EXPECTED A VALUE", token);
		}
	}
	
	private Variable runFunction() throws SyntaxException {
		ErrorChecker.checkForMoreInput(source, "FUNCTION NAME");
		Token func = source.next();
		mustBe(Kind.OPEN_PAREN, "OPENNING PARENTHESI");
		
		if(func.lexeme().equals("readInt")) {
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			System.out.print("int? ");
			try {
				return new Variable(Kind.INT, input.nextInt());
			} catch (ValueException|InputMismatchException e) {
				throw new SyntaxException("INPUT IS NOT INTEGER", func);
			} catch(NoSuchElementException|IllegalStateException e) {
				throw new SyntaxException("UNABLE TO READ INPUT", func);
			}
		} else if(func.lexeme().equals("readFloat")) {
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			System.out.print("float? ");
			try {
				return new Variable(Kind.FLOAT, input.nextFloat());
			} catch (ValueException|InputMismatchException e) {
				throw new SyntaxException("INPUT IS NOT FLOAT", func);
			} catch(NoSuchElementException|IllegalStateException e) {
				throw new SyntaxException("UNABLE TO READ INPUT", func);
			}
		} else if(func.lexeme().equals("readBool")) {
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			System.out.print("true or false? ");
			try {
				return new Variable(Kind.BOOL, input.nextBoolean());
			} catch (ValueException|InputMismatchException e) {
				throw new SyntaxException("INPUT IS NOT BOOLEAN", func);
			} catch(NoSuchElementException|IllegalStateException e) {
				throw new SyntaxException("UNABLE TO READ INPUT", func);
			}
		} else if(func.lexeme().equals("printInt")) {
			Variable intToPrint = calculateExpression();
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			if(intToPrint.isType(Integer.class)) {
				System.out.print(intToPrint + " ");
			} else {
				throw new SyntaxException("INVALID TYPE; EXPECTED INTEGER TYPE", func);
			}
		} else if(func.lexeme().equals("printFloat")) {
			Variable floatToPrint = calculateExpression();
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			if(floatToPrint.isType(Float.class)) {
				System.out.printf("%.2f ", floatToPrint.getValue());
			} else {
				throw new SyntaxException("INVALID TYPE; EXPECTED FLOAT TYPE", func);
			}
		} else if(func.lexeme().equals("printBool")) {
			Variable boolToPrint = calculateExpression();
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			if(boolToPrint.isType(Boolean.class)) {
				System.out.print(boolToPrint + " ");
			} else {
				throw new SyntaxException("INVALID TYPE; EXPECTED BOOLEAN TYPE", func);
			}
		} else if(func.lexeme().equals("println")) {
			mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN");
			System.out.println();
		} else {
			throw new SyntaxException("UNKNOWN FUNCTION \"" + func.lexeme() + "\"", func);
		}
		
		return null;
	}
	
	private Token mustBe(Kind kind, String expected) throws SyntaxException {
		ErrorChecker.checkForMoreInput(source, expected);
		Token token = source.next();
		if(token.kind() != kind) {
			throw new SyntaxException("Expected " + expected + " but got " + token.kind() + ".", token);
		} else {
			return token;
		}
	}

}
