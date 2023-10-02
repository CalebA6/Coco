package coco;

public class RedefinitionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5410561820584908197L;
	
	private String error;
	
	public RedefinitionException(Token var) {
		error = "DeclareSymbolError(" + var.lineNumber() + "," + var.charPosition() + ")[" + var.lexeme() + " already exists.]\n";
	}
	
	@Override
	public String toString() {
		return error;
	}

}
