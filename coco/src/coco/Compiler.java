package coco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.cli.CommandLine;

import ast.AST;
import ir.Block;
import ir.Graph;
import reg.LiveRange;
import reg.LiveRangeComparator;

public class Compiler {
	
	private Scanner scanner;
	private AST ast;
	private List<Graph> functions;

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
		if(functions == null) functions = ast.genIr();
		
		for(Graph function: functions) {
			boolean change = true;
			while(change) {
				change = false;
				if(optStrings.contains("dce") || optStrings.contains("max")) change = function.eliminateDeadCode() || change;
				if(optStrings.contains("cf") || optStrings.contains("max")) change = function.foldConstants() || change;
				if(optStrings.contains("cp") || optStrings.contains("max") || optStrings.contains("cpp")) change = function.propagateConstants() || change;
				if(optStrings.contains("cse") || optStrings.contains("max")) change = function.eliminateCommonSubexpressions() || change;
			}
		}
		
		StringBuilder dot = new StringBuilder();
		for(Graph function: functions) {
			dot.append(function.dotGraph());
		}
		return dot.toString();
	}
	
	public void regAlloc(int numReg) {
		if(functions == null) functions = ast.genIr();
		
		/* Design: 
		 * Function params put into registers starting with R0 in order
		 * array containing allocation stats of registers
		 * get all variables and their live ranges
		 * build graph (with array of ordered nodes)
		 * */
		for(Graph function: functions) {
			Map<String, LiveRange> nameToLv = new HashMap<>();
			List<LiveRange> ranges = new LinkedList<>();
			
			boolean change = true;
			while(change) {
				change = false;
				for(Block block: function.getBlocks()) {
					change = block.updateLiveVariables() || change;
				}
			}
			
			Block entry = function.getEntryBlock();
			Queue<Block> blocks = new LinkedList<>();
			Set<Block> visited = new HashSet<>();
			blocks.add(entry);
			while(!blocks.isEmpty()) {
				Block block = blocks.remove();
				if(visited.contains(block)) {
					continue;
				} else {
					visited.add(block);
				}
				
				List<Collection<String>> liveSets = block.genLiveSets();
				if(liveSets.size() > 0) {
					Collection<String> liveSet = liveSets.get(0);
					for(String liveVar: liveSet) {
						if(nameToLv.containsKey(liveVar)) {
							nameToLv.get(liveVar).addBlock(block);
						}
					}
				}
				for(int i=1; i<liveSets.size(); ++i) {
					Collection<String> thisSet = liveSets.get(i);
					Collection<String> lastSet = liveSets.get(i - 1);
					for(String var: thisSet) {
						if(!lastSet.contains(var)) {
							if(nameToLv.containsKey(var)) {
								nameToLv.get(var).addStart(i / 2, block);
							} else {
								LiveRange varLv = new LiveRange(var);
								varLv.addStart(i / 2, block);
								nameToLv.put(var, varLv);
								ranges.add(varLv);
							}
						}
					}
					for(String var: lastSet) {
						if(!thisSet.contains(var) && nameToLv.containsKey(var)) {
							nameToLv.get(var).addEnd(i / 2, block);
						}
					}
				}
				
				blocks.addAll(block.getSuccessors());
			}
			
			Stack<LiveRange> stack = new Stack<>();
			for(LiveRange lv: ranges) {
				lv.link(ranges);
				lv.setStack(stack);
			}
			
			while(!ranges.isEmpty()) {
				ranges.sort(new LiveRangeComparator());
				int removal = 0;
				while(removal < ranges.size()) {
					if(ranges.get(removal).numConflicts() < numReg) {
						break;
					} else {
						++removal;
					}
				}
				if(removal >= ranges.size()) {
					removal = 0;
				}
				stack.push(ranges.remove(removal));
			}
			
			while(!stack.isEmpty()) {
				LiveRange var = stack.pop();
				var.setReg(numReg);
			}
			
			// Testing: Prints Allocation
			for(String var: nameToLv.keySet()) {
				System.out.println(var + ": " + nameToLv.get(var).getReg());
			}
			System.out.println();
		}
	}
	
	public boolean hasError() {
		return ast.hasError();
	}
	
	public String errorReport() {
		return ast.errorReport();
	}
	
}
