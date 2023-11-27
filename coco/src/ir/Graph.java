package ir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Graph implements Iterable<Block> {
	
	private String name;
	private String[] parameters;
	private Block entry;
	private List<Block> blocks = new ArrayList<>();
	
	public Graph(String function, ValueCode code, String[] parameters, Set<String> globalVariables) {
		name = function;
		this.parameters = parameters;
		
		List<Instruction> instructions = code.instructions;
		assignIndicies(instructions);
		moveJumpsOffNoOps(instructions);
		removeNoOps(instructions);
		addExit(instructions);
		assignIndicies(instructions);
		addJumpTargets(instructions);
		Block block = entry = new Block(globalVariables, this);
		blocks.add(block);
		for(Instruction instr: instructions) {
			if(instr.targeted()) {
				Block newBlock = new Block(globalVariables, this);
				block.addSuccessor(newBlock);
				block = newBlock;
				blocks.add(block);
			}
			block.addInstruction(instr);
			if(instr.isJump()) {
				Block newBlock = new Block(globalVariables, this);
				if(instr.isConditionalJump()) block.addSuccessor(newBlock);
				block = newBlock;
				blocks.add(block);
			}
		}
		updateSuccessors(blocks);
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getParameters() {
		return parameters;
	}
	
	public int length() {
		int len = 0;
		for(Block block: blocks) {
			len += block.numInstructions();
		}
		return len;
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
	
	public boolean eliminateDeadCode() {
		boolean someChange = false;
		boolean change = true;
		while(change) {
			change = false;

			for(Block block: blocks) {
				block.clearPropagationSets();
			}
			boolean lvChange = true;
			while(lvChange) {
				lvChange = false;
				for(Block block: blocks) {
					lvChange = block.updateLiveVariables() || lvChange;
				}
			}
			
			Queue<Block> queued = new LinkedList<>();
			queued.addAll(blocks);
			for(Block block: queued) {
				change = block.eliminateDeadCode() || change;
			}
			
			if(change) someChange = true;
		}
		return someChange;
	}
	
	public boolean foldConstants() {
		boolean change = false;
		for(Block block: blocks) {
			change = block.foldConstants() || change;
		}
		return change;
	}
	
	public boolean propagateAssignments(boolean consts) {
		boolean someChange = false;
		boolean change = true;
		while(change) {
			change = false;

			boolean paChange = true;
			while(paChange) {
				paChange = false;
				for(Block block: blocks) {
					paChange = block.updateAvailableExpressions() || paChange;
				}
			}
			
			for(Block block: blocks) {
				change = block.propagateAssignments(consts) || change;
			}
			
			if(change) someChange = true;
		}
		return someChange;
	}
	
	public boolean eliminateCommonSubexpressions() {
		boolean someChange = false;
		boolean change = true;
		while(change) {
			change = false;

			for(Block block: blocks) {
				block.clearPropagationSets();
			}
			boolean paChange = true;
			while(paChange) {
				paChange = false;
				for(Block block: blocks) {
					paChange = block.updateAvailableExpressions() || paChange;
				}
			}
			
			for(Block block: blocks) {
				change = block.eliminateCommonSubexpressions() || change;
			}
			
			if(change) someChange = true;
		}
		return someChange;
	}
	
	public Block getEntryBlock() {
		return entry;
	}
	
	public Collection<Block> getBlocks() {
		return blocks;
	}
	
	protected void removeBlock(Block block) {
		blocks.remove(block);
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

	@Override
	public Iterator<Block> iterator() {
		return new GraphIterator(blocks);
	}
	
}
