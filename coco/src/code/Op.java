package code;

import java.util.Map;

import ir.InstructType;

public enum Op {
	ADD(2, 0), SUB(2, 1), MUL(2, 2), DIV(2, 3), MOD(2, 4), POW(2, 5), CMP(2, 6), OR(2, 13), AND(2, 14), BIC(2, 15), 
	ADDI(1, 20), SUBI(1, 21), MULI(1, 22), DIVI(1, 23), MODI(1, 24), POWI(1, 25), CMPI(1, 26), ORI(1, 33), ANDI(1, 34), BICI(1, 35), 
	LDW(1, 40), STW(1, 43), 
	BEQ(1, 47), BNE(1, 48), BLT(1, 49), BGE(1, 50), BLE(1, 51), BGT(1, 52), BSR(1, 53), 
	RET(2, 55), 
	RDI(2, 56), RDB(2, 58), WRI(2, 59), WRB(2, 61), WRL(1, 62);
	
	int format;
	int opCode;
	Op(int format, int opCode) {
		this.format = format;
		this.opCode = opCode;
	}
	
	/* private static Map<Integer, Op> opById;
	static {
		for(Op op: Op.values()) {
			opById.put(op.opCode, op);
		}
	}
	
	public static Op getOp(int id) {
		return opById.get(id);
	} */
	
	public static Op fromInstructOp(InstructType instrType, boolean immediate) {
		if(immediate) {
			switch(instrType) {
				case ADD: 
					return ADDI;
				case SUB: 
					return SUBI;
				case MUL: 
					return MULI;
				case DIV: 
					return DIVI;
				case MOD: 
					return MODI;
				case POW: 
					return POWI;
				case OR: 
					return ORI;
				case AND: 
					return ANDI;
				default: 
					throw new RuntimeException("Attempted to use fromInstructOp to get a non operation op code");
			}
		} else {
			switch(instrType) {
			case ADD: 
				return ADD;
			case SUB: 
				return SUB;
			case MUL: 
				return MUL;
			case DIV: 
				return DIV;
			case MOD: 
				return MOD;
			case POW: 
				return POW;
			case OR: 
				return OR;
			case AND: 
				return AND;
			default: 
				throw new RuntimeException("Attempted to use fromInstructOp to get a non operation op code");
		}
		}
	}
}
