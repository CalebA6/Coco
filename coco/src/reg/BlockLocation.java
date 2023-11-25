package reg;

import ir.Block;

public class BlockLocation {
	
	int instruction;
	Block block;
	
	public BlockLocation(int instruction, Block block) {
		this.instruction = instruction;
		this.block = block;
	}
	
}
