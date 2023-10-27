package ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Block implements Iterable<Instruction> {

	private static int index = 0;
	List<Instruction> instructions = new ArrayList<>();
	Set<Block> successors = new HashSet<>();
	String name = "B" + ++index;
	
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
	
}
