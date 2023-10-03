package types;

import coco.Location;
import coco.Token;

public class Type {
	// ERROR, VOID, BOOL, INT, FLOAT;
	private int dimensions = 0;
	
	public int numDimensions() {
		return dimensions;
	}
	
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}
	
	public void decrementDimensions() throws ArrayAccessException {
		if(dimensions == 0) {
			throw new ArrayAccessException();
		}
		--dimensions;
	}
	
	public static Type fromString(String str, Location location) {
		String[] split = str.split("\\[");
		String basicType = split[0];
		Type type;
		if(basicType.equals("bool")) {
			type = new BoolType();
		} else if(basicType.equals("int")) {
			type = new IntType();
		} else if(basicType.equals("float")) {
			type = new FloatType();
		} else if(basicType.equals("void")) {
			type = new VoidType();
		} else {
			type = new ErrorType();
			((ErrorType) type).setError(location, str + " is not a type");
		}
		type.dimensions = split.length - 1;
		if(type instanceof VoidType && type.dimensions > 0) {
			type = new ErrorType();
			((ErrorType) type).setError(location, "Array access is not allowed on VOID type");
		}
		return type;
	}
	
	public static Type fromToken(Token token) {
		switch(token.kind()) {
			case VOID: 
				return new VoidType();
			case BOOL: 
			case TRUE: 
			case FALSE: 
				return new BoolType();
			case INT_VAL: 
			case INT: 
				return new IntType();
			case FLOAT_VAL: 
			case FLOAT: 
				return new FloatType();
			default: 
				Type error = new ErrorType();
				((ErrorType) error).setError(token, "Not a type");
				return error;
		}
	}
}
