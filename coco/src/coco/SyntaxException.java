package coco;

public class SyntaxException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3494378355574592590L;
	
	private String error;
	
	public SyntaxException(String error, Token token) {
		this.error = "SyntaxError(" + token.lineNumber() + "," + token.charPosition() + ")[" + error + "]\n";
	}
	
	public SyntaxException(String error, int lineNumber, int charPosition) {
		this.error = "SyntaxError(" + lineNumber + "," + charPosition + ")[" + error + "]\n";
	}
	
	@Override
	public String toString() {
		return error;
	}
	
}
