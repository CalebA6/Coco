package types;

import java.util.ArrayList;
import java.util.List;

public class TypeChecker {
	
	private List<Type> errors = new ArrayList<>();
	
	public void reportError(Type error) {
		errors.add(error);
	}
	
}
