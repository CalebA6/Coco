package coco;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import code.Code;
import code.Op;

public class VariableLoader {
	
	private List<Code> code;
	private Map<String, Integer> funcVarOffsets;
	private Map<String, Integer> globalVarOffsets;
	private Map<String, Integer> regAllocs;
	private Set<Integer> crossAllocedRegs;
	private int currentReg = 25;
	
	public VariableLoader(List<Code> code, Map<String, Integer> regAllocs, Map<String, Integer> funcVarOffsets, Map<String, Integer> globalVarOffsets) {
		this.code = code;
		this.regAllocs = regAllocs;
		this.funcVarOffsets = funcVarOffsets;
		this.globalVarOffsets = globalVarOffsets;
		
		crossAllocedRegs = new HashSet<>();
		for(String funcVar: funcVarOffsets.keySet()) {
			for(String globalVar: globalVarOffsets.keySet()) {
				if(regAllocs.get(funcVar) == regAllocs.get(globalVar)) {
					crossAllocedRegs.add(regAllocs.get(globalVar));
					break;
				}
			}
		}
	}
	
	public int load(String var) {
		if(regAllocs.containsKey(var) && regAllocs.get(var) != 0) return regAllocs.get(var);

		currentReg ^= 3; // Switches back and forth between 25 and 26
		return specialLoad(var, currentReg);
	}
	
	public void push(int reg, String var) {
		if(reg == 25 || reg == 26) {
			specialPush(reg, var);
		}
	}
	
	public boolean isSpilled(String var) {
		return !regAllocs.containsKey(var) || regAllocs.get(var) == 0;
	}
	
	public int specialLoad(String var, int reg) {
		if(funcVarOffsets.containsKey(var)) {
			code.add(new Code(Op.LDW, reg, Compiler.FRAME_REG, funcVarOffsets.get(var)));
			return currentReg;
		} else if(globalVarOffsets.containsKey(var)) {
			code.add(new Code(Op.LDW, reg, Compiler.GLOBAL_REG, globalVarOffsets.get(var)));
			return currentReg;
		} else {
			return 0;
		}
	}
	
	public void specialPush(int reg, String var) {
		int pointerReg;
		int offset;
		if(funcVarOffsets.containsKey(var)) {
			pointerReg = Compiler.FRAME_REG;
			offset = funcVarOffsets.get(var);
		} else {
			pointerReg = Compiler.GLOBAL_REG;
			offset = globalVarOffsets.get(var);
		}
		code.add(new Code(Op.STW, reg, pointerReg, offset));
	}
	
	public void install(String var, int offset) {
		if(regAllocs.containsKey(var)) {
			code.add(new Code(Op.LDW, regAllocs.get(var), Compiler.STACK_REG, offset));
		} else if(funcVarOffsets.containsKey(var)) {
			code.add(new Code(Op.LDW, Compiler.TEMP_REG, Compiler.STACK_REG, offset));
			code.add(new Code(Op.STW, Compiler.TEMP_REG, Compiler.FRAME_REG, funcVarOffsets.get(var)));
		}
		// else: unused parameter
	}
	
	public void save() {
		for(String var: funcVarOffsets.keySet()) {
			if(!isSpilled(var)) {
				specialPush(regAllocs.get(var), var);
			}
		}
		saveGlobals();
	}
	
	public void saveGlobals() {
		for(String var: globalVarOffsets.keySet()) {
			if(!isSpilled(var) && !funcVarOffsets.containsKey(var)) {
				specialPush(regAllocs.get(var), var);
			}
		}
	}
	
	public void restore() {
		for(String var: funcVarOffsets.keySet()) {
			if(!isSpilled(var)) {
				specialLoad(var, regAllocs.get(var));
			}
		}
		for(String var: globalVarOffsets.keySet()) {
			if(!isSpilled(var) && !funcVarOffsets.containsKey(var)) {
				specialLoad(var, regAllocs.get(var));
			}
		}
	}
	
	public boolean isCrossAlloced(String var) {
		return globalVarOffsets.containsKey(var) && crossAllocedRegs.contains(regAllocs.get(var));
	}
	
}
