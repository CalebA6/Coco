package types;

public class IntType extends NumberType {
	
	@Override
	public String toString() {
		return "int";
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof IntType) {
			Type otherType = (Type) other;
			return otherType.numDimensions() == numDimensions();
		} else {
			return false;
		}
	}

	public static boolean is(Type type) {
		return type instanceof IntType;
	}
	
}
