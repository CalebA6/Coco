package ir;

import java.util.HashSet;
import java.util.Set;

public class Instruction {
	
	String assignee = null;
	String value1 = null;
	InstructType op = null;
	String value2 = null;
	Instruction location = null;
	
	private int index;
	
	private Set<Instruction> targetingJumps = new HashSet<>();
	
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
	
	public boolean isJump() {
		return location != null || op == InstructType.RETURN;
	}
	
	public boolean isReturn() {
		return op == InstructType.RETURN;
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
	
}
