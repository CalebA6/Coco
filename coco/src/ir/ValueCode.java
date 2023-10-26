package ir;

import java.util.List;

public class ValueCode {
	
	public List<Instruction> instructions;
	public String returnValue = null;
	
	public ValueCode(List<Instruction> instructions, String returnValue) {
		this.instructions = instructions;
		this.returnValue = returnValue;
	}
	
	public ValueCode(List<Instruction> instructions) {
		this.instructions = instructions;
	}
	
}
