package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Token.Kind;
import ir.InstructType;
import ir.Instruction;
import ir.ValueCode;
import coco.Variables;
import types.BoolType;
import types.ErrorType;
import types.Type;
import types.TypeChecker;

public class While extends CheckableNode {
	
	private Node decision;
	private Statements action;
	private Token start;

	public While(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.WHILE, "WHILE", source);
		start = source.last();
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables).genAST();
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
		ErrorChecker.mustBe(Kind.DO, "DO", source);
		action = new Statements(source, variables);
		ErrorChecker.mustBe(Kind.OD, "OD", source);
	}
	
	public int lineNumber() {
		return start.lineNumber();
	}
	
	public int charPosition() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		if(decision instanceof CheckableNode) ((CheckableNode) decision).checkFunctionCalls(parent);
		action.checkFunctionCalls(parent);
	}
	
	public Type getType() {
		return action.getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		if(!BoolType.is(decision.getType())) {
			ErrorType error = new ErrorType();
			error.setError(this, "WhileStat requires bool condition not " + decision.getType() + ".");
			reporter.reportError(error);
		}
		
		action.checkType(reporter, returnType, functionName);
	}
	
	public ValueCode genCode(ir.Variables variables) {
		List<Instruction> instructions = new ArrayList<>();
		ValueCode decisionCode = decision.genCode(variables);
		instructions.addAll(decisionCode.instructions);
		String jumpDecision = variables.getTemp();
		instructions.add(new Instruction(jumpDecision, InstructType.NOT, decisionCode.returnValue));
		
		ValueCode actionCode = action.genCode(variables);

		Instruction afterAction = new Instruction();
		Instruction jump = new Instruction(InstructType.JUMP, afterAction, jumpDecision);
		
		instructions.add(jump);
		instructions.addAll(actionCode.instructions);
		if(decisionCode.instructions.size() > 0) {
			instructions.add(new Instruction(InstructType.JUMP, decisionCode.instructions.get(0)));
		} else {
			instructions.add(new Instruction(InstructType.JUMP, jump));
		}
		instructions.add(afterAction);

		return new ValueCode(instructions, decisionCode.returnValue);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("WhileStatement\n");
		print.append(decision.printPreOrder(level+1));
		print.append(action.printPreOrder(level+1));
		return print.toString();
	}
	
}
