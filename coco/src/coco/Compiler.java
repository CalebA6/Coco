package coco;

import java.util.ArrayList;
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
import code.Code;
import code.Op;
import ir.Block;
import ir.Graph;
import ir.Instruction;
import reg.LiveRange;
import reg.LiveRangeComparator;

public class Compiler {
	
	private Scanner scanner;
	private AST ast;
	private List<Graph> functions;
	private Map<Graph, Map<String, Integer>> regAllocs;

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
				if(optStrings.contains("cp") || optStrings.contains("max")) change = function.propagateAssignments(true) || change;
				if(optStrings.contains("cse") || optStrings.contains("max")) change = function.eliminateCommonSubexpressions() || change;
				if(optStrings.contains("cpp") || optStrings.contains("max")) change = function.propagateAssignments(false) || change;
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
		regAllocs = new HashMap<>();
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
					if(block == entry) {
						for(String liveVar: liveSet) {
							LiveRange varLv = new LiveRange(liveVar);
							varLv.addStart(0, block);
							nameToLv.put(liveVar, varLv);
							ranges.add(varLv);
						}
					} else {
						for(String liveVar: liveSet) {
							if(nameToLv.containsKey(liveVar)) {
								nameToLv.get(liveVar).addBlock(block);
							}
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
			
			Map<String, Integer> allocs = new HashMap<>();
			for(String var: nameToLv.keySet()) {
				allocs.put(var, nameToLv.get(var).getReg());
			}
			regAllocs.put(function, allocs);
			
			// Testing: Prints Allocation
			/* for(String var: nameToLv.keySet()) {
				System.out.println(var + ": " + nameToLv.get(var).getReg());
			}
			System.out.println(); */
		}
	}
	
	public int[] genCode() {
		if(regAllocs == null) regAlloc(26);
		
		Map<Graph, List<Code>> functionCodes = new HashMap<>();
		
		for(Graph function: functions) {
			Map<String, Integer> allocs = regAllocs.get(function);
			List<Code> code = new ArrayList<>();
			Map<Block, Set<Integer>> jumps = new HashMap<>();
			Map<Block, Integer> starts = new HashMap<>();
			
			// Block entry = function.getEntryBlock();
			// Queue<Block> blocks = new LinkedList<>();
			// Set<Block> visited = new HashSet<>();
			// blocks.add(entry);
			for(Block block: function) {
				/* Block block = blocks.remove();
				if(visited.contains(block)) {
					continue;
				} else {
					visited.add(block);
				} */
				
				starts.put(block, code.size());
				if(jumps.containsKey(block)) {
					for(int jump: jumps.get(block)) {
						code.get(jump).setJump(code.size() - jump);
					}
				}
				
				for(Instruction instr: block) {
					if(instr.isVoidCall() || instr.isCall()) {
						if(instr.isVoidCall()) {
							if(instr.assignee.startsWith("call printInt(")) {
								String [] parameters = instr.getParameters();
								if(parameters.length == 1) {
									int r;
									if(Instruction.isVar(parameters[0])) {
										if(allocs.containsKey(parameters[0])) {
											r = allocs.get(parameters[0]);
										} else {
											r = 0;
										}
									} else {
										code.add(new Code(Op.ADDI, 27, 0, Integer.parseInt(parameters[0])));
										r = 27;
									}
									code.add(new Code(Op.WRI, 0, r, 0));
								}
							} else if(instr.assignee.startsWith("call printBool(")) {
								String [] parameters = instr.getParameters();
								if(parameters.length == 1) {
									int r;
									if(Instruction.isVar(parameters[0])) {
										if(allocs.containsKey(parameters[0])) {
											r = allocs.get(parameters[0]);
										} else {
											r = 0;
										}
									} else {
										int bool = 0;
										if(parameters[0].equals("true")) bool = 1;
										code.add(new Code(Op.ADDI, 27, 0, bool));
										r = 27;
									}
									code.add(new Code(Op.WRI, 0, r, 0));
								}
							} else if(instr.assignee.equals("call println()")) {
								code.add(new Code(Op.WRL, 0, 0, 0));
							} else {
								throw new RuntimeException("TODO: implement function calling");
							}
						} else {
							if(instr.value1.equals("call readInt()")) {
								code.add(new Code(Op.RDI, allocs.get(instr.assignee), 0, 0));
							} else if(instr.value1.equals("call readBool()")) {
								code.add(new Code(Op.RDB, allocs.get(instr.assignee), 0, 0));
							} else {
								throw new RuntimeException("TODO: implement function calling");
							}
						}
					} else if(instr.isCopy()) {
						if(Instruction.isVar(instr.value1)) {
							int assignment;
							if(allocs.containsKey(instr.value1)) {
								assignment = allocs.get(instr.value1);
							} else {
								assignment = 0;
							}
							code.add(new Code(Op.ADDI, allocs.get(instr.assignee), assignment, 0));
						} else {
							int value;
							try {
								value = Integer.parseInt(instr.value1);
							} catch(NumberFormatException e) {
								if(instr.value1.equals("true")) value = 1;
								else value = 0;
							}
							code.add(new Code(Op.ADDI, allocs.get(instr.assignee), 0, value));
						}
					} else if(instr.isNot()) {
						code.add(new Code(Op.ADDI, 27, 0, 1));
						Op op;
						int bool;
						if(Instruction.isVar(instr.value1)) {
							if(allocs.containsKey(instr.value1)) { 
								op = Op.BIC;
								bool = allocs.get(instr.value1);
							} else {
								op = Op.BICI;
								bool = 0;
							}
						} else {
							op = Op.BICI;
							if(instr.value1.equals("true")) bool = 1;
							else bool = 0;
						}
						code.add(new Code(op, allocs.get(instr.assignee), 27, bool));
					} else if(instr.isOp()) {
						if(instr.isComparison()) {
							int assignee = allocs.get(instr.assignee);
							Op op;
							int first;
							int second;
							if(Instruction.isVar(instr.value1)) {
								if(allocs.containsKey(instr.value1)) {
									first = allocs.get(instr.value1);
								} else {
									first = 0;
								}
								if(Instruction.isVar(instr.value2)) {
									if(allocs.containsKey(instr.value2)) {
										op = Op.CMP;
										second = allocs.get(instr.value2);
									} else {
										op = Op.CMPI;
										second = 0;
									}
								} else {
									op = Op.CMPI;
									second = Integer.parseInt(instr.value2);
								}
							} else {
								code.add(new Code(Op.ADDI, 27, 0, Integer.parseInt(instr.value1)));
								first = 27;
								if(Instruction.isVar(instr.value2)) {
									if(allocs.containsKey(instr.value2)) {
										op = Op.CMP;
										second = allocs.get(instr.value2);
									} else {
										op = Op.CMPI;
										second = 0;
									}
								} else {
									op = Op.CMPI;
									second = Integer.parseInt(instr.value2);
								}
							}
							code.add(new Code(op, 27, first, second));
							code.add(new Code(Op.ADDI, assignee, 0, 1));
							switch(instr.op) {
								case EQUAL: 
									op = Op.BEQ;
									break;
								case NOT_EQUAL: 
									op = Op.BNE;
									break;
								case LESS_EQUAL: 
									op = Op.BLE;
									break;
								case GREATER_EQUAL: 
									op = Op.BGE;
									break;
								case LESS: 
									op = Op.BLT;
									break;
								case GREATER: 
									op = Op.BGT;
									break;
								default: 
									throw new RuntimeException("something had gone wrong");
							}
							code.add(new Code(op, 27, 0, 2));
							code.add(new Code(Op.ADDI, assignee, 0, 0));
						} else {
							int assignee = allocs.get(instr.assignee);
							Op op;
							int first;
							int second;
							if(Instruction.isVar(instr.value1)) {
								if(allocs.containsKey(instr.value1)) {
									first = allocs.get(instr.value1);
								} else {
									first = 0;
								}
								if(Instruction.isVar(instr.value2)) {
									if(allocs.containsKey(instr.value2)) {
										op = Op.fromInstructOp(instr.op, false);
										second = allocs.get(instr.value2);
									} else {
										op = Op.fromInstructOp(instr.op, true);
										second = 0;
									}
								} else {
									op = Op.fromInstructOp(instr.op, true);
									try {
										second = Integer.parseInt(instr.value2);
									} catch(NumberFormatException e) {
										if(instr.value2.equals("true")) second = 1;
										else second = 0;
									}
								}
							} else {
								try {
									first = Integer.parseInt(instr.value1);
								} catch(NumberFormatException e) {
									if(instr.value1.equals("true")) first = 1;
									else first = 0;
								}
								code.add(new Code(Op.ADDI, 27, 0, first));
								first = 27;
								if(Instruction.isVar(instr.value2)) {
									if(allocs.containsKey(instr.value2)) {
										op = Op.fromInstructOp(instr.op, false);
										second = allocs.get(instr.value2);
									} else {
										op = Op.fromInstructOp(instr.op, true);
										second = 0;
									}
								} else {
									op = Op.fromInstructOp(instr.op, true);
									try {
										second = Integer.parseInt(instr.value2);
									} catch(NumberFormatException e) {
										if(instr.value2.equals("true")) second = 1;
										else second = 0;
									}
								}
							}
							code.add(new Code(op, assignee, first, second));
						}
					} else if(instr.isJump() && !instr.isReturn()) {
						Op op;
						int decision = 0;
						if(instr.isConditionalJump()) {
							op = Op.BNE;
							if(Instruction.isVar(instr.value1)) {
								if(allocs.containsKey(instr.value1)) {
									decision = allocs.get(instr.value1);
								} else {
									decision = 0;
								}
							} else {
								int bool = 0;
								if(instr.value2.equals("true")) bool = 1;
								code.add(new Code(Op.ADDI, 27, 0, bool));
							}
						} else {
							op = Op.BSR;
						}
						
						Block jumpBlock = instr.getJump().getBlock();
						int jumpVector = 0;
						if(starts.containsKey(jumpBlock)) {
							jumpVector = starts.get(jumpBlock) - code.size();
						} else {
							if(!jumps.containsKey(jumpBlock)) {
								jumps.put(jumpBlock, new HashSet<>());
							}
							jumps.get(jumpBlock).add(code.size());
						}
						
						code.add(new Code(op, decision, 0, jumpVector));
					} else if(instr.isExit()) {
						code.add(new Code(Op.RET, 0, 0, 0));
					}
				}

				// blocks.addAll(block.getSuccessors());
			}
			
			functionCodes.put(function, code);
			
			// Troubleshooting
			/* System.out.println("### " + function.getName());
			for(Code inst: code) {
				System.out.println(inst);
			} */
		}
		
		int length = 0;
		for(List<Code> code: functionCodes.values()) {
			length += code.size();
		}
		
		int[] code = new int[length];
		int next = 0;
		for(Graph function: functionCodes.keySet()) {
			if(function.getName().equals("main")) {
				for(Code inst: functionCodes.get(function)) {
					code[next++] = inst.gen();
				}
			}
		}
		for(Graph function: functionCodes.keySet()) {
			if(!function.getName().equals("main")) {
				for(Code inst: functionCodes.get(function)) {
					code[next++] = inst.gen();
				}
			}
		}
		
		return code;
	}
	
	public boolean hasError() {
		return ast.hasError();
	}
	
	public String errorReport() {
		return ast.errorReport();
	}
	
}
