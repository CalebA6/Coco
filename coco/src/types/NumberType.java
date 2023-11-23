package types;

public class NumberType extends Type {
	
	public static boolean is(Type type) {
		return type instanceof NumberType;
	}
	
}
