package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import ir.InstructType;
import ir.Instruction;
import ir.ValueCode;
import types.ErrorType;
import types.FloatType;
import types.IntType;
import types.Type;
import types.TypeChecker;
import coco.Token.Kind;

public class Assignment extends CheckableNode {
	
	NamedNode assignee;
	Token operation;
	Node operand = null;

	public Assignment(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		assignee = (NamedNode)new Designator(source, variables).genAST();
		ErrorChecker.checkForMoreInput(source, "assignment operator");
		Token assignOp = source.next();
		operation = assignOp;
		if((assignOp.kind() == Kind.ASSIGN) || (assignOp.kind() == Kind.ADD_ASSIGN) || (assignOp.kind() == Kind.SUB_ASSIGN) || (assignOp.kind() == Kind.MUL_ASSIGN) || (assignOp.kind() == Kind.DIV_ASSIGN) || (assignOp.kind() == Kind.MOD_ASSIGN) || (assignOp.kind() == Kind.POW_ASSIGN)) {
			operand = new Relation(source, variables).genAST();
		} else if((assignOp.kind() == Kind.UNI_INC) || (assignOp.kind() == Kind.UNI_DEC)) {
			operand = new Sum(assignee, assignOp).genAST();
		} else {
			throw new SyntaxException("Expected assignment operator but got " + assignOp.kind() + " .", assignOp);
		}
	}
	
	public int lineNumber() {
		return operation.lineNumber();
	}
	
	public int charPosition() {
		return operation.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(operand != null && operand instanceof CheckableNode) {
			((CheckableNode) operand).checkFunctionCalls(parent);
		}
	}
	
	public Type getType() {
		return assignee.getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(assignee instanceof ArrayIndex) ((ArrayIndex) assignee).setLocation(operation);
		
		if(operand != null) {
			if(!assignee.getType().equals(operand.getType())) {
				ErrorType error = new ErrorType();
				error.setError(assignee, "Cannot set " + operand.getType() + " to " + assignee.getType());
				reporter.reportError(error);
			}
		} else {
			if(!IntType.is(assignee.getType()) && !FloatType.is(assignee.getType())) {
				ErrorType error = new ErrorType();
				error.setError(assignee, "Cannot increment " + assignee.getType());
				reporter.reportError(error);
			}
		}

		assignee.checkType(reporter, returnType, functionName);
		if(operand != null) operand.checkType(reporter, returnType, functionName);
	}
	
	public ValueCode genCode(ir.Variables variables) {
		List<Instruction> instructions = new ArrayList<>();
		ValueCode operandCode = operand.genCode(variables);
		ValueCode assigneeCode = assignee.genCode(variables);
		instructions.addAll(operandCode.instructions);
		instructions.addAll(assigneeCode.instructions);
		switch(operation.kind()) {
			case ASSIGN: 
			case UNI_INC: 
			case UNI_DEC: 
				instructions.add(new Instruction(assigneeCode.returnValue, operandCode.returnValue));
				break;
			case ADD_ASSIGN: 
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.ADD, operandCode.returnValue));
				break;
			case SUB_ASSIGN: 
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.SUB, operandCode.returnValue));
				break;
			case MUL_ASSIGN: 
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.MUL, operandCode.returnValue));
				break;
			case DIV_ASSIGN: 
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.DIV, operandCode.returnValue));
				break;
			case MOD_ASSIGN: 
				/*String factor = variables.getTemp();
				instructions.add(new Instruction(factor, assigneeCode.returnValue, InstructType.DIV, operandCode.returnValue));
				String goesAway = variables.getTemp();
				instructions.add(new Instruction(goesAway, operandCode.returnValue, InstructType.MUL, factor));
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.SUB, goesAway));*/
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.MOD, operandCode.returnValue));
				break;
			case POW_ASSIGN: 
				/* String times = variables.getTemp();
				String original = variables.getTemp();
				instructions.add(new Instruction(times, operandCode.returnValue));
				instructions.add(new Instruction(original, assigneeCode.returnValue));
				instructions.add(new Instruction(assigneeCode.returnValue, 1));
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.MUL, original));*/
				instructions.add(new Instruction(assigneeCode.returnValue, assigneeCode.returnValue, InstructType.POW, operandCode.returnValue));
				break;
			default: 
				throw new RuntimeException("Unexpected Assignment Operation Encountered");
		}
		return new ValueCode(instructions, assigneeCode.returnValue);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("Assignment\n");
		print.append(assignee.printPreOrder(level+1));
		if(operand != null) {
			print.append(operand.printPreOrder(level+1));
		}
		return print.toString();
	}
	
}
