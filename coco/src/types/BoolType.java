package types;

public class BoolType extends Type {
	
	@Override
	public String toString() {
		return "bool";
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof BoolType) {
			Type otherType = (Type) other;
			return otherType.numDimensions() == numDimensions();
		} else {
			return false;
		}
	}
	
	public static boolean is(Type type) {
		return type instanceof BoolType;
	}
	
}
