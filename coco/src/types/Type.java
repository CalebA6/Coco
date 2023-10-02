package types;

import coco.Token;

public enum Type {
	ERROR, VOID, BOOL, INT, FLOAT;
	private int dimensions = 0;
	private String message;
	
	public int numDimensions() {
		return dimensions;
	}
	
	public void decrementDimensions() throws ArrayAccessException {
		if(dimensions == 0) {
			throw new ArrayAccessException();
		}
		--dimensions;
	}
	
	public void setError(Token location, String message) {
		this.message = "TypeError(" + location.lineNumber() + "," + location.charPosition() + ")[" + message + "]";
	}
	
	public static Type fromString(String str, Token location) {
		String[] split = str.split("[");
		String basicType = split[0];
		Type type;
		if(basicType == "bool") {
			type = Type.BOOL;
		} else if(basicType == "int") {
			type = Type.INT;
		} else if(basicType == "float") {
			type = Type.FLOAT;
		} else if(basicType == "void") {
			type = Type.VOID;
		} else {
			type = Type.ERROR;
			type.setError(location, str + " is not a type");
		}
		type.dimensions = split.length - 1;
		if(type == Type.VOID && type.dimensions > 0) {
			type = Type.ERROR;
			type.setError(location, "Array access is not allowed on VOID type");
		}
		return type;
	}
	
	public static Type fromToken(Token token) {
		switch(token.kind()) {
			case VOID: 
				return Type.VOID;
			case BOOL: 
			case TRUE: 
			case FALSE: 
				return Type.BOOL;
			case INT_VAL: 
			case INT: 
				return Type.INT;
			case FLOAT_VAL: 
			case FLOAT: 
				return Type.FLOAT;
			default: 
				Type error = Type.ERROR;
				error.setError(token, "Not a type");
				return error;
		}
	}
}
