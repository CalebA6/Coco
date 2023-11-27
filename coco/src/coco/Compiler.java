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
	private int numReg;
	static final int TEMP_REG = 27;
	static final int FRAME_REG = 28;
	static final int STACK_REG = 29;
	static final int GLOBAL_REG = 30;
	static final int RETURN_REG = 31;
	static final int WORD_SIZE = 4;

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
		this.numReg = numReg;
		
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
		
		Set<String> globalVariables = ast.getGlobalVars();
		Map<String, Integer> globalOffsets = new HashMap<>();
		int offset = 0;
		for(String var: globalVariables) {
			globalOffsets.put(var, offset -= WORD_SIZE);
		}
		
		Map<Graph, List<Code>> functionCodes = new HashMap<>();
		// TODO: need to handle overloaded functions
		Map<String, Collection<Code>> functionCalls = new HashMap<>();
		
		for(Graph function: functions) {
			Map<String, Integer> allocs = regAllocs.get(function);
			List<Code> code = new ArrayList<>();
			Map<Block, Set<Integer>> jumps = new HashMap<>();
			Map<Block, Integer> starts = new HashMap<>();
			
			if(function.getName().equals("main")) {
				code.add(new Code(Op.ADDI, FRAME_REG, GLOBAL_REG, offset));
			}
			
			Map<String, Integer> frameOffsets = new HashMap<>();
			String[] funcParams = function.getParameters();
			Set<String> funcParamsSet = new HashSet<>(Arrays.asList(funcParams));
			offset = numReg * -WORD_SIZE;
			for(String var: allocs.keySet()) {
				if(!globalVariables.contains(var) || funcParamsSet.contains(var)) {
					if(allocs.get(var) != 0) {
						frameOffsets.put(var, allocs.get(var) * -WORD_SIZE);
					} else {
						frameOffsets.put(var, offset -= WORD_SIZE);
					}
				}
			}
			
			code.add(new Code(Op.ADDI, STACK_REG, FRAME_REG, offset));
			
			VariableLoader varLoader = new VariableLoader(code, allocs, frameOffsets, globalOffsets);
			
			for(int param=0; param<funcParams.length; ++param) {
				varLoader.install(funcParams[param], (funcParams.length - param) * WORD_SIZE - offset);
			}
			
			for(Block block: function) {
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
										r = varLoader.load(parameters[0]);
									} else {
										code.add(new Code(Op.ADDI, TEMP_REG, 0, Integer.parseInt(parameters[0])));
										r = TEMP_REG;
									}
									code.add(new Code(Op.WRI, 0, r, 0));
									varLoader.push(r, parameters[0]);
								}
							} else if(instr.assignee.startsWith("call printBool(")) {
								String [] parameters = instr.getParameters();
								if(parameters.length == 1) {
									int r;
									if(Instruction.isVar(parameters[0])) {
										r = varLoader.load(parameters[0]);
									} else {
										int bool = 0;
										if(parameters[0].equals("true")) bool = 1;
										code.add(new Code(Op.ADDI, TEMP_REG, 0, bool));
										r = TEMP_REG;
									}
									code.add(new Code(Op.WRI, 0, r, 0));
									varLoader.push(r, parameters[0]);
								}
							} else if(instr.assignee.equals("call println()")) {
								code.add(new Code(Op.WRL, 0, 0, 0));
							} else {
								callFunction(instr, code, varLoader, functionCalls, offset);
							}
						} else {
							if(instr.value1.equals("call readInt()")) {
								int assignee = varLoader.load(instr.assignee);
								code.add(new Code(Op.RDI, assignee, 0, 0));
								varLoader.push(assignee, instr.assignee);
							} else if(instr.value1.equals("call readBool()")) {
								int assignee = varLoader.load(instr.assignee);
								code.add(new Code(Op.RDB, assignee, 0, 0));
								varLoader.push(assignee, instr.assignee);
							} else {
								callFunction(instr, code, varLoader, functionCalls, offset);
								varLoader.install(instr.assignee, 0);
							}
						}
					} else if(instr.isCopy()) {
						int assignee = varLoader.load(instr.assignee);
						if(Instruction.isVar(instr.value1)) {
							int assignment = varLoader.load(instr.value1);
							code.add(new Code(Op.ADDI, assignee, assignment, 0));
							varLoader.push(assignment, instr.value1);
						} else {
							int value;
							try {
								value = Integer.parseInt(instr.value1);
							} catch(NumberFormatException e) {
								if(instr.value1.equals("true")) value = 1;
								else value = 0;
							}
							code.add(new Code(Op.ADDI, assignee, 0, value));
						}
						varLoader.push(assignee, instr.assignee);
					} else if(instr.isNot()) {
						code.add(new Code(Op.ADDI, TEMP_REG, 0, 1));
						Op op;
						int bool;
						if(Instruction.isVar(instr.value1)) {
							op = Op.BIC;
							bool = varLoader.load(instr.value1);
						} else {
							op = Op.BICI;
							if(instr.value1.equals("true")) bool = 1;
							else bool = 0;
						}
						int assignee = varLoader.load(instr.assignee);
						code.add(new Code(op, assignee, TEMP_REG, bool));
						if(Instruction.isVar(instr.value1)) varLoader.push(bool, instr.value1);
						varLoader.push(assignee, instr.assignee);
					} else if(instr.isOp()) {
						if(instr.isComparison()) {
							Op op;
							int first;
							int second;
							if(Instruction.isVar(instr.value1)) {
								first = varLoader.load(instr.value1);
								if(Instruction.isVar(instr.value2)) {
									op = Op.CMP;
									second = varLoader.load(instr.value2);
								} else {
									op = Op.CMPI;
									second = Integer.parseInt(instr.value2);
								}
							} else {
								code.add(new Code(Op.ADDI, TEMP_REG, 0, Integer.parseInt(instr.value1)));
								first = TEMP_REG;
								if(Instruction.isVar(instr.value2)) {
									op = Op.CMP;
									second = varLoader.load(instr.value2);
								} else {
									op = Op.CMPI;
									second = Integer.parseInt(instr.value2);
								}
							}
							code.add(new Code(op, TEMP_REG, first, second));
							varLoader.push(first, instr.value1);
							varLoader.push(second, instr.value2);
							int assignee = varLoader.load(instr.assignee);
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
							code.add(new Code(op, TEMP_REG, 0, 2));
							code.add(new Code(Op.ADDI, assignee, 0, 0));
							varLoader.push(assignee, instr.assignee);
						} else {
							int assignee;
							Op op;
							int first;
							int second;
							if(Instruction.isVar(instr.value1)) {
								first = varLoader.load(instr.value1);
								if(Instruction.isVar(instr.value2)) {
									op = Op.fromInstructOp(instr.op, false);
									second = varLoader.load(instr.value2);
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
								code.add(new Code(Op.ADDI, TEMP_REG, 0, first));
								first = TEMP_REG;
								if(Instruction.isVar(instr.value2)) {
									op = Op.fromInstructOp(instr.op, false);
									second = varLoader.load(instr.value2);
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
							if(varLoader.isSpilled(instr.assignee) && (first == 25 && second == 26 || first == 26 && second == 25)) {
								assignee = varLoader.specialLoad(instr.assignee, TEMP_REG);
							} else {
								assignee = varLoader.load(instr.assignee);
							}
							code.add(new Code(op, assignee, first, second));
							if(assignee == TEMP_REG) {
								varLoader.specialPush(assignee, instr.assignee);
							} else {
								varLoader.push(assignee, instr.assignee);
							}
						}
					} else if(instr.isJump() && !instr.isReturn()) {
						Op op;
						int decision = 0;
						if(instr.isConditionalJump()) {
							op = Op.BNE;
							if(Instruction.isVar(instr.value1)) {
								decision = varLoader.load(instr.value1);
							} else {
								int bool = 0;
								if(instr.value1.equals("true")) bool = 1;
								code.add(new Code(Op.ADDI, TEMP_REG, 0, bool));
								decision = TEMP_REG;
							}
						} else {
							op = Op.BEQ;
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
						varLoader.push(decision, instr.value1);
					} else if(instr.isReturn()) {
						if(function.getName().equals("main")) {
							code.add(new Code(Op.RET, 0, 0, 0));
						} else {
							if(instr.value1 != null) {
								int reg;
								if(Instruction.isVar(instr.value1)) {
									reg = varLoader.load(instr.value1);
								} else {
									int value;
									try {
										value = Integer.parseInt(instr.value1);
									} catch(NumberFormatException e) {
										if(instr.value1.equals("true")) value = 1;
										else value = 0;
									}
									code.add(new Code(Op.ADDI, TEMP_REG, 0, value));
									reg = TEMP_REG;
								}
								code.add(new Code(Op.STW, reg, FRAME_REG, WORD_SIZE));
							}
							code.add(new Code(Op.RET, 0, 0, RETURN_REG));
						}
					} else if(instr.isExit()) {
						if(function.getName().equals("main")) {
							code.add(new Code(Op.RET, 0, 0, 0));
						} else {
							code.add(new Code(Op.RET, 0, 0, RETURN_REG));
						}
					}
				}
			}
			
			functionCodes.put(function, code);
			
			// Troubleshooting
			/* System.out.println("### " + function.getName());
			for(Code inst: code) {
				System.out.println(inst);
			}
			System.out.println(); */
		}
		
		Map<String, Integer> functionSizes = new HashMap<>();
		for(Graph function: functionCodes.keySet()) {
			functionSizes.put(function.getName(), functionCodes.get(function).size());
		}
		
		Map<String, Integer> functionLocations = new HashMap<>();
		functionLocations.put("main", 0);
		int location = functionSizes.get("main");
		for(Graph function: functionCodes.keySet()) {
			if(!function.getName().equals("main")) {
				functionLocations.put(function.getName(), location);
				location += functionSizes.get(function.getName());
			}
		}
		
		for(Graph function: functions) {
			if(functionCalls.containsKey(function.getName())) {
				for(Code call: functionCalls.get(function.getName())) {
					call.setJump(functionLocations.get(function.getName()) * WORD_SIZE);
				}
			}
		}
		
		int length = location;
		int[] code = new int[length];
		int next = 0;
		for(Graph function: functionCodes.keySet()) {
			next = functionLocations.get(function.getName());
			for(Code inst: functionCodes.get(function)) {
				// System.out.println(next + ": " + inst);
				code[next++] = inst.gen();
			}
		}
		
		return code;
	}
	
	private void callFunction(Instruction instr, List<Code> code, VariableLoader varLoader, Map<String, Collection<Code>> functionCalls, int offset) {
		String [] parameters = instr.getParameters();
		for(int paramIndex=0; paramIndex<parameters.length; ++paramIndex) {
			String param = parameters[paramIndex].trim();
			int paramReg;
			if(Instruction.isVar(param)) {
				paramReg = varLoader.load(param);
			} else {
				int paramVal;
				try {
					paramVal = Integer.parseInt(param);
				} catch(NumberFormatException e) {
					if(param.equals("true")) paramVal = 1;
					else paramVal = 0;
				}
				code.add(new Code(Op.ADDI, TEMP_REG, 0, paramVal));
				paramReg = TEMP_REG;
			}
			code.add(new Code(Op.STW, paramReg, STACK_REG, paramIndex * -WORD_SIZE));
		}
		
		varLoader.save();
		
		code.add(new Code(Op.STW, RETURN_REG, STACK_REG, parameters.length * -WORD_SIZE));
		code.add(new Code(Op.ADDI, FRAME_REG, STACK_REG, parameters.length * -WORD_SIZE));
		
		Code jump = new Code(Op.JSR, -1);
		code.add(jump);
		if(!functionCalls.containsKey(instr.getFunctionName())) {
			functionCalls.put(instr.getFunctionName(), new ArrayList<>());
		}
		functionCalls.get(instr.getFunctionName()).add(jump);
		
		code.add(new Code(Op.SUBI, STACK_REG, FRAME_REG, parameters.length * -WORD_SIZE));
		code.add(new Code(Op.SUBI, FRAME_REG, STACK_REG, offset));
		code.add(new Code(Op.LDW, RETURN_REG, STACK_REG, parameters.length * -WORD_SIZE));
		
		varLoader.restore();
	}
	
	public boolean hasError() {
		return ast.hasError();
	}
	
	public String errorReport() {
		return ast.errorReport();
	}
	
}
