package ir;

public class Graph {
	
	private String name;
	
	public Graph(String function, ValueCode code) {
		name = function;
	}
	
	public String dotGraph() {
		return "digraph " + name + " {}";
	}
	
}
