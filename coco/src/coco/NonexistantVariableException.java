package coco;

public class NonexistantVariableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2582710168106195L;
	
	private String error;
	
	public NonexistantVariableException(Token var) {
		error = "ResolveSymbolError(" + var.lineNumber() + "," + var.charPosition() + ")[Could not find " + var.lexeme() + ".]\n";
	}
	
	@Override
	public String toString() {
		return error;
	}

}
