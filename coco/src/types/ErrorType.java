package types;

import coco.Location;

public class ErrorType extends Type {

	private String message;
	private String plainMessage;
	
	public void setError(Location location, String message) {
		plainMessage = message;
		this.message = "TypeError(" + location.lineNumber() + "," + location.charPosition() + ")[" + message + "]";
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "ErrorType(" + plainMessage + ")";
	}
	
	public static boolean is(Type type) {
		return type instanceof ErrorType;
	}

}
