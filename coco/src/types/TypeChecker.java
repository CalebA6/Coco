package types;

import java.util.ArrayList;
import java.util.List;

import ast.AST;

public class TypeChecker {
	
	private List<ErrorType> errors = new ArrayList<>();
	
	public boolean check(AST ast) {
		ast.checkType(this, new VoidType());
		return errors.isEmpty();
	}
	
	public void reportError(ErrorType error) {
		errors.add(error);
	}
	
	public String errorReport() {
		StringBuilder report = new StringBuilder();
		for(ErrorType error: errors) {
			report.append(error.getMessage());
			report.append("\n");
		}
		return report.toString();
	}
	
}
