package ast;

import java.util.ArrayList;
import java.util.List;

import coco.ErrorChecker;
import coco.NonexistantVariableException;
import coco.ReversibleScanner;
import coco.SyntaxException;
import coco.Token;
import coco.Variables;
import types.BoolType;
import types.ErrorType;
import types.Type;
import types.TypeChecker;
import coco.Token.Kind;
import ir.InstructType;
import ir.Instruction;
import ir.ValueCode;

public class Repeat extends CheckableNode {

	private Node decision;
	private Statements action;
	private Token start;

	public Repeat(ReversibleScanner source, Variables variables) throws SyntaxException, NonexistantVariableException {
		ErrorChecker.mustBe(Kind.REPEAT, "REPEAT", source);
		start = source.last();
		action = new Statements(source, variables);
		ErrorChecker.mustBe(Kind.UNTIL, "UNTIL", source);
		ErrorChecker.mustBe(Kind.OPEN_PAREN, "OPEN_PAREN", source);
		decision = new Relation(source, variables).genAST();
		ErrorChecker.mustBe(Kind.CLOSE_PAREN, "CLOSE_PAREN", source);
	}
	
	public int lineNumber() {
		return start.lineNumber();
	}
	
	public int charPosition() {
		return start.charPosition();
	}
	
	public void checkFunctionCalls(AST parent) {
		action.checkFunctionCalls(parent);
		if(decision instanceof CheckableNode) ((CheckableNode) decision).checkFunctionCalls(parent);
	}
	
	public Type getType() {
		return action.getType();
	}
	
	public void checkType(TypeChecker reporter, Type returnType, String functionName) {
		action.checkType(reporter, returnType, functionName);
		
		decision.checkType(reporter, returnType, functionName);
		if(!BoolType.is(decision.getType())) {
			ErrorType error = new ErrorType();
			error.setError(start, "RepeatStat requires bool condition not " + decision.getType() + ".");
			reporter.reportError(error);
		}
	}
	
	public ValueCode genCode(ir.Variables variables) {
		List<Instruction> instructions = new ArrayList<>();
		ValueCode decisionCode = decision.genCode(variables);
		
		ValueCode actionCode = action.genCode(variables);
		
		if(actionCode.instructions.size() > 0) {
			Instruction jump = new Instruction(InstructType.JUMP, actionCode.instructions.get(0), decisionCode.returnValue);
			
			instructions.addAll(actionCode.instructions);
			instructions.addAll(decisionCode.instructions);
			instructions.add(jump);
		} else {
			instructions.addAll(decisionCode.instructions);
		}

		return new ValueCode(instructions, decisionCode.returnValue);
	}
	
	public String printPreOrder(int level) {
		StringBuilder print = new StringBuilder();
		addLevel(level, print);
		print.append("RepeatStatement\n");
		print.append(action.printPreOrder(level+1));
		print.append(decision.printPreOrder(level+1));
		return print.toString();
	}
	
}
