package ir;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Block implements Iterable<Instruction> {

	List<Instruction> instructions = new ArrayList<>();
	
	public void addInstruction(Instruction instruction) {
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
	
}
