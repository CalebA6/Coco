package ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Block implements Iterable<Instruction> {

	private static int index = 0;
	List<Instruction> instructions = new ArrayList<>();
	Set<Block> predeccessors = new HashSet<>();
	Set<Block> successors = new HashSet<>();
	String name = "B" + ++index;
	boolean entry = false;
	
	Set<String> varIn = new HashSet<>();
	Set<String> varOut = new HashSet<>();
	Map<String, String> expIn = new HashMap<>();
	Map<String, String> expOut = new HashMap<>();
	Set<String> setIn = new HashSet<>();
	Set<String> setOut = new HashSet<>();
	
	Set<String> globalVariables;
	
	Graph graph;
	
	public Block(Set<String> globalVariables, Graph graph) {
		this.globalVariables = globalVariables;
		this.graph = graph;
	}
	
	public Block(Set<String> globalVariables, Graph graph, boolean entry) {
		this(globalVariables, graph);
		this.entry = entry;
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
		if(successors.add(successor)) {
			successor.addPredeccessor(this);
		}
	}
	
	public void addSuccessors(Collection<Block> successors) {
		for(Block successor: successors) {
			if(this.successors.add(successor)) {
				successor.addPredeccessor(this);
			}
		}
	}
	
	public void removeSuccessor(Block successor) {
		if(successors.remove(successor) && (instructions.size() > 0)) {
			Instruction last = instructions.get(instructions.size() - 1);
			if((last != null && !last.isReturn()) && last.isJump() && (last.getJump().getBlock() == successor)) {
				last.setJump(successor.getFirst());
			}
		}
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
	
	public void addPredeccessor(Block predeccessor) {
		if(predeccessors.add(predeccessor)) {
			predeccessor.addSuccessor(this);
		}
	}
	
	public void addPredeccessors(Collection<Block> predeccessors) {
		for(Block predeccessor: predeccessors) {
			if(this.predeccessors.add(predeccessor)) {
				predeccessor.addSuccessor(this);
			}
		}
	}
	
	public void removePredeccessor(Block predeccessor) {
		successors.remove(predeccessor);
	}
	
	public String getName() {
		return name;
	}
	
	protected void clearPropagationSets() {
		varIn = new HashSet<>();
		varOut = new HashSet<>();
	}
	
	public void pruneAtReturn() {
		boolean returned = false;
		for(int i=0; i<numInstructions(); ++i) {
			if(getInstruction(i).isReturn()) {
				returned = true;
			} else if(returned) {
				instructions.remove(i);
			}
		}
		if(returned) {
			Collection<Block> wrongs = new ArrayList<>(successors);
			for(Block block: wrongs) {
				removeSuccessor(block);
			}
		}
	}
	
	public boolean updateLiveVariables() {
		varOut = new HashSet<>();
		for(Block after: successors) {
			varOut.addAll(after.varIn);
		}
		if(successors.size() == 0 && !graph.getSignature().equals("main()")) {
			varOut.addAll(globalVariables);
		}
		
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(varOut);
		for(int i = numInstructions()-1; i>=0; --i) {
			Instruction instr = instructions.get(i);
			if(instr.isCall() || instr.isVoidCall()) {
				String function = instr.isCall() ? instr.value1 : instr.assignee;
				liveVariables.addAll(functionParameters(function));
				if(!instr.isBuiltInFunction()) liveVariables.addAll(globalVariables);
			}
			if(instr.assignee != null) {
				liveVariables.remove(instr.assignee);
			}
			if(instr.value1 != null && Instruction.isVar(instr.value1)) {
				liveVariables.add(instr.value1);
			}
			if(instr.value2 != null && Instruction.isVar(instr.value2)) {
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
		if(!entry) {
			Map<String, Boolean> exprSeen = new HashMap<>();
			boolean first = true;
			for(Block before: predeccessors) {
				for(String assignment: before.expOut.keySet()) {
					String value = before.expOut.get(assignment);
					if(expIn.containsKey(assignment) && !value.equals(expIn.get(assignment))) {
						expIn.put(assignment, "#any");
					} else if(first) {
						expIn.put(assignment, value);
					} else {
						exprSeen.put(assignment, true);
					}
				}
				
				if(first) {
					for(String expr: expIn.keySet()) {
						exprSeen.put(expr, false);
					}
					first = false;
				} else {
					Set<String> exprs = new HashSet<>();
					exprs.addAll(expIn.keySet());
					for(String expr: exprs) {
						if(exprSeen.get(expr)) {
							exprSeen.put(expr, false);
						} else {
							expIn.remove(expr);
						}
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
			} else if(instr.isVoidCall() || instr.isCall()) {
				if(!instr.isBuiltInFunction()) {
					Set<String> needsRemoving = new HashSet<>();
					needsRemoving.addAll(globalVariables);
					for(String exp: availExp.keySet()) {
						String[] vars = availExp.get(exp).split(" ");
						for(String var: vars) {
							if(globalVariables.contains(var)) {
								needsRemoving.add(exp);
							}
						}
					}
					for(String var: needsRemoving) {
						availExp.remove(var);
					}
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
	
	public boolean updateSetVariables() {
		setIn = new HashSet<>();
		for(Block predeccessor: predeccessors) {
			setIn.addAll(predeccessor.setOut);
		}
		if(entry) {
			setIn.addAll(Arrays.asList(graph.getParameters()));
		}
		
		Set<String> setVars = new HashSet<>();
		setVars.addAll(setIn);
		for(int i = 0; i<numInstructions(); ++i) {
			Instruction instr = instructions.get(i);
			if(instr.assignee != null && !instr.isVoidCall()) {
				setVars.add(instr.assignee);
			}
		}
		
		boolean change = !setOut.equals(setVars);
		setOut = setVars;
		return change;
	}
	
	protected boolean eliminateDeadCode() {
		boolean change = false;
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(varOut);
		for(int i = numInstructions()-1; i>=0; --i) {
			Instruction instr = instructions.get(i);
			if(instr.isCall() || instr.isVoidCall()) {
				if(instr.isCall() && !liveVariables.contains(instr.assignee)) {
					instr.assignee = instr.value1;
					instr.value1 = null;
				}
				liveVariables.addAll(Arrays.asList(instr.getParameters()));
				if(!instr.isBuiltInFunction()) liveVariables.addAll(globalVariables);
			} else if(instr.isAssignment() && !liveVariables.contains(instr.assignee)) {
				// Updates Jumps
				for(Instruction target: instr.getTargetingJumps()) {
					if(i < numInstructions()-1) {
						target.setJump(instructions.get(i+1));
					} else {
						// Block has been emptied
						for(Block predeccessor: predeccessors) {
							predeccessor.addSuccessors(successors);
							predeccessor.removeSuccessor(this);
						}
						for(Block successor: successors) {
							successor.addPredeccessors(predeccessors);
							successor.removePredeccessor(this);
							target.setJump(successor.getFirst());
						}
						graph.removeBlock(this);
					}
				}
				instructions.remove(i);
				change = true;
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
					case EQUAL: 
						boolResult = left == right;
						break;
					case NOT_EQUAL: 
						boolResult = left != right;
						break;
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
					continue;
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
			
			if(instr.op != null) {
				switch(instr.op) {
				case ADD: 
					if(instr.value1.equals("0")) {
						instr.value1 = instr.value2;
						instr.op = null;
						instr.value2 = null;
						change = true;
					} else if(instr.value2.equals("0")) {
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case SUB: 
					if(instr.value2.equals("0")) {
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case MUL: 
					if(instr.value1.equals("0") || instr.value2.equals("0")) {
						instr.value1 = "0";
						instr.op = null;
						instr.value2 = null;
						change = true;
					} else if(instr.value1.equals("1")) {
						instr.value1 = instr.value2;
						instr.op = null;
						instr.value2 = null;
						change = true;
					} else if(instr.value2.equals("1")) {
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case DIV: 
					if(instr.value2.equals("1")) {
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case MOD: 
					if(instr.value2.equals("1")) {
						instr.value1 = "0";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case POW: 
					if(instr.value2.equals("0")) {
						instr.value1 = "1";
						instr.op = null;
						instr.value2 = null;
						change = true;
					} else if(instr.value2.equals("1")) {
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case EQUAL: 
					if(instr.value1.equals(instr.value2)) {
						instr.value1 = "true";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case NOT_EQUAL: 
					if(instr.value1.equals(instr.value2)) {
						instr.value1 = "false";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case LESS_EQUAL: 
					if(instr.value1.equals(instr.value2)) {
						instr.value1 = "true";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case GREATER_EQUAL: 
					if(instr.value1.equals(instr.value2)) {
						instr.value1 = "true";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case LESS: 
					if(instr.value1.equals(instr.value2)) {
						instr.value1 = "false";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case GREATER: 
					if(instr.value1.equals(instr.value2)) {
						instr.value1 = "false";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case OR: 
					if(instr.value1.equals("true") || instr.value2.equals("true")) {
						instr.value1 = "true";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
				case AND: 
					if(instr.value1.equals("false") || instr.value2.equals("false")) {
						instr.value1 = "false";
						instr.op = null;
						instr.value2 = null;
						change = true;
					}
					break;
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
						String newValue = availConsts.get(parameters[param]);
						change = change || !newValue.equals(parameters[param]);
						instr.replaceParameter(param, newValue);
					}
				}
				if(!instr.isBuiltInFunction()) {
					for(String var: globalVariables) {
						availConsts.remove(var);
					}
				}
			} else {
				if(instr.value1 != null && availConsts.containsKey(instr.value1)) {
					String newValue = availConsts.get(instr.value1);
					change = change || !newValue.equals(instr.value1);
					instr.value1 = newValue;
				}
				if(instr.value2 != null && availConsts.containsKey(instr.value2)) {
					String newValue = availConsts.get(instr.value2);
					change = change || !newValue.equals(instr.value2);
					instr.value2 = newValue;
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
	
	public void zeroUnsetVariables() {
		Set<String> setVariables = new HashSet<>();
		setVariables.addAll(setIn);
		
		for(int i = 0; i<numInstructions(); ++i) {
			Instruction instr = instructions.get(i);
			if(instr.isVoidCall() || instr.isCall()) {
				String[] params = instr.getParameters();
				for(int param=0; param<params.length; ++param) {
					if(Instruction.isVar(params[param]) && !setVariables.contains(params[param])) {
						instr.replaceParameter(param, "0");
					}
				}
				if(!instr.isBuiltInFunction()) setVariables.addAll(globalVariables);
			}
			if(instr.assignee != null && !instr.isVoidCall()) {
				setVariables.add(instr.assignee);
			}
			if(instr.value1 != null && !instr.isCall() && Instruction.isVar(instr.value1) && !setVariables.contains(instr.value1)) {
				instr.value1 = "0";
			}
			if(instr.value2 != null && Instruction.isVar(instr.value2) && !setVariables.contains(instr.value2)) {
				instr.value2 = "0";
			}
		}
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
			if(instr.value1 != null && Instruction.isVar(instr.value1)) {
				liveVariables.add(instr.value1);
			}
			if(instr.value2 != null && Instruction.isVar(instr.value2)) {
				liveVariables.add(instr.value2);
			}
			liveSets.add(0, new ArrayList<>(liveVariables));
			if(assigneeNeedsAllocation && Instruction.isVar(instr.assignee)) {
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
