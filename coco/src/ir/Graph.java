package ir;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Graph {
	
	private String name;
	private Block entry;
	
	public Graph(String function, ValueCode code) {
		name = function;
		
		List<Instruction> instructions = code.instructions;
		assignIndicies(instructions);
		moveJumpsOffNoOps(instructions);
		removeNoOps(instructions);
		assignIndicies(instructions);
		addJumpTargets(instructions);
		entry = new Block();
		for(Instruction instr: instructions) {
			entry.addInstruction(instr);
		}
	}
	
	public String dotGraph() {
		StringBuilder dot = new StringBuilder();
		dot.append("digraph ");
		dot.append(name);
		dot.append("{\n");
		int block = 0;
		Queue<Block> blocks = new LinkedList<>();
		blocks.add(entry);
		while(!blocks.isEmpty()) {
			Block b = blocks.remove();
			dot.append("\tB");
			dot.append(++block);
			dot.append(" [shape=record, label=\"<b>B");
			dot.append(block);
			dot.append(" | {");
			boolean first = true;
			for(Instruction instr: b) {
				if(first) {
					first = false;
				} else {
					dot.append("|");
				}
				dot.append(instr);
			}
			dot.append("}\"];\n");
		}
		dot.append("}\n");
		return dot.toString();
	}
	
	private void assignIndicies(List<Instruction> instructions) {
		int index = 0;
		for(Instruction instr: instructions) {
			instr.setIndex(index++);
		}
	}
	
	private void moveJumpsOffNoOps(List<Instruction> instructions) {
		for(Instruction instr: instructions) {
			if(instr.isJump() && !instr.isReturn()) {
				Instruction jumpTarget = instr.getJump();
				if(jumpTarget.noOp()) {
					int jumpIndex = jumpTarget.getIndex();
					if(jumpIndex < instructions.size() - 1) {
						instr.setJump(instructions.get(jumpIndex + 1));
					}
				}
			}
		}
	}
	
	private void removeNoOps(List<Instruction> instructions) {
		for(int instr=0; instr<instructions.size()-1; ++instr) {
			while((instr < instructions.size()-1) && instructions.get(instr).noOp()) {
				instructions.remove(instr);
			}
		}
	}
	
	private void addJumpTargets(List<Instruction> instructions) {
		for(Instruction instr: instructions) {
			if(instr.isJump() && !instr.isReturn()) {
				instr.getJump().addTargetingJump(instr);
			}
		}
	}
	
}
