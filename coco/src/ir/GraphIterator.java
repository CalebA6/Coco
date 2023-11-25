package ir;

import java.util.Iterator;
import java.util.List;

public class GraphIterator implements Iterator<Block> {
	
	private List<Block> blocks;
	private int index;
	public GraphIterator(List<Block> blocks) {
		this.blocks = blocks;
	}

	@Override
	public boolean hasNext() {
		return index < blocks.size();
	}

	@Override
	public Block next() {
		return blocks.get(index++);
	}

}
