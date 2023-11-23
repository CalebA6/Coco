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
	
	public AST genSSA(AST ast) {
		return ast;
	}
	
	public String optimization(List<String> optStrings, CommandLine optsCmd) {
		List<Graph> functions = ast.genIr();
		
		for(Graph function: functions) {
			boolean change = true;
			while(change) {
				change = false;
				if(optStrings.contains("dce") || optStrings.contains("max")) change = function.eliminateDeadCode() || change;
				if(optStrings.contains("cf") || optStrings.contains("max")) change = function.foldConstants() || change;
				if(optStrings.contains("cp") || optStrings.contains("max")) change = function.propagateConstants() || change;
				if(optStrings.contains("cse") || optStrings.contains("max")) change = function.eliminateCommonSubexpressions() || change;
			}
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
