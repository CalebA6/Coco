package ast;

import java.util.ArrayList;
import java.util.List;

import coco.Location;
import coco.Token;
import ir.InstructType;
import ir.Instruction;
import ir.ValueCode;
import types.ArrayAccessException;
import types.ErrorType;
import types.IntType;
import types.Type;
import types.TypeChecker;

public class ArrayIndex extends NamedNode {

	private Node index;
	private NamedNode item;
	private Token start;
	private String indexSize;
	
	private Location location;
	
	public ArrayIndex(NamedNode item, int index, List<Relation> indicies, List<Token> starts, String[] indexSizes) {
		location = this.index = indicies.get(index).genAST();
		if(index >= indicies.size() - 1) {
			start = starts.get(index);
			indexSize = indexSizes[index];
			this.item = item;
		} else {
			start = starts.get(index);
			indexSize = indexSizes[index];
			this.item = new ArrayIndex(item, index+1, indicies, starts, indexSizes);
		}
	}
	
	public int lineNumber() {
		return item.lineNumber();
	}
	
	public int charPosition() {
		return item.charPosition();
	}
	
	public Token getName() {
		return item.getName();
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public void checkFunctionCalls(AST parent) {
		if(index instanceof CheckableNode) ((CheckableNode) index).checkFunctionCalls(parent);
		item.checkFunctionCalls(parent);
	}
	
	public Type getType() {
		Type type = item.getType();
		try {
			type.decrementDimensions();
		} catch (ArrayAccessException e) {
			type = new ErrorType();
			((ErrorType) type).setError(start, "Cannot dereference " + type);
		}
		
		if(!IntType.is(index.getType())) {
			ErrorType error = new ErrorType();
			error.setError(index, "Cannot index " + item + " with " + index.getType() + ".");
			type = error;
		} else {
			/* if(index instanceof Literal) {
				boolean outOfBounds = false;
				int index = Integer.parseInt(this.index.toString());
				if(index < 0) outOfBounds = true;
				int indexSize = Integer.parseInt(this.indexSize);
				if(index >= indexSize) outOfBounds = true;
				
				if(outOfBounds) {
					ErrorType error = new ErrorType();
					error.setError(location, "Array Index Out of Bounds : " + index + " for array " + item);
					type = error;
				}
			} */
		}
		
		return type;
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(ErrorType.is(getType())) {
			reporter.reportError((ErrorType) getType());
		}

		index.checkType(reporter, returnType, functionName);
		item.checkType(reporter, returnType, functionName);
	}
	
	public ValueCode genCode(ir.Variables variables) {
		List<Instruction> instructions = new ArrayList<>();
		String begin = variables.getTemp();
		instructions.add(new Instruction(begin, "0"));
		ValueCode code = genCode(variables, begin);
		instructions.addAll(code.instructions);
		String location = variables.getTemp();
		instructions.add(new Instruction(location, code.returnValue, InstructType.MUL, "4"));
		return new ValueCode(instructions, getName().lexeme() + "[" + location + "]");
	}
	
	public ValueCode genCode(ir.Variables variables, String input) {
		List<Instruction> instructions = new ArrayList<>();
		ValueCode indexCode = index.genCode(variables);
		String indexCalc = variables.getTemp();
		instructions.add(new Instruction(indexCalc, input, InstructType.ADD, indexCode.returnValue));
		instructions.add(new Instruction(indexCalc, indexCalc, InstructType.MUL, indexSize));
		if(item instanceof ArrayIndex) {
			ValueCode fullIndex = ((ArrayIndex)item).genCode(variables, indexCalc);
			instructions.addAll(fullIndex.instructions);
			return new ValueCode(instructions, fullIndex.returnValue);
		} else {
			return new ValueCode(instructions, indexCalc);
		}
	}
	
	@Override
	public String toString() {
		return item.toString();
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("ArrayIndex\n");
		print.append(item.printPreOrder(level+1));
		return print.toString();
	}
	
}
