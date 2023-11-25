package ast;

import java.util.ArrayList;
import java.util.List;

import coco.Token;
import coco.Token.Kind;
import ir.InstructType;
import ir.Instruction;
import ir.ValueCode;
import types.BoolType;
import types.ErrorType;
import types.NumberType;
import types.Type;
import types.TypeChecker;

public class Operation extends CheckableNode {
	
	private Node left;
	private Node right;
	private String operation;
	private Token opToken;
	
	public Operation(Node left, Node right, String operation, Token opToken) {
		this.left = left;
		this.right = right;
		this.operation = operation;
		this.opToken = opToken;
	}
	
	public int lineNumber() {
		return opToken.lineNumber();
	}
	
	public int charPosition() {
		return opToken.lineNumber();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(left instanceof CheckableNode) ((CheckableNode) left).checkFunctionCalls(parent);
		if(right instanceof CheckableNode) ((CheckableNode) right).checkFunctionCalls(parent);
	}
	
	public Type getType() {
		if(opToken.kind() == Kind.AND || opToken.kind() == Kind.OR) {
			if(!BoolType.is(left.getType()) || !BoolType.is(right.getType())) {
				ErrorType error = new ErrorType();
				error.setError(opToken, "Cannot " + toString(opToken, left.getType().toString(), right.getType().toString()) + ".");
				return error;
			} else {
				return new BoolType();
			}
		} else if(opToken.kind() == Kind.NOT) {
			if(!BoolType.is(right.getType())) {
				ErrorType error = new ErrorType();
				error.setError(opToken, "Cannot " + toString(opToken, left.getType().toString(), right.getType().toString()) + ".");
				return error;
			} else {
				return new BoolType();
			}
		} else {
			if(!NumberType.is(left.getType()) || !NumberType.is(right.getType())) {
				ErrorType error = new ErrorType();
				error.setError(opToken, "Cannot " + toString(opToken, right.getType().toString()) + ".");
				return error;
			} else {
				if(operation.startsWith("Relation")) {
					return new BoolType();
				} else {
					return left.getType();
				}
			}
		}
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		Type opType = getType();
		if(opType instanceof ErrorType) {
			reporter.reportError((ErrorType) opType);
		}
		
		if(opToken.kind() != Kind.NOT) left.checkType(reporter, returnType, functionName);
		right.checkType(reporter, returnType, functionName);
	}
	
	public ValueCode genCode(ir.Variables variables) {
		List<Instruction> instructions = new ArrayList<>();
		
		ValueCode leftCode = null;
		if(opToken.kind() != Kind.NOT) {
			leftCode = left.genCode(variables);
			instructions.addAll(leftCode.instructions);
		}
		
		ValueCode rightCode = right.genCode(variables);
		instructions.addAll(rightCode.instructions);
		
		String result = variables.getTemp();
		switch(opToken.kind()) {
			case EQUAL_TO: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.EQUAL, rightCode.returnValue));
				break;
			case NOT_EQUAL: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.NOT_EQUAL, rightCode.returnValue));
				break;
			case LESS_EQUAL: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.LESS_EQUAL, rightCode.returnValue));
				break;
			case GREATER_EQUAL: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.GREATER_EQUAL, rightCode.returnValue));
				break;
			case LESS_THAN: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.LESS, rightCode.returnValue));
				break;
			case GREATER_THAN: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.GREATER, rightCode.returnValue));
				break;
			case ADD: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.ADD, rightCode.returnValue));
				break;
			case SUB: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.SUB, rightCode.returnValue));
				break;
			case OR: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.OR, rightCode.returnValue));
				break;
			case MUL: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.MUL, rightCode.returnValue));
				break;
			case DIV: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.DIV, rightCode.returnValue));
				break;
			case MOD: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.MOD, rightCode.returnValue));
				break;
			case AND: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.AND, rightCode.returnValue));
				break;
			case POW: 
				instructions.add(new Instruction(result, leftCode.returnValue, InstructType.POW, rightCode.returnValue));
				break;
			case NOT: 
				instructions.add(new Instruction(result, InstructType.NOT, rightCode.returnValue));
				break;
			default: 
				throw new RuntimeException("Unexpected Operation Found");
		}
		
		return new ValueCode(instructions, result);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append(operation);
		print.append("\n");
		print.append(left.printPreOrder(level+1));
		print.append(right.printPreOrder(level+1));
		return print.toString();
	}
	
	private String toString(Token op, String left, String right) {
		switch(op.kind()) {
			case ADD: 
				return "add " + left + " to " + right;
			case SUB: 
				return "subtract " + right + " from " + left;
			case EQUAL_TO: 
				return "compare " + left + " with " + right;
			default: 
				return op.kind().name() + " " + left + " to " + right;
		}
	}
	
	private String toString(Token op, String right) {
		if(op.kind() != Kind.NOT) throw new RuntimeException("Error in type checking implementation");
		return "not " + right;
	}
	
}
