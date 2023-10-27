package ir;

import java.util.Iterator;

public class BlockIterator implements Iterator<Instruction> {
	
	private Block block;
	private int size;
	private int index;
	
	public BlockIterator(Block block) {
		this.block = block;
		this.size = block.numInstructions();
		this.index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < size;
	}

	@Override
	public Instruction next() {
		return block.getInstruction(index++);
	}

}
