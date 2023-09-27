package coco;

import ast.AST;

public class Compiler {
	
	private Scanner scanner;
	private AST ast;

	public Compiler(Scanner scanner, int numRegs) {
		this.scanner = scanner;
	}
	
	public AST genAST() {
		ast = new AST(scanner);
		return ast;
	}
	
	public boolean hasError() {
		return ast.hasError();
	}
	
	public String errorReport() {
		return ast.errorReport();
	}
	
}
