package ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Block implements Iterable<Instruction> {

	private static int index = 0;
	List<Instruction> instructions = new ArrayList<>();
	Set<Block> predeccessors = new HashSet<>();
	Set<Block> successors = new HashSet<>();
	String name = "B" + ++index;
	
	Set<String> in = new HashSet<>();
	Set<String> out = new HashSet<>();
	
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
	
	protected boolean updateLiveVariables() {
		out = new HashSet<>();
		for(Block after: successors) {
			out.addAll(after.in);
		}
		
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(out);
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
			if(instr.value1 != null) {
				liveVariables.add(instr.value1);
			}
			if(instr.value2 != null) {
				liveVariables.add(instr.value2);
			}
		}
		
		if(liveVariables.size() != in.size()) {
			in = liveVariables;
			return true;
		}
		for(String var: liveVariables) {
			if(!in.contains(var)) {
				in = liveVariables;
				return true;
			}
		}
		in = liveVariables;
		return false;
	}
	
	protected boolean eliminateDeadCode() {
		boolean change = false;
		Set<String> liveVariables = new HashSet<>();
		liveVariables.addAll(out);
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
	
	private List<String> functionParameters(String functionCall) {
		String afterClosing = functionCall.split("\\(")[1];
		String parametersString = afterClosing.substring(0, afterClosing.length()-1);
		String[] paramsUntrimed = parametersString.split(",");
		List<String> params = new ArrayList<>();
		for(String parameter: paramsUntrimed) {
			params.add(parameter.trim());
		}
		return params;
		
	}
	
	private boolean toBoolean(String value) {
		if(value.equals("true")) return true;
		if(value.equals("false")) return false;
		throw new RuntimeException("Not a boolean");
	}
	
	protected Instruction getFirst() {
		return instructions.get(0);
	}
	
}
