package ast;

import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import ir.Instruction;
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
	
	/* public List<Instruction> genInstr() {
		switch(operation.kind()) {
		case ASSIGN: 
		case ADD_ASSIGN: 
		case SUB_ASSIGN: 
		case MUL_ASSIGN: 
		case DIV_ASSIGN: 
		case MOD_ASSIGN: 
		case POW_ASSIGN: 
			String opVar = Instruction.getTemp();
			List<Instruction> instructions = operand.genInstr(opVar);
			if(operation.kind() == Kind.ASSIGN) {
				instructions.add(new Instruction(InstrType.COPY, opVar, assignee));
			} else {
				Instruction op;
				Instruction copy;
				switch(operation.kind()) {
				case ADD_ASSIGN: 
					op = new Instruction(InstrType.ADD, opVar, assignee);
				case SUB_ASSIGN: 
					op = new Instruction(InstrType.SUB, opVar, assignee);
					
				}
				copy = new Instruction(InstrType.COPY, op.result(), assignee);
				instructions.add(op);
				instructions.add(copy);
			}
		}
	} */
	
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
