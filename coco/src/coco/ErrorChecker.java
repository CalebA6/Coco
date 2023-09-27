package coco;

import coco.Token.Kind;

public class ErrorChecker {
	
	public static void checkForMoreInput(ReversibleScanner scanner, String expected) throws SyntaxException {
		if(!scanner.hasNext()) {
			throw new SyntaxException("PROGRAM ENDED UNEXPECTEDLY; EXPECTED " + expected, scanner.last());
		}
	}
	
	public static Token mustBe(Kind kind, String expected, ReversibleScanner source) throws SyntaxException {
		ErrorChecker.checkForMoreInput(source, expected);
		Token token = source.next();
		if(token.kind() != kind) {
			source.push(token);
			throw new SyntaxException("Expected " + expected + " but got " + token.kind() + ".", token);
		} else {
			return token;
		}
	}
	
}
