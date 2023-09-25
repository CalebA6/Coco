package coco;

import coco.Token.Kind;

public class Program {

	public Program(Scanner scanner) {
		ReversibleScanner source = new ReversibleScanner(scanner);

		if(!source.hasNext()) {
			throw new SyntaxException("no program", 0, 0);
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
	
}
