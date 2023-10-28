package ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Graph {
	
	private String name;
	private Block entry;
	private List<Block> blocks = new ArrayList<>();
	
	public Graph(String function, ValueCode code, Set<String> globalVariables) {
		name = function;
		
		List<Instruction> instructions = code.instructions;
		assignIndicies(instructions);
		moveJumpsOffNoOps(instructions);
		removeNoOps(instructions);
		addExit(instructions);
		assignIndicies(instructions);
		addJumpTargets(instructions);
		Block block = entry = new Block(globalVariables);
		blocks.add(block);
		for(Instruction instr: instructions) {
			if(instr.targeted()) {
				Block newBlock = new Block(globalVariables);
				block.addSuccessor(newBlock);
				block = newBlock;
				blocks.add(block);
			}
			block.addInstruction(instr);
			if(instr.isJump()) {
				Block newBlock = new Block(globalVariables);
				if(instr.isConditionalJump()) block.addSuccessor(newBlock);
				block = newBlock;
				blocks.add(block);
			}
		}
		updateSuccessors(blocks);
	}
	
	public String dotGraph() {
		StringBuilder dot = new StringBuilder();
		List<String> edges = new ArrayList<>();
		dot.append("digraph ");
		dot.append(name);
		dot.append("{\n");
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
			dot.append("\t");
			dot.append(block.getName());
			dot.append(" [shape=record, label=\"<b>");
			dot.append(block.getName());
			dot.append(" | {");
			boolean first = true;
			for(Instruction instr: block) {
				if(first) {
					first = false;
				} else {
					dot.append("|");
				}
				dot.append(instr);
			}
			dot.append("}\"];\n");
			for(Block successor: block.getSuccessors()) {
				edges.add(block.getName() + ":s -> " + successor.getName() + ":n");
			}
			blocks.addAll(block.getSuccessors());
		}
		for(String edge: edges) {
			dot.append("\t");
			dot.append(edge);
			dot.append("\n");
		}
		dot.append("}\n");
		return dot.toString();
	}
	
	public void eliminateDeadCode() {
		boolean change = true;
		while(change) {
			change = false;
			
			boolean lvChange = true;
			while(lvChange) {
				lvChange = false;
				for(Block block: blocks) {
					lvChange = block.updateLiveVariables() || lvChange;
				}
			}
			
			for(Block block: blocks) {
				change = block.eliminateDeadCode() || change;
			}
		}
	}
	
	private void assignIndicies(List<Instruction> instructions) {
		int index = 0;
		for(Instruction instr: instructions) {
			instr.setIndex(index++);
		}
	}
	
	private void moveJumpsOffNoOps(List<Instruction> instructions) {
		boolean madeAChange = true;
		while(madeAChange) {
			madeAChange = false;
			for(Instruction instr: instructions) {
				if(instr.isJump() && !instr.isReturn()) {
					Instruction jumpTarget = instr.getJump();
					if(jumpTarget.noOp()) {
						int jumpIndex = jumpTarget.getIndex();
						if(jumpIndex < instructions.size() - 1) {
							instr.setJump(instructions.get(jumpIndex + 1));
							madeAChange = true;
						}
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
	
	private Instruction addExit(List<Instruction> instructions) {
		Instruction newExit = new Instruction();
		if(instructions.size() > 0) {
			Instruction last = instructions.get(instructions.size() - 1);
			if(!last.noOp()) {
				instructions.add(newExit);
			} else {
				return last;
			}
		} else {
			instructions.add(newExit);
		}
		return newExit;
	}
	
	private void addJumpTargets(List<Instruction> instructions) {
		for(Instruction instr: instructions) {
			if(instr.isJump() && !instr.isReturn()) {
				instr.getJump().addTargetingJump(instr);
			}
		}
	}
	
	private void updateSuccessors(List<Block> blocks) {
		for(Block block: blocks) {
			block.addJumpSuccessor();
		}
	}
	
}
