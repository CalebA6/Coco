package code;

public class Code {
	
	Op op;
	int a;
	int b;
	int c;
	
	public Code(Op op, int a, int b, int c) {
		this.op = op;
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public int gen() {
		if(op.opCode >= 64 || a >= 32 || a < 0 || b >= 32 || b < 0 || op.format > 3 || op.format < 1) {
			throw new RuntimeException("Attempted to generate bad instruction");
		}
		
		switch(op.format) {
		case 1: 
			if(c > 32767 || c < -32768) {
				throw new RuntimeException("Attempted to generate bad instruction");
			}
			return (op.opCode << 26) | ((31 & a) << 21) | ((31 & b) << 16) | (65535 & c);
		case 2: 
			if(c >= 32 || c < 0) {
				throw new RuntimeException("Attempted to generate bad instruction");
			}
			return (op.opCode << 26) | ((31 & a) << 21) | ((31 & b) << 16) | (31 & c);
		}
		
		throw new RuntimeException("Unknown instruction format");
	}
	
	public void setJump(int distance) {
		c = distance;
	}
	
	@Override
	public String toString() {
		return (op + "(" + a + ", " + b + ", " + c + ")");
	}

}
