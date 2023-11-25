package ir;

import java.util.HashSet;
import java.util.Set;

public class Instruction {
	
	public String assignee = null;
	public String value1 = null;
	public InstructType op = null;
	public String value2 = null;
	Instruction location = null;
	
	private int index;
	
	private Set<Instruction> targetingJumps = new HashSet<>();
	private Block block;
	
	public Instruction() {
		
	}
	
	// Void Function
	public Instruction(String call) {
		assignee = call;
	}
	
	// Copy Assignment
	public Instruction(String assignee, String value) {
		this.assignee = assignee;
		this.value1 = value;
	}
	
	// NOT Instruction
	public Instruction(String assignee, InstructType op, String bool) {
		this.assignee = assignee;
		this.op = op;
		this.value1 = bool;
	}
	
	// Operation
	public Instruction(String assignee, String value1, InstructType op, String value2) {
		this.assignee = assignee;
		this.value1 = value1;
		this.op = op;
		this.value2 = value2;
	}
	
	// Jump Instruction
	public Instruction(InstructType op, Instruction location) {
		this.op = op;
		this.location = location;
	}
	
	// Conditional Jump
	public Instruction(InstructType op, Instruction location, String decision) {
		this.op = op;
		this.location = location;
		this.value1 = decision;
	}
	
	// Return Instruction
	public Instruction(InstructType op, String value) {
		this.op = op;
		this.value1 = value;
	}
	
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean noOp() {
		return assignee == null && value1 == null && op == null && value2 == null && location == null;
	}
	
	public boolean isAssignment() {
		return value1 != null && (op == null || (op != InstructType.JUMP && op != InstructType.RETURN));
	}
	
	public boolean isJump() {
		return location != null || op == InstructType.RETURN;
	}
	
	public boolean isConditionalJump() {
		return isJump() && value1 != null;
	}
	
	public boolean isReturn() {
		return op == InstructType.RETURN;
	}
	
	public boolean isCall() {
		return value1 != null && value1.startsWith("call ");
	}
	
	public boolean isVoidCall() {
		return assignee != null && assignee.startsWith("call ");
	}
	
	public boolean isCopy() {
		return assignee != null && value1 != null && op == null;
	}
	
	public boolean isExpr() {
		return value2 != null || op == InstructType.NOT;
	}
	
	public boolean isNot() {
		return op == InstructType.NOT;
	}
	
	public boolean isOp() {
		return value2 != null;
	}
	
	public boolean isExit() {
		return (op != InstructType.JUMP) && (op != InstructType.RETURN) && (assignee == null);
	}
	
	public boolean isComparison() {
		if(value2 != null) {
			switch(op) {
			case EQUAL: 
			case NOT_EQUAL: 
			case LESS_EQUAL: 
			case GREATER_EQUAL: 
			case LESS: 
			case GREATER: 
				return true;
			}
		}
		return false;
	}
	
	public String[] getParameters() {
		if(!isCall() && !isVoidCall()) {
			throw new RuntimeException("Can only get the parameters of a function call");
		}
		if(isCall()) {
			int start = value1.indexOf('(');
			String parameters = value1.substring(start + 1, value1.length() - 1);
			return parameters.split(",");
		} else {
			int start = assignee.indexOf('(');
			String parameters = assignee.substring(start + 1, assignee.length() - 1);
			return parameters.split(",");
		}
	}
	
	public void replaceParameter(int index, String newParameter) {
		if(!isCall() && !isVoidCall()) {
			throw new RuntimeException("Can only replace the parameters of a function call");
		}
		String base;
		String parametersString;
		if(isCall()) {
			int start = value1.indexOf('(');
			base = value1.substring(0, start + 1);
			parametersString = value1.substring(start + 1, value1.length() - 1);
		} else {
			int start = assignee.indexOf('(');
			base = assignee.substring(0, start + 1);
			parametersString = assignee.substring(start + 1, assignee.length() - 1);
		}
		String[] parameters = parametersString.split(",");
		for(int i=0; i<index; ++i) {
			base += parameters[i] + ",";
		}
		base += newParameter;
		for(int i=index+1; i<parameters.length; ++i) {
			base += parameters[i];
			if(i < parameters.length - 1) {
				base += ",";
			}
		}
		base += ")";
		if(isCall()) {
			value1 = base;
		} else {
			assignee = base;
		}
	}
	
	public Instruction getJump() {
		return location;
	}
	
	public void setJump(Instruction jump) {
		location = jump;
	}
	
	public void addTargetingJump(Instruction jump) {
		targetingJumps.add(jump);
	}
	
	public boolean targeted() {
		return !targetingJumps.isEmpty();
	}
	
	public boolean targeted(Instruction instr) {
		return targetingJumps.contains(instr);
	}
	
	public Set<Instruction> getTargetingJumps() {
		return targetingJumps;
	}
	
	public void makeCopy(String variable) {
		value1 = variable;
		op = null;
		value2 = null;
	}
	
	public void setBlock(Block block) {
		this.block = block;
	}
	
	public Block getBlock() {
		return block;
	}
	
	@Override
	public String toString() {
		StringBuilder instr = new StringBuilder();
		instr.append(index);
		instr.append(": ");
		if((op != InstructType.JUMP) && (op != InstructType.RETURN)) {
			if(assignee == null) {
				instr.append("EXIT");
			} else {
				instr.append(assignee);
			}
			if(value1 != null) {
				instr.append(" = ");
				if(op == InstructType.NOT) {
					instr.append(op);
					instr.append(" ");
					instr.append(value1);
				} else if(op != null) {
					instr.append(value1);
					instr.append(" ");
					instr.append(op);
					instr.append(" ");
					instr.append(value2);
				} else {
					instr.append(value1);
				}
			}
		} else {
			instr.append(op);
			if(location != null) {
				instr.append(" (");
				instr.append(location.getIndex());
				instr.append(")");
			}
			if(value1 != null) {
				instr.append(" ");
				instr.append(value1);
			}
		}
		return instr.toString();
	}
	
	public static boolean isVar(String var) {
    	if((var.length() == 0) || !Character.isLetter(var.charAt(0))) {
    		return false;
    	}
    	
    	for(int i=1; i<var.length(); ++i) {
    		if(!Character.isLetterOrDigit(var.charAt(i)) && (var.charAt(i) != '_')) {
    			return false;
    		}
    	}
    	
    	if(var.equals("true") || var.equals("false")) {
    		return false;
    	}
    	
    	return true;
	}
	
}
