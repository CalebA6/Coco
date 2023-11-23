package types;

public class VoidType extends Type {
	
	@Override
	public String toString() {
		return "void";
	}

	public static boolean is(Type type) {
		return type instanceof VoidType;
	}
	
}
