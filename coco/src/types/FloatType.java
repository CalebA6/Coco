package types;

public class FloatType extends NumberType {
	
	@Override
	public String toString() {
		return "float";
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof FloatType) {
			Type otherType = (Type) other;
			return otherType.numDimensions() == numDimensions();
		} else {
			return false;
		}
	}

	public static boolean is(Type type) {
		return type instanceof FloatType;
	}
	
}
