package reg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ir.Block;

public class LiveRange {
	
	private String name;
	private Collection<BlockLocation> starts;
	private Collection<Block> blocks;
	private Collection<BlockLocation> ends;
	private Collection<LiveRange> conflicts;
	private Collection<LiveRange> stack;
	private int regAlloc = 0;
	
	public LiveRange(String name) {
		this.name = name;
		this.conflicts = new ArrayList<>();
		this.starts = new ArrayList<>();
		this.blocks = new HashSet<>();
		this.ends = new ArrayList<>();
	}
	
	public void addStart(int instr, Block block) {
		starts.add(new BlockLocation(instr, block));
		blocks.add(block);
	}
	
	public void addBlock(Block block) {
		blocks.add(block);
	}
	
	public void addEnd(int instr, Block block) {
		ends.add(new BlockLocation(instr, block));
		blocks.add(block);
	}
	
	public void link(Collection<LiveRange> graph) {
		for(LiveRange var: graph) {
			for(Block block: blocks) {
				List<Collection<String>> liveVars = block.genLiveSets();
				for(Collection<String> varSet: liveVars) {
					if(varSet.contains(name) && varSet.contains(var.name)) {
						conflicts.add(var);
						break;
					}
				}
			}
		}
	}
	
	public void setStack(Collection<LiveRange> stack) {
		this.stack = stack;
	}
	
	public int numConflicts() {
		int num = 0;
		for(LiveRange lv: conflicts) {
			if(!stack.contains(lv)) {
				++num;
			}
		}
		return num;
	}
	
	public void setReg(int numRegisters) {
		boolean[] possibleAllocs = new boolean[numRegisters];
		for(int possibleAlloc=0; possibleAlloc<numRegisters; ++possibleAlloc) {
			possibleAllocs[possibleAlloc] = true;
		}
		
		for(LiveRange conflict: conflicts) {
			if(conflict.isAlloced()) {
				possibleAllocs[conflict.getReg() - 1] = false;
			}
		}
		
		regAlloc = 0;
		while(regAlloc < numRegisters) {
			if(possibleAllocs[regAlloc]) break;
			++regAlloc;
		}
		++regAlloc;
		if(regAlloc > numRegisters) {
			regAlloc = 0;
		}
	}
	
	public int getReg() {
		return regAlloc;
	}
	
	public boolean isAlloced() {
		return regAlloc > 0;
	}
	
}
