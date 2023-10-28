package coco;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import ast.AST;
import ir.Graph;

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
	
	public String optimization(List<String> optStrings, CommandLine optsCmd) {
		List<Graph> functions = ast.genIr();
		
		for(Graph function: functions) {
			if(optStrings.contains("dce")) function.eliminateDeadCode();
			if(optStrings.contains("cf")) function.foldConstants();
		}
		
		StringBuilder dot = new StringBuilder();
		for(Graph function: functions) {
			dot.append(function.dotGraph());
		}
		return dot.toString();
	}
	
	public boolean hasError() {
		return ast.hasError();
	}
	
	public String errorReport() {
		return ast.errorReport();
	}
	
}
