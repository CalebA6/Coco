package ir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import coco.Token;

public class Block implements Iterable<Instruction> {

	private static int index = 0;
	List<Instruction> instructions = new ArrayList<>();
	Set<Block> predeccessors = new HashSet<>();
	Set<Block> successors = new HashSet<>();
	String name = "B" + ++index;
	
	Set<String> varIn = new HashSet<>();
	Set<String> varOut = new HashSet<>();
	Map<String, String> expIn = new HashMap<>();
	Map<String, String> expOut = new HashMap<>();
	
	Set<String> globalVariables;
	
	public Block(Set<String> globalVariables) {
		this.globalVariables = globalVariables;
	}
	
	public void addInstruction(Instruction instruction) {
		instruction.setBlock(this);
		instructions.add(instruction);
	}
	
	public Instruction getInstruction(int index) {
		return instructions.get(index);
	}
	
	public int numInstructions() {
		return instructions.size();
	}

	@Override
	public Iterator<Instruction> iterator() {
		return new BlockIterator(this);
	}
	
	public void addSuccessor(Block successor) {
		successors.add(successor);
		successor.predeccessors.add(this);
	}
	
	protected void addJumpSuccessor() {
		int size = numInstructions();
		if(size > 0) {
			Instruction last = getInstruction(size - 1);
			if(last.isJump() && !last.isReturn()) {
				addSuccessor(last.getJump().getBlock());
			}
		}
	}
	
	public Set<Block> getSuccessors() {
		return successors;
	}
	
	public String getName() {
		return name;
	}
	
	protected void clearPropagationSets() {
		varIn = new HashSet<>();
		varOut = new HashSet<>();
	}
	
	public boolean updateLiveVariables() {
		varOut = new HashSet<>();
		for(Block after: successors) {
			varOut.addAll(after.varIn);
		}
		
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(varOut);
		for(int i = numInstructions()-1; i>=0; --i) {
			Instruction instr = instructions.get(i);
			if(instr.isCall() || instr.isVoidCall()) {
				String function = instr.isCall() ? instr.value1 : instr.assignee;
				liveVariables.addAll(functionParameters(function));
				liveVariables.addAll(globalVariables);
			}
			if(instr.assignee != null) {
				liveVariables.remove(instr.assignee);
			}
			if(instr.value1 != null && Token.isIdent(instr.value1)) {
				liveVariables.add(instr.value1);
			}
			if(instr.value2 != null && Token.isIdent(instr.value2)) {
				liveVariables.add(instr.value2);
			}
		}
		
		if(liveVariables.size() != varIn.size()) {
			varIn = liveVariables;
			return true;
		}
		for(String var: liveVariables) {
			if(!varIn.contains(var)) {
				varIn = liveVariables;
				return true;
			}
		}
		varIn = liveVariables;
		return false;
	}
	
	public boolean updateAvailableExpressions() {
		expIn = new HashMap<>();
		Map<String, Boolean> exprSeen = new HashMap<>();
		boolean first = true;
		for(Block before: predeccessors) {
			for(String assignment: before.expOut.keySet()) {
				String value = before.expOut.get(assignment);
				if(expIn.containsKey(assignment) && !value.equals(expIn.get(assignment))) {
					expIn.put(assignment, "#any");
				} else if(expIn.containsKey(assignment)) {
					if(first) {
						expIn.put(assignment, value);
					} else {
						exprSeen.put(assignment, true);
					}
				}
			}
			
			if(first) {
				for(String expr: expIn.keySet()) {
					exprSeen.put(expr, false);
				}
				first = false;
			} else {
				for(String expr: expIn.keySet()) {
					if(exprSeen.get(expr)) {
						exprSeen.put(expr, false);
					} else {
						expIn.remove(expr);
					}
				}
			}
		}
		
		Map<String, String> availExp = new HashMap<>();
		copyMap(expIn, availExp);
		for(Instruction instr: instructions) {
			if(instr.isAssignment() && !instr.isCall()) {
				String[] exp = instr.toString().split(" = ");
				String var = exp[0].split(" ")[1];
				String value = exp[1];
				if(!exprContainsValue(value, var)) {
					availExp.put(var, value);
				}
			}
		}
		
		if(availExp.size() != expOut.size()) {
			expOut = availExp;
			return true;
		}
		for(String exp: availExp.keySet()) {
			if(!expOut.containsKey(exp) || !expOut.get(exp).equals(availExp.get(exp))) {
				expOut = availExp;
				return true;
			}
		}
		expOut = availExp;
		return false;
	}
	
	protected boolean eliminateDeadCode() {
		boolean change = false;
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(varOut);
		for(int i = numInstructions()-1; i>=0; --i) {
			Instruction instr = instructions.get(i);
			if(instr.isAssignment() && !liveVariables.contains(instr.assignee)) {
				// Updates Jumps
				for(Instruction target: instr.getTargetingJumps()) {
					if(i < numInstructions()-1) {
						target.setJump(instructions.get(i+1));
					} else {
						// Block has been emptied
						for(Block predeccessor: predeccessors) {
							predeccessor.successors.addAll(successors);
							predeccessor.successors.remove(this);
						}
						for(Block successor: successors) {
							successor.predeccessors.addAll(predeccessors);
							successor.predeccessors.remove(this);
							target.setJump(successor.getFirst());
						}
					}
				}
				instructions.remove(i);
				change = true;
			} else if(instr.isCall() || instr.isVoidCall()) {
				String function = instr.isCall() ? instr.value1 : instr.assignee;
				liveVariables.addAll(functionParameters(function));
				liveVariables.addAll(globalVariables);
			} else {
				if(instr.assignee != null) {
					liveVariables.remove(instr.assignee);
				}
				if(instr.value1 != null) {
					liveVariables.add(instr.value1);
				}
				if(instr.value2 != null) {
					liveVariables.add(instr.value2);
				}
			}
		}
		return change;
	}
	
	public boolean foldConstants() {
		boolean change = false;
		for(Instruction instr: instructions) {
			try {
				int left = Integer.parseInt(instr.value1);
				int right = Integer.parseInt(instr.value2);
				int result = 0;
				switch(instr.op) {
				case ADD: 
					result = left + right;
					break;
				case SUB: 
					result = left - right;
					break;
				case MUL: 
					result = left * right;
					break;
				case DIV: 
					result = left / right;
					break;
				case MOD: 
					result = left % right;
					break;
				case POW: 
					result = (int)Math.pow(left, right);
					break;
				default: 
					boolean boolResult;
					switch(instr.op) {
					case LESS_EQUAL: 
						boolResult = left <= right;
						break;
					case GREATER_EQUAL: 
						boolResult = left >= right;
						break;
					case LESS: 
						boolResult = left < right;
						break;
					case GREATER: 
						boolResult = left > right;
						break;
					default: 
						throw new RuntimeException("Invalid operation");
					}
					instr.value1 = Boolean.toString(boolResult);
					instr.op = null;
					instr.value2 = null;
					change = true;
				}
				instr.value1 = Integer.toString(result);
				instr.op = null;
				instr.value2 = null;
				change = true;
			} catch (Exception e) {
				try {
					boolean left = toBoolean(instr.value1);
					boolean right = toBoolean(instr.value2);
					boolean result;
					switch(instr.op) {
					case EQUAL: 
						result = left == right;
						break;
					case NOT_EQUAL: 
						result = left != right;
						break;
					case OR: 
						result = left || right;
						break;
					case AND: 
						result = left && right;
						break;
					default: 
						throw new RuntimeException("Invalid operation");
					}
					instr.value1 = Boolean.toString(result);
					instr.op = null;
					instr.value2 = null;
					change = true;
				} catch(Exception e1) {
					if(instr.op != null && instr.op == InstructType.NOT && (instr.value1.equals("true") || instr.value1.equals("false"))) {
						boolean value = toBoolean(instr.value1);
						instr.value1 = Boolean.toString(!value);
						instr.op = null;
						change = true;
					}
				}
			}
		}
		return change;
	}
	
	public boolean propagateAssignments(boolean consts) {
		Map<String, String> availConsts = new HashMap<>();
		copyMap(expIn, availConsts);
		Set<String> nonConstantExpr = new HashSet<>();
		for(String var: availConsts.keySet()) {
			if(availConsts.get(var).equals("#any")) {
				nonConstantExpr.add(var);
			} else if(!isConst(availConsts.get(var))) {
				nonConstantExpr.add(var);
			} else if((consts && Instruction.isVar(var)) || (!consts && !Instruction.isVar(var))) {
				nonConstantExpr.add(var);
			}
		}
		for(String expr: nonConstantExpr) {
			availConsts.remove(expr);
		}
		
		boolean change = false;
		
		for(Instruction instr: instructions) {
			if(instr.isCall() || instr.isVoidCall()) {
				String[] parameters = instr.getParameters();
				for(int param=0; param<parameters.length; ++param) {
					if(availConsts.containsKey(parameters[param])) {
						instr.replaceParameter(param, availConsts.get(parameters[param]));
						change = true;
					}
				}
			} else {
				if(instr.value1 != null && availConsts.containsKey(instr.value1)) {
					instr.value1 = availConsts.get(instr.value1);
					change = true;
				}
				if(instr.value2 != null && availConsts.containsKey(instr.value2)) {
					instr.value2 = availConsts.get(instr.value2);
					change = true;
				}
			}
			if(instr.isCopy() && !instr.isCall() && ((consts && !Instruction.isVar(instr.value1)) || (!consts && Instruction.isVar(instr.value1)))) {
				availConsts.put(instr.assignee, instr.value1);
			} else if(instr.assignee != null) {
				availConsts.remove(instr.assignee);
			}
		}
		
		return change;
	}
	
	public boolean eliminateCommonSubexpressions() {
		Map<String, String> availExprVars = new HashMap<>();
		copyMap(expIn, availExprVars);
		Set<String> nonConstantExpr = new HashSet<>();
		for(String var: availExprVars.keySet()) {
			if(availExprVars.get(var).equals("#any")) {
				nonConstantExpr.add(var);
			} else if(isConst(availExprVars.get(var))) {
				nonConstantExpr.add(var);
			}
		}
		for(String expr: nonConstantExpr) {
			availExprVars.remove(expr);
		}
		
		boolean change = false;
		
		for(Instruction instr: instructions) {
			if(instr.isExpr()) {
				// Gets Variables
				String[] exp = instr.toString().split(" = ");
				String var = exp[0].split(" ")[1];
				String value = exp[1];
				
				// Checks if expression can be eliminated
				if(availExprVars.values().contains(value)) {
					for(String potentialVar: availExprVars.keySet()) {
						if(availExprVars.get(potentialVar).equals(value)) {
							instr.makeCopy(potentialVar);
							change = true;
							break;
						}
					}
					// Remove expressions with changed operands
					Set<String> exprsToRemove = new HashSet<>();
					for(String expr: availExprVars.keySet()) {
						String[] ops = availExprVars.get(expr).split(" ");
						for(String op: ops) {
							if(op.equals(instr.assignee)) {
								exprsToRemove.add(expr);
							}
						}
					}
					for(String expr: exprsToRemove) {
						availExprVars.remove(expr);
					}
				} else {
					// Adds Expression to Map (assuming the variable isn't defined in terms of itself)
					String[] ops = value.split(" ");
					boolean safeToAdd = true;
					for(String op: ops) {
						if(op.equals(instr.assignee)) {
							safeToAdd = false;
							break;
						}
					}
					if(safeToAdd) availExprVars.put(var, value);
				}
			} else if(instr.isCopy()) {
				// Remove expressions changed by copy
				Set<String> exprsToRemove = new HashSet<>();
				for(String expr: availExprVars.keySet()) {
					String[] ops = availExprVars.get(expr).split(" ");
					for(String op: ops) {
						if(op.equals(instr.assignee)) {
							exprsToRemove.add(expr);
						}
					}
				}
				for(String expr: exprsToRemove) {
					availExprVars.remove(expr);
				}
			}
		}
		
		return change;
	}
	
	public List<Collection<String>> genLiveSets() {
		varOut = new HashSet<>();
		for(Block after: successors) {
			varOut.addAll(after.varIn);
		}
		
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(varOut);
		List<Collection<String>> liveSets = new ArrayList<>();
		for(int i = numInstructions()-1; i>=0; --i) {
			liveSets.add(0, new ArrayList<>(liveVariables));
			Instruction instr = instructions.get(i);
			if(instr.isCall() || instr.isVoidCall()) {
				String function = instr.isCall() ? instr.value1 : instr.assignee;
				liveVariables.addAll(functionParameters(function));
				liveVariables.addAll(globalVariables);
			}
			boolean assigneeNeedsAllocation = false;
			if(instr.assignee != null) {
				if(!liveVariables.remove(instr.assignee)) {
					assigneeNeedsAllocation = true;
				}
			}
			if(instr.value1 != null && Token.isIdent(instr.value1)) {
				liveVariables.add(instr.value1);
			}
			if(instr.value2 != null && Token.isIdent(instr.value2)) {
				liveVariables.add(instr.value2);
			}
			liveSets.add(0, new ArrayList<>(liveVariables));
			if(assigneeNeedsAllocation && Token.isIdent(instr.assignee)) {
				liveSets.get(0).add(instr.assignee);
			}
		}
		
		liveSets.add(0, new ArrayList<>(liveVariables));
		
		return liveSets;
	}
	
	private List<String> functionParameters(String functionCall) {
		String afterClosing = functionCall.split("\\(")[1];
		String parametersString = afterClosing.substring(0, afterClosing.length()-1);
		String[] paramsUntrimed = parametersString.split(",");
		List<String> params = new ArrayList<>();
		for(String parameter: paramsUntrimed) {
			String param = parameter.trim();
			if(!param.equals("")) {
				params.add(param);
			}
		}
		return params;
		
	}
	
	private boolean toBoolean(String value) {
		if(value.equals("true")) return true;
		if(value.equals("false")) return false;
		throw new RuntimeException("Not a boolean");
	}
	
	private void copyMap(Map original, Map copy) {
		for(Object key: original.keySet()) {
			copy.put(key, original.get(key));
		}
	}
	
	private boolean exprContainsValue(String expr, String value) {
		String[] ops = expr.split(" ");
		for(String op: ops) {
			if(op.equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isConst(String expression) {
		return !(expression.contains(" ") || expression.contains("("));
	}
	
	protected Instruction getFirst() {
		return instructions.get(0);
	}
	
}
