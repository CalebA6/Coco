package ir;

import java.util.List;

public class Graph {
	
	private String name;
	
	public Graph(String function, List<Instruction> code) {
		name = function;
	}
	
	public String dotGraph() {
		return "digraph " + name + " {}";
	}
	
}
